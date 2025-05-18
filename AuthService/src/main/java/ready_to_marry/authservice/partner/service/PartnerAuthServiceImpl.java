package ready_to_marry.authservice.partner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.account.service.AccountService;
import ready_to_marry.authservice.common.config.AppProperties;
import ready_to_marry.authservice.common.enums.AccountStatus;
import ready_to_marry.authservice.common.enums.AuthMethod;
import ready_to_marry.authservice.common.enums.Role;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.common.util.MaskingUtil;
import ready_to_marry.authservice.partner.dto.request.PartnerProfileRequest;
import ready_to_marry.authservice.partner.dto.request.PartnerSignupRequest;
import ready_to_marry.authservice.partner.email.EmailService;
import ready_to_marry.authservice.token.service.VerificationTokenService;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerAuthServiceImpl implements PartnerAuthService {
    private final AccountService accountService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void registerPartner(PartnerSignupRequest request) {
        // 0) 같은 loginId의 만료된 대기 계정 정리
        try {
            accountService.findByLoginId(request.getLoginId())
                    .filter(a -> a.getStatus() == AccountStatus.WAITING_EMAIL_VERIFICATION && a.getCreatedAt().isBefore(OffsetDateTime.now().minusMinutes(10)))
                    .ifPresent(a -> {
                        // 0-1) auth_account 에서 해당 계정 삭제
                        try {
                            accountService.deleteById(a.getAccountId());
                            String maskedLoginId = MaskingUtil.maskEmailLoginId(request.getLoginId());
                            log.info("Expired unverified account(loginId={}) auto-deleted: identifierType=accountId, identifierValue={}", maskedLoginId, a.getAccountId());
                        } catch (DataAccessException ex) {
                            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_DELETE_FAILURE, a.getAccountId(), ex);
                            throw new InfrastructureException(ErrorCode.DB_DELETE_FAILURE, ex);
                        }

                        // 0-2) PARTNER SERVICE에 요청 (INTERNAL API) -> 해당 계정의 partner_profile(partnerDB) 삭제
                        // TODO: INTERNAL API 호출 로직 추가
                        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가
                    });
        } catch (DataAccessException ex) {
            String maskedLoginId = MaskingUtil.maskEmailLoginId(request.getLoginId());
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        // 1) loginId 중복 검사
        try {
            accountService.findByLoginId(request.getLoginId())
                    .ifPresent(a -> {
                        String maskedLoginId = MaskingUtil.maskEmailLoginId(request.getLoginId());
                        log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DUPLICATE_LOGIN_ID.getMessage(), maskedLoginId);
                        throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
                    });
        } catch (DataAccessException ex) {
            String maskedLoginId = MaskingUtil.maskEmailLoginId(request.getLoginId());
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        // 2) password 암호화
        String encoded = passwordEncoder.encode(request.getPassword());

        // 3)-1 AuthAccount 엔티티 생성
        AuthAccount account = AuthAccount.builder()
                // PARTNER는 EMAIL 방식
                .authMethod(AuthMethod.EMAIL)
                .loginId(request.getLoginId())
                .password(encoded)
                .role(Role.PARTNER)
                // 파트너의 이메일 인증 필요
                .status(AccountStatus.WAITING_EMAIL_VERIFICATION)
                .build();

        // 3)-2 auth_account(authDB)에 저장
        AuthAccount savedAccount;
        try {
            savedAccount = accountService.save(account);
        } catch (DataAccessException ex) {
            String maskedLoginId = MaskingUtil.maskEmailLoginId(request.getLoginId());
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
        }

        // 4)-1 PARTNER SERVICE에 요청할 DTO 생성 (INTERNAL API)
        PartnerProfileRequest internalRequest = PartnerProfileRequest.builder()
                .name(request.getName())
                .companyName(request.getCompanyName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .companyNum(request.getCompanyNum())
                .businessNum(request.getBusinessNum())
                .build();

        // 4)-2 PARTNER SERVICE에 요청 (INTERNAL API) -> partner_profile(partnerDB)에 저장
        // TODO: INTERNAL API 호출 로직 추가
        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가
        // FIXME: INTERNAL API 호출 결과에서 가져오는 partnerId로 변경 (임시 코드)
        Random rnd = new Random();
        Long partnerId = rnd.nextLong();

        // 5) auth_account.partner_id 업데이트
        try {
            accountService.updatePartnerId(savedAccount.getAccountId(), partnerId);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), savedAccount.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
        }

        // 6) 이메일 verification token 발급 및 Redis 저장
        String token = UUID.randomUUID().toString();
        try {
            verificationTokenService.save(token, savedAccount.getAccountId());
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.VERIFICATION_TOKEN_SAVE_FAILURE.getMessage(), savedAccount.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.VERIFICATION_TOKEN_SAVE_FAILURE, ex);
        }

        // 7) 이메일 인증 메일 전송
        String link = String.format("%s/auth/partners/verify?token=%s", appProperties.getUrlBase(), token);
        try {
            emailService.sendPartnerVerification(savedAccount.getLoginId(), link);
        } catch (MailException ex) {
            String maskedLoginId = MaskingUtil.maskEmailLoginId(request.getLoginId());
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.EMAIL_SEND_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.EMAIL_SEND_FAILURE, ex);
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        // 1) 이메일 verification token 으로 accountId 조회
        UUID accountId;
        try {
            accountId = verificationTokenService.findAccountId(token)
                    .orElseThrow(() -> {
                        String maskedToken = MaskingUtil.maskEmailLoginId(token);
                        log.error("{}: identifierType=token, identifierValue={}", ErrorCode.INVALID_VERIFICATION_TOKEN.getMessage(), maskedToken);
                        return new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN);
                    });
        } catch (DataAccessException ex) {
            String maskedToken = MaskingUtil.maskEmailLoginId(token);
            log.error("{}: identifierType=token, identifierValue={}", ErrorCode.VERIFICATION_TOKEN_RETRIEVE_FAILURE.getMessage(), maskedToken, ex);
            throw new InfrastructureException(ErrorCode.VERIFICATION_TOKEN_RETRIEVE_FAILURE, ex);
        }

        // 2) auth_account.status 업데이트 WAITING_EMAIL_VERIFICATION -> PENDING_ADMIN_APPROVAL
        try {
            accountService.updateStatus(accountId, AccountStatus.PENDING_ADMIN_APPROVAL);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
        }

        // 3) 이메일 verification token 삭제
        try {
            verificationTokenService.delete(token);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.VERIFICATION_TOKEN_DELETE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.VERIFICATION_TOKEN_DELETE_FAILURE, ex);
        }
    }
}
