package ready_to_marry.authservice.social.service;

import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.social.dto.SocialLoginResult;

/**
 * 소셜 로그인(OAuth2 PKCE + state 기반 인증) 전 과정을 백엔드에서 전담 처리하는 서비스 인터페이스
 *
 * 지원되는 provider(Kakao 등)에 따라 인증 URL 생성 및 callback 처리 로직을 추상화
 */
public interface OAuth2Service {
    /**
     * 소셜 로그인 인증 요청 URL을 생성
     * 1) CSRF + PKCE 방지를 위한 state 및 code_verifier 생성
     * 2) Redis에 (state→verifier) 저장
     * 3) Provider 로 SocialOAuthClient 조회
     * 4) Provider별 구현체에서 인가 요청 URL을 구성하여 반환
     *
     * @param provider                  소셜 로그인 제공자 (예: "kakao")
     * @return String                   소셜 인증 서버로 이동하기 위한 인가 요청 URL
     * @throws BusinessException        PROVIDER_NOT_SUPPORTED
     * @throws InfrastructureException  PKCE_CHALLENGE_GENERATION_FAILURE
     * @throws InfrastructureException  OAUTH_STATE_SAVE_FAILURE
     */
    String buildAuthUrl(String provider);

    /**
     * 소셜 인증 서버로부터 받은 인가 코드와 state를 처리하여 로그인 또는 2단계 가입 흐름을 수행
     * 1) Redis에서 state로 PKCE verifier(code_verifier) 조회 → → CSRF/state 검증 겸 만료 검증
     * 2) Provider 로 SocialOAuthClient 조회
     * 3) 인가 코드 → 액세스 토큰 교환
     * 4) 액세스 토큰 → 사용자 정보 조회
     * 5) socialId 생성 (provider|id)
     * 6) 2단계 가입 흐름 수행
     *
     * @param provider                  소셜 로그인 제공자 (예: "kakao")
     * @param code                      소셜 인증 서버가 전달한 인가 코드
     * @param state                     요청 시 전달된 CSRF 방지용 state 값
     * @return SocialLoginResult        로그인 결과 (JWT 토큰 발급 or 프로필 입력 요청)
     * @throws BusinessException        PROVIDER_NOT_SUPPORTED
     * @throws BusinessException        INVALID_OAUTH2_STATE
     * @throws InfrastructureException  OAUTH_STATE_RETRIEVE_REMOVE_FAILURE
     * @throws InfrastructureException  OAUTH_TOKEN_EXCHANGE_FAILURE
     * @throws InfrastructureException  OAUTH_USERINFO_FAILURE
     * @throws InfrastructureException  DB_RETRIEVE_FAILURE
     * @throws InfrastructureException  DB_SAVE_FAILURE
     * @throws InfrastructureException  REFRESH_TOKEN_SAVE_FAILURE
     */
    SocialLoginResult handleCallback(String provider, String code, String state);
}