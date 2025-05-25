package ready_to_marry.authservice.social.client;

import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.social.dto.external.OAuth2Token;
import ready_to_marry.authservice.social.dto.external.SocialUserInfo;

/**
 * OAuth2 소셜 로그인 클라이언트 인터페이스
 *
 * 각 소셜 제공자의 OAuth2 인증 과정을 처리
 * 구현 클래스는 소셜 제공자별로 개별 HTTP 요청·응답 구조를 처리
 */
public interface SocialOAuthClient {
    /**
     * Provider별 인가 요청 URL 생성
     * 1) PROVIDER 설정 정보 조회
     * 2) 인가 요청 URL을 구성하여 반환
     *
     * @param state                     CSRF 방지를 위한 state
     * @param codeChallenge             PKCE code_challenge (PKCE 미지원 프로바이더는 사용안함)
     * @return String                   인가 요청 URL
     * @throws BusinessException        PROVIDER_NOT_SUPPORTED
     */
    String getAuthorizationUrl(String state, String codeChallenge);

    /**
     * 인가 코드 → 액세스 토큰 교환
     * 1) PROVIDER 설정 정보 조회
     * 2) HTTP 요청 데이터 생성
     * 3) 소셜 인증서버에 액세스 토큰 요청 (HTTP POST)
     * 4) 소셜 인증서버로부터 액세스 토큰 응답 수신
     * 5) 응답 데이터를 OAuth2Token 객체로 역직렬화
     * 6) OAuth2Token 객체 반환
     *
     * @param code                      소셜 인증서버로부터 전달받은 인가 코드(Authorization Code)
     * @param verifier                  PKCE code_verifier or state (state-only 프로바이더)
     * @return OAuth2Token              액세스 토큰 및 리프레시 토큰 정보가 담긴 OAuth2Token(DTO)
     * @throws BusinessException        PROVIDER_NOT_SUPPORTED
     */
    OAuth2Token getToken(String code, String verifier);

    /**
     * 액세스 토큰 → 사용자 정보 조회
     * 1) PROVIDER 설정 정보 조회
     * 2) HTTP 요청 데이터 생성 (Authorization 헤더에 Bearer 토큰 설정)
     * 3) 소셜 인증서버에 사용자 정보 요청 (HTTP GET)
     * 4) 소셜 인증서버로부터 사용자 정보 응답 수신
     * 5) 응답 데이터를 SocialUserInfo 객체로 역직렬화
     * 6) SocialUserInfo 객체 반환
     *
     * @param accessToken               소셜 인증서버가 발급한 유효한 액세스 토큰
     * @return SocialUserInfo           사용자의 소셜 고유 ID를 담고 있는 SocialUserInfo(DTO)
     * @throws BusinessException        PROVIDER_NOT_SUPPORTED
     */
    SocialUserInfo getUserInfo(String accessToken);
}
