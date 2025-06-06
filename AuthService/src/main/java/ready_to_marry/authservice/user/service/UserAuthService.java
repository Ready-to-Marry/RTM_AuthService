package ready_to_marry.authservice.user.service;

import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.social.dto.SocialLoginResult;
import ready_to_marry.authservice.user.dto.request.UserProfileCompletionRequest;

/**
 * 유저 소셜 로그인 및 프로필 완성 후 JWT 발급을 담당하는 서비스 인터페이스
 *
 * OAuth2 기반 소셜 로그인 후, 해당 유저의 계정 존재 여부 및 프로필 완료 상태를 판단
 * 신규 계정 생성 또는 기존 계정 조회 후, 프로필이 미완료된 경우 accountId를 반환 (SocialLoginResult.incomplete)
 * 프로필이 완료된 경우, JWT 액세스/리프레시 토큰을 발급 (SocialLoginResult.active)
 *
 * 프로필 입력 요청을 받아 프로필을 등록하고 계정 상태를 ACTIVE로 변경 후 JWT 토큰을 발급
 *
 * 이 서비스는 OAuth2 인증 흐름의 최종 단계에서 호출되며, 소셜 인증 서버와 직접 통신하지는 않음
 */
public interface UserAuthService {
    /**
     * 소셜 로그인 처리
     * 1) 소셜 ID로 계정 조회 또는 신규 생성
     * 2) ACTIVE 상태이면 JWT 토큰 발급
     * 2-1) JWT 토큰 발급 (Access Token 생성)
     * 2-2) JWT 토큰 발급 (Refresh Token 생성)
     * 2-3) Refresh Token Redis에 저장
     * 3) WAITING_PROFILE_COMPLETION 상태이면 프로필 등록 유도
     *
     * @param socialId                      "provider|소셜유저ID" 형태의 통합 소셜 식별자
     * @return SocialLoginResult            프로필 미완료이면 accountId 포함, ACTIVE 회원이면 access/refresh 토큰 포함
     * @throws BusinessException            PROVIDER_NOT_SUPPORTED
     * @throws InfrastructureException      DB_RETRIEVE_FAILURE
     * @throws InfrastructureException      DB_SAVE_FAILURE
     * @throws InfrastructureException      REFRESH_TOKEN_SAVE_FAILURE
     */
    SocialLoginResult socialLogin(String provider, String socialId);

    /**
     * 프로필 등록 완료 처리 후 JWT 토큰 발급
     * 1) accountId 유효성 및 상태 확인
     * 2)-1 USER SERVICE에 요청할 DTO 생성 (INTERNAL API)
     * 2)-2 USER SERVICE에 요청 (INTERNAL API) → user_profile(userDB)에 저장
     * 3) auth_account에 userId, status 업데이트
     * 4) JWT 토큰 발급 (Access Token 생성)
     * 5) JWT 토큰 발급 (Refresh Token 생성)
     * 6) Refresh Token Redis에 저장
     * 7) 최종 응답 DTO 반환
     *
     * @param request                       유저 프로필 + FCM 토큰 등록 완료 요청 DTO (accountId, 추가 정보)
     * @return JwtResponse                  발급된 access/refresh 토큰 + expiresIn (만료 시간)
     * @throws BusinessException            ACCOUNT_NOT_FOUND
     * @throws BusinessException            PROFILE_ALREADY_COMPLETED
     * @throws InfrastructureException      DB_RETRIEVE_FAILURE
     * @throws InfrastructureException      DB_SAVE_FAILURE
     * @throws InfrastructureException      REFRESH_TOKEN_SAVE_FAILURE
     */
    JwtResponse completeUserProfile(UserProfileCompletionRequest request);
}
