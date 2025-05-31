package ready_to_marry.authservice.social.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ready_to_marry.authservice.social.config.OAuthProviderProperties;
import ready_to_marry.authservice.social.repository.OAuthStateRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthStateServiceImpl implements OAuthStateService {
    private final OAuthStateRepository oAuthStateRepository;
    private final OAuthProviderProperties oAuthProviderProperties;

    @Override
    // 저장 중 DataAccessException 발생 시, 최대 3회까지 재시도(backoff 100ms)한 뒤 예외를 전파
    @Retryable(
            include = DataAccessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void saveVerifier(String state, String verifier) {
        oAuthStateRepository.save(state, verifier, oAuthProviderProperties.getStateTtl());
    }

    @Override
    // 조회 또는 삭제 중 DataAccessException 발생 시, 최대 3회까지 재시도(backoff 100ms)한 뒤 예외를 전파
    @Retryable(
            include = DataAccessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public Optional<String> retrieveAndRemoveVerifier(String state) {
        Optional<String> verifier = oAuthStateRepository.find(state);
        verifier.ifPresent(v -> oAuthStateRepository.delete(state));
        return verifier;
    }
}
