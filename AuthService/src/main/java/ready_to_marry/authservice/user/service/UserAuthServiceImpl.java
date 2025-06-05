package ready_to_marry.authservice.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
import ready_to_marry.authservice.social.dto.SocialLoginResult;
import ready_to_marry.authservice.token.service.RefreshTokenService;
import ready_to_marry.authservice.user.dto.request.UserProfileCompletionRequest;
import ready_to_marry.authservice.user.dto.request.UserProfileRequest;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {
    private final AccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final UserClient userClient;

    @Override
    @Transactional
    public SocialLoginResult socialLogin(String provider, String socialId) {
        // 1) 소셜 ID로 계정 조회 또는 신규 생성
        AuthAccount account;
        try {
            account = accountService.findByLoginId(socialId)
                    .orElseGet(() -> {
                        try {
                            AuthMethod method = AuthMethod.valueOf(provider.toUpperCase());
                            AuthAccount newAccount = AuthAccount.builder()
                                    .authMethod(method)
                                    .loginId(socialId)
                                    .role(Role.USER)
                                    .status(AccountStatus.WAITING_PROFILE_COMPLETION)
                                    .build();
                            return accountService.save(newAccount);
                        } catch (IllegalArgumentException ex) {
                            log.error("{}: identifierType=provider, identifierValue={}", ErrorCode.PROVIDER_NOT_SUPPORTED.getMessage(), provider);
                            throw new BusinessException(ErrorCode.PROVIDER_NOT_SUPPORTED);
                        } catch (DataAccessException ex) {
                            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), MaskingUtil.maskSocialLoginId(socialId), ex);
                            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
                        }
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), MaskingUtil.maskSocialLoginId(socialId), ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        // 2) ACTIVE 상태이면 JWT 토큰 발급
        if (account.getStatus() == AccountStatus.ACTIVE) {
            // 2-1) JWT 토큰 발급 (Access Token 생성)
            String accessToken = jwtTokenProvider.generateAccessToken(
                    account.getAccountId().toString(),
                    // role, userId 설정
                    JwtClaims.builder()
                            .role(account.getRole().name())
                            .userId(account.getUserId())
                            .build()
            );

            // 2-2) JWT 토큰 발급 (Refresh Token 생성)
            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    account.getAccountId().toString()
            );


            // 2-3) Refresh Token Redis에 저장
            try {
                refreshTokenService.save(account.getAccountId(), refreshToken);
            }  catch (DataAccessException ex) {
                log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.REFRESH_TOKEN_SAVE_FAILURE.getMessage(), account.getAccountId(), ex);
                throw new InfrastructureException(ErrorCode.REFRESH_TOKEN_SAVE_FAILURE, ex);
            }

            return SocialLoginResult.active(accessToken, refreshToken, jwtProperties.getAccessExpiry());
        }

        // 3) WAITING_PROFILE_COMPLETION 상태이면 프로필 등록 유도
        return SocialLoginResult.incomplete(account.getAccountId());
    }

    @Override
    @Transactional
    public JwtResponse completeUserProfile(UserProfileCompletionRequest request) {
        // 1) accountId 유효성 및 상태 확인
        AuthAccount account;
        try {
            account = accountService.findById(request.getAccountId())
                    .orElseThrow(() -> {
                        log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.ACCOUNT_NOT_FOUND.getMessage(), request.getAccountId());
                        return new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), request.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        if (account.getStatus() != AccountStatus.WAITING_PROFILE_COMPLETION) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.PROFILE_ALREADY_COMPLETED.getMessage(), request.getAccountId());
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_COMPLETED);
        }

        // 2)-1 USER SERVICE에 요청할 DTO 생성 (INTERNAL API)
        UserProfileRequest internalRequest = UserProfileRequest.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        // 2)-2 USER SERVICE에 요청 (INTERNAL API) → user_profile(userDB)에 저장
        // TODO: INTERNAL API 호출 로직 추가 O
        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가 O
        // FIXME: INTERNAL API 호출 결과에서 가져오는 userId로 변경 (임시 코드) O
        System.out.println("추가 요청 시작");
        Long userId;
        try {
            userId = userClient.savePartnerProfile(internalRequest);
        } catch (Exception e) {
            throw new InfrastructureException(ErrorCode.EXTERNAL_API_FAILURE, e);
        }
        System.out.println("추가 요청 완료");

        // 3) auth_account에 userId, status 업데이트
        try {
            accountService.updateUserIdAndStatus(request.getAccountId(), userId, AccountStatus.ACTIVE);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), request.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
        }

        // 4) JWT 토큰 발급 (Access Token 생성)
        String accessToken = jwtTokenProvider.generateAccessToken(
                account.getAccountId().toString(),
                // role, userId 설정
                JwtClaims.builder()
                        .role(account.getRole().name())
                        .userId(account.getUserId())
                        .build()
        );

        // 5) JWT 토큰 발급 (Refresh Token 생성)
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                account.getAccountId().toString()
        );


        // 6) Refresh Token Redis에 저장
        try {
            refreshTokenService.save(account.getAccountId(), refreshToken);
        }  catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.REFRESH_TOKEN_SAVE_FAILURE.getMessage(), account.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.REFRESH_TOKEN_SAVE_FAILURE, ex);
        }

        // 7) 최종 응답 DTO 반환
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessExpiry())
                .build();
    }
}
