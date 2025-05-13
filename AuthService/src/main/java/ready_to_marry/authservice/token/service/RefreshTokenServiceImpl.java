package ready_to_marry.authservice.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ready_to_marry.authservice.common.jwt.JwtProperties;
import ready_to_marry.authservice.token.repository.RefreshTokenRepository;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    // redis 저장을 2회 재시도(backoff 100ms) 후에도 실패하면 DataAccessException을 던짐
    @Retryable(
            include = DataAccessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void save(UUID accountId, String token) {
        refreshTokenRepository.save(accountId, token, Duration.ofSeconds(jwtProperties.getRefreshExpiry()));
    }
}
