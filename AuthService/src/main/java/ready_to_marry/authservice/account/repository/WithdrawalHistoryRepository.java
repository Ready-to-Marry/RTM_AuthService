package ready_to_marry.authservice.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ready_to_marry.authservice.account.entity.WithdrawalHistory;

import java.util.UUID;

/**
 * WithdrawalHistory CRUD 및 조회용 레포지토리
 */
@Repository
public interface WithdrawalHistoryRepository extends JpaRepository<WithdrawalHistory, Long> {

}
