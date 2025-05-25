package ready_to_marry.authservice.social.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisOAuthStateRepository implements OAuthStateRepository {
    private static final String KEY_PREFIX = "oauth2_state:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String state, String verifier, Duration ttl) {
        String key = KEY_PREFIX + state;
        redisTemplate.opsForValue().set(key, verifier, ttl);
    }

    @Override
    public Optional<String> find(String state) {
        String key = KEY_PREFIX + state;
        String v = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(v);
    }

    @Override
    public void delete(String state) {
        String key = KEY_PREFIX + state;
        redisTemplate.delete(key);
    }
}
