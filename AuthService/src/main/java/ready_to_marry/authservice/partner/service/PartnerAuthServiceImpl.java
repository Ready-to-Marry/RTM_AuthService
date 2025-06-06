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
import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.common.enums.AccountStatus;
import ready_to_marry.authservice.common.enums.AuthMethod;
import ready_to_marry.authservice.common.enums.Role;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.common.jwt.JwtClaims;
import ready_to_marry.authservice.common.jwt.JwtProperties;
import ready_to_marry.authservice.common.jwt.JwtTokenProvider;
import ready_to_marry.authservice.common.util.MaskingUtil;
import ready_to_marry.authservice.partner.config.AuthPartnerProperties;
import ready_to_marry.authservice.partner.dto.request.PartnerLoginRequest;
import ready_to_marry.authservice.partner.dto.request.PartnerProfileRequest;
import ready_to_marry.authservice.partner.dto.request.PartnerSignupRequest;
import ready_to_marry.authservice.partner.email.EmailService;
import ready_to_marry.authservice.token.service.RefreshTokenService;
import ready_to_marry.authservice.token.service.VerificationTokenService;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerAuthServiceImpl implements PartnerAuthService {
    private final AccountService accountService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthPartnerProperties authPartnerProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final PartnerClient partnerClient;

    @Override
    @Transactional
    public void registerPartner(PartnerSignupRequest request) {
        // 0) 마스킹된 loginId 준비 (로그용)
        String maskedLoginId = MaskingUtil.maskEmailLoginId(request.getLoginId());

        // 1) 같은 loginId의 만료된 대기 계정 정리
        try {
            accountService.findByLoginId(request.getLoginId())
                    .filter(a -> a.getStatus() == AccountStatus.WAITING_EMAIL_VERIFICATION && a.getCreatedAt().isBefore(OffsetDateTime.now().minusMinutes(10)))
                    .ifPresent(a -> {
                        // 0-1) auth_account 에서 해당 계정 삭제
                        try {
                            accountService.deleteById(a.getAccountId());
                            log.info("Expired unverified account(loginId={}) auto-deleted: identifierType=accountId, identifierValue={}", maskedLoginId, a.getAccountId());
                        } catch (DataAccessException ex) {
                            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_DELETE_FAILURE, a.getAccountId(), ex);
                            throw new InfrastructureException(ErrorCode.DB_DELETE_FAILURE, ex);
                        }
                        System.out.println("삭제 요청 시작");
                        // 0-2) PARTNER SERVICE에 요청 (INTERNAL API) -> 해당 계정의 partner_profile(partnerDB) 삭제
                        // TODO: INTERNAL API 호출 로직 추가 O
                        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가 O
                        try {
                            partnerClient.deletePartnerProfile(a.getPartnerId());
                        } catch (Exception e) {
                            log.error("{}: Failed to delete partner profile for partnerId={}", ErrorCode.EXTERNAL_API_FAILURE, a.getPartnerId(), e);
                            throw new InfrastructureException(ErrorCode.EXTERNAL_API_FAILURE, e);
                        }
                        System.out.println("삭제 완료");
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        // 2) loginId 중복 검사
        try {
            accountService.findByLoginId(request.getLoginId())
                    .ifPresent(a -> {
                        log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DUPLICATE_LOGIN_ID.getMessage(), maskedLoginId);
                        throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        // 3) password 암호화
        String encoded = passwordEncoder.encode(request.getPassword());

        // 4)-1 AuthAccount 엔티티 생성
        AuthAccount account = AuthAccount.builder()
                // PARTNER는 EMAIL 방식
                .authMethod(AuthMethod.EMAIL)
                .loginId(request.getLoginId())
                .password(encoded)
                .role(Role.PARTNER)
                // 파트너의 이메일 인증 필요
                .status(AccountStatus.WAITING_EMAIL_VERIFICATION)
                .build();

        // 4)-2 auth_account(authDB)에 저장
        AuthAccount savedAccount;
        try {
            savedAccount = accountService.save(account);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
        }

        // 5)-1 PARTNER SERVICE에 요청할 DTO 생성 (INTERNAL API)
        PartnerProfileRequest internalRequest = PartnerProfileRequest.builder()
                .name(request.getName())
                .companyName(request.getCompanyName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .companyNum(request.getCompanyNum())
                .businessNum(request.getBusinessNum())
                .build();

        System.out.println("추가 요청 시작");
        // 5)-2 PARTNER SERVICE에 요청 (INTERNAL API) -> partner_profile(partnerDB)에 저장
        // TODO: INTERNAL API 호출 로직 추가 O
        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가 O
        // FIXME: INTERNAL API 호출 결과에서 가져오는 partnerId로 변경 (임시 코드) O
        Long partnerId;
        try {
            partnerId = partnerClient.savePartnerProfile(internalRequest);
        } catch (Exception e) {
            throw new InfrastructureException(ErrorCode.EXTERNAL_API_FAILURE, e);
        }
        System.out.println("추가 요청 완료");

        // 6) auth_account에 partnerId 업데이트
        try {
            accountService.updatePartnerId(savedAccount.getAccountId(), partnerId);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), savedAccount.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
        }

        // 7) 이메일 verification token 발급 및 Redis 저장
        String token = UUID.randomUUID().toString();
        try {
            verificationTokenService.save(token, savedAccount.getAccountId());
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.VERIFICATION_TOKEN_SAVE_FAILURE.getMessage(), savedAccount.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.VERIFICATION_TOKEN_SAVE_FAILURE, ex);
        }

        // 8) 이메일 인증 메일 전송
        String link = String.format("%s?token=%s", authPartnerProperties.getVerifyPath(), token);
        try {
            emailService.sendPartnerVerification(savedAccount.getLoginId(), link);
        } catch (MailException ex) {
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.EMAIL_SEND_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.EMAIL_SEND_FAILURE, ex);
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        // 0) 마스킹된 token 준비 (로그용)
        String maskedToken = MaskingUtil.maskToken(token);

        // 1) 이메일 verification token 으로 accountId 조회
        UUID accountId;
        try {
            accountId = verificationTokenService.findAccountId(token)
                    .orElseThrow(() -> {
                        log.error("{}: identifierType=token, identifierValue={}", ErrorCode.INVALID_VERIFICATION_TOKEN.getMessage(), maskedToken);
                        return new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN);
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=token, identifierValue={}", ErrorCode.VERIFICATION_TOKEN_RETRIEVE_FAILURE.getMessage(), maskedToken, ex);
            throw new InfrastructureException(ErrorCode.VERIFICATION_TOKEN_RETRIEVE_FAILURE, ex);
        }

        // 2) auth_account에 status 업데이트 (WAITING_EMAIL_VERIFICATION -> PENDING_ADMIN_APPROVAL)
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

    @Override
    @Transactional(readOnly = true)
    public JwtResponse login(PartnerLoginRequest request) {
        // 0) 마스킹된 loginId 준비 (로그용)
        String maskedLoginId = MaskingUtil.maskEmailLoginId(request.getLoginId());

        // 1) 계정 조회 및 승인 상태 확인
        AuthAccount account;
        try {
            account = accountService.findByLoginId(request.getLoginId())
                    .filter(a -> a.getRole() == Role.PARTNER)
                    .orElseThrow(() -> {
                        log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.INVALID_CREDENTIALS.getMessage(), maskedLoginId);
                        return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        switch (account.getStatus()) {
            case WAITING_EMAIL_VERIFICATION:
                // 이메일 인증 전
                log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.EMAIL_NOT_VERIFIED.getMessage(), maskedLoginId);
                throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);

            case PENDING_ADMIN_APPROVAL:
                // 관리자 승인 대기 중
                log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.PENDING_ADMIN_APPROVAL.getMessage(), maskedLoginId);
                throw new BusinessException(ErrorCode.PENDING_ADMIN_APPROVAL);

            case ACTIVE:
                // 정상 로그인 가능
                break;

            default:
                // (이론적으로 여기로 오지는 않지만, 안전망으로 처리)
                log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.INVALID_CREDENTIALS.getMessage(), maskedLoginId);
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.INVALID_CREDENTIALS.getMessage(), maskedLoginId);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3) Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                account.getAccountId().toString(),
                // role, adminRole 설정
                JwtClaims.builder()
                        .role(account.getRole().name())
                        .partnerId(account.getPartnerId())
                        .build()
        );

        // 4) Refresh Token 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                account.getAccountId().toString()
        );

        // 5) Refresh Token Redis에 저장
        try {
            refreshTokenService.save(account.getAccountId(), refreshToken);
        }  catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.REFRESH_TOKEN_SAVE_FAILURE.getMessage(), account.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.REFRESH_TOKEN_SAVE_FAILURE, ex);
        }

        // 6) 응답 DTO
        long expiresIn = jwtProperties.getAccessExpiry(); // 초 단위 만료 시간

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
