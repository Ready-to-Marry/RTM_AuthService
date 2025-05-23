package ready_to_marry.authservice.social.service;

import java.util.Optional;

/**
 * CSRF 방지 및 PKCE 관리를 위한 서비스 인터페이스
 */
public interface OAuthStateService {
    /**
     * CSRF 방지를 위한 state 값과 PKCE 인증용 verifier(code_verifier)를 저장
     *
     * @param state     CSRF 방지를 위한 state 문자열
     * @param verifier  PKCE 코드 검증을 위한 code_verifier 문자열
     */
    void saveVerifier(String state, String verifier);

    /**
     * 저장된 verifier(code_verifier)를 state 키로 조회하고, 조회 후 해당 키를 삭제
     *
     * @param state               CSRF 방지를 위한 state 문자열
     * @return Optional<String>   조회된 code_verifier 문자열 (없으면 Optional.empty)
     */
    Optional<String> retrieveAndRemoveVerifier(String state);
}