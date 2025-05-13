package ready_to_marry.authservice.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ready_to_marry.authservice.account.entity.AuthAccount;

import java.util.Optional;
import java.util.UUID;

/**
 * AuthAccount CRUD 및 조회용 레포지토리
 */
@Repository
public interface AuthAccountRepository extends JpaRepository<AuthAccount, UUID> {
    /**
     * 로그인 ID로 계정 조회
     *
     * @param loginId USER: 소셜 식별자, PARTNER: 이메일, ADMIN: 아이디
     * @return Optional.empty()이면 미존재
     */
    Optional<AuthAccount> findByLoginId(String loginId);
}
