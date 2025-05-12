package ready_to_marry.authservice.token.repository;

import java.time.Duration;
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
}
