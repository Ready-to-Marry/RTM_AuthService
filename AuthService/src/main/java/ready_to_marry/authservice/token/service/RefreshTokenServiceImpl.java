package ready_to_marry.authservice.token.service;

import lombok.RequiredArgsConstructor;
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
    public void save(UUID accountId, String token) {
        refreshTokenRepository.save(accountId, token, Duration.ofSeconds(jwtProperties.getRefreshExpiry()));
    }
}
