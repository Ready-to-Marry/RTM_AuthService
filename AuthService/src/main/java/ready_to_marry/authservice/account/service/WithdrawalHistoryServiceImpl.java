package ready_to_marry.authservice.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ready_to_marry.authservice.account.entity.WithdrawalHistory;
import ready_to_marry.authservice.account.repository.WithdrawalHistoryRepository;

@Service
@RequiredArgsConstructor
public class WithdrawalHistoryServiceImpl implements WithdrawalHistoryService {
    private final WithdrawalHistoryRepository withdrawalHistoryRepository;

    @Override
    @Transactional
    public void save(WithdrawalHistory history) {
        withdrawalHistoryRepository.save(history);
    }
}
