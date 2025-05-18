package ready_to_marry.authservice.account.service;

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
    @Transactional
    public AuthAccount save(AuthAccount account) {
        return authAccountRepository.save(account);
    }

    @Override
    @Transactional
    public void updatePartnerId(UUID accountId, Long partnerId) {
        authAccountRepository.findById(accountId).ifPresent(a -> {
            a.setPartnerId(partnerId);
            authAccountRepository.save(a);
        });
    }

    @Override
    @Transactional
    public void updateStatus(UUID accountId, AccountStatus status) {
        authAccountRepository.findById(accountId).ifPresent(a -> {
            a.setStatus(status);
            authAccountRepository.save(a);
        });
    }

    @Override
    @Transactional
    public void deleteById(UUID accountId) {
        authAccountRepository.deleteById(accountId);
    }
}
