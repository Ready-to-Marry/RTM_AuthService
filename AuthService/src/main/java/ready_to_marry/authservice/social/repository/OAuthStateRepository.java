package ready_to_marry.authservice.social.repository;

import java.time.Duration;
import java.util.Optional;

/**
 * OAuth2 state 와 PKCE verifier 저장소 추상화 인터페이스
 */
public interface OAuthStateRepository {
    /**
     * state → verifier 매핑을 TTL 과 함께 저장
     *
     * @param state     CSRF 방지를 위한 state 문자열
     * @param verifier  PKCE 코드 검증을 위한 code_verifier 문자열
     * @param ttl       키 만료까지의 기간(Duration)
     */
    void save(String state, String verifier, Duration ttl);

    /**
     * state 로 저장된 verifier 조회
     *
     * @param state                CSRF 방지를 위한 state 문자열
     * @return Optional<String>    verifier (없으면 Optional.empty)
     */
    Optional<String> find(String state);

    /**
     * state 키 삭제
     *
     * @param state     CSRF 방지를 위한 state 문자열
     */
    void delete(String state);
}
