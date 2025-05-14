package ready_to_marry.authservice.token.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisVerificationTokenRepository implements VerificationTokenRepository {
    private static final String KEY_PREFIX = "verify_token:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String token, UUID accountId, Duration ttl) {
        String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, accountId.toString(), ttl);
    }

    @Override
    public Optional<UUID> find(String token) {
        String key = KEY_PREFIX + token;
        String v = redisTemplate.opsForValue().get(key);
        return (v != null) ? Optional.of(UUID.fromString(v)) : Optional.empty();
    }

    @Override
    public void delete(String token) {
        String key = KEY_PREFIX + token;
        redisTemplate.delete(key);
    }
}