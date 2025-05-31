package ready_to_marry.authservice.token.repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * 이메일 인증 Token 저장소 추상화 인터페이스
 */
public interface VerificationTokenRepository {
    /**
     * 지정된 계정 ID에 대해 인증 토큰을 저장하고 TTL을 설정
     *
     * @param token     발급된 인증 토큰 문자열
     * @param accountId 토큰을 저장할 계정의 고유 ID
     * @param ttl       토큰 만료까지의 기간(Duration)
     */
    void save(String token, UUID accountId, Duration ttl);

    /**
     * 토큰으로 저장된 accountId 조회
     *
     * @param token 인증 토큰 문자열
     * @return accountId (없으면 Optional.empty)
     */
    Optional<UUID> find(String token);

    /**
     * 토큰 삭제
     *
     * @param token 인증 토큰 문자열
     */
    void delete(String token);
}
