package ready_to_marry.authservice.account.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.account.repository.AuthAccountRepository;
import ready_to_marry.authservice.common.enums.AccountStatus;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class AccountServiceImpl implements AccountService {
    private final AuthAccountRepository authAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthAccount> findByLoginId(String loginId) {
        return authAccountRepository.findByLoginId(loginId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthAccount> findById(UUID accountId) {
        return authAccountRepository.findById(accountId);
    }

    @Override
    @Transactional
    public AuthAccount save(AuthAccount account) {
        return authAccountRepository.save(account);
    }

    @Override
    @Transactional
    public void updatePartnerId(UUID accountId, Long partnerId) {
        AuthAccount account = authAccountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account(" + accountId + ") not found"));

        account.setPartnerId(partnerId);
    }

    @Override
    @Transactional
    public void updateUserIdAndStatus(UUID accountId, Long userId, AccountStatus status) {
        AuthAccount account = authAccountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account(" + accountId + ") not found"));
        account.setUserId(userId);
        account.setStatus(status);
    }

    @Override
    @Transactional
    public void updateStatus(UUID accountId, AccountStatus status) {
        AuthAccount account = authAccountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account(" + accountId + ") not found"));

        account.setStatus(status);
    }

    @Override
    @Transactional
    public void deleteById(UUID accountId) {
        authAccountRepository.deleteById(accountId);
    }
}
