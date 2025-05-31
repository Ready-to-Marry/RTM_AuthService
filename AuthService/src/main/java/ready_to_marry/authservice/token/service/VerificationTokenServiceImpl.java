package ready_to_marry.authservice.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ready_to_marry.authservice.common.jwt.JwtProperties;
import ready_to_marry.authservice.token.repository.VerificationTokenRepository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    // redis에 저장을 2회 재시도(backoff 100ms) 후에도 실패하면 DataAccessException을 던짐
    @Retryable(
            include = DataAccessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void save(String token, UUID accountId) {
        verificationTokenRepository.save(token, accountId, Duration.ofSeconds(jwtProperties.getVerificationExpiry()));
    }

    @Override
    // redis에 조회를 2회 재시도(backoff 100ms) 후에도 실패하면 DataAccessException을 던짐
    @Retryable(
            include = DataAccessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public Optional<UUID> findAccountId(String token) {
        return verificationTokenRepository.find(token);
    }

    @Override
    // redis에 삭제 2회 재시도(backoff 100ms) 후에도 실패하면 DataAccessException을 던짐
    @Retryable(
            include = DataAccessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void delete(String token) {
        verificationTokenRepository.delete(token);
    }
}
