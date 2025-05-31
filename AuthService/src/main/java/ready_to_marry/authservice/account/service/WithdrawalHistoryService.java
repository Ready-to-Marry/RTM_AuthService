package ready_to_marry.authservice.account.service;

import ready_to_marry.authservice.account.entity.WithdrawalHistory;

public interface WithdrawalHistoryService {
    /**
     * 탈퇴·거부 이력 기록
     *
     * @param history 저장할 WithdrawalHistory 엔티티
     */
    void save(WithdrawalHistory history);
}
