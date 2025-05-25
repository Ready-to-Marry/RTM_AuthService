package ready_to_marry.authservice.token.repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Refresh Token 저장소 추상화 인터페이스
 */
public interface RefreshTokenRepository {
    /**
     * 지정된 계정 ID에 대해 리프레시 토큰을 저장하고 TTL을 설정
     *
     * @param accountId 토큰을 저장할 계정의 고유 ID
     * @param token     발급된 리프레시 토큰 문자열
     * @param ttl       토큰 만료까지의 기간(Duration)
     */
    void save(UUID accountId, String token, Duration ttl);

    /**
     * 지정된 계정 ID에 저장된 리프레시 토큰을 조회
     *
     * @param accountId 조회할 refresh 토큰 계정의 고유 ID
     * @return 저장된 refresh 토큰 문자열 (없으면 Optional.empty())
     */
    Optional<String> find(UUID accountId);

    /**
     * 지정된 계정 ID에 대한 리프레시 토큰을 삭제
     *
     * @param accountId 삭제할 refresh 토큰 계정의 고유 ID
     */
    void delete(UUID accountId);
}
