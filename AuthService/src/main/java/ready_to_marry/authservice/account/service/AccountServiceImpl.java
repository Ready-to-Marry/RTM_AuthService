package ready_to_marry.authservice.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.account.repository.AuthAccountRepository;

import java.util.Optional;

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
}
