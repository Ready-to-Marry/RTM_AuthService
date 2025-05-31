package ready_to_marry.authservice.token.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {
    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(UUID accountId, String token, Duration ttl) {
        String key = KEY_PREFIX + accountId;
        redisTemplate.opsForValue().set(key, token, ttl);
    }

    @Override
    public Optional<String> find(UUID accountId) {
        String key = KEY_PREFIX + accountId;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void delete(UUID accountId) {
        String key = KEY_PREFIX + accountId;
        redisTemplate.delete(key);
    }
}
