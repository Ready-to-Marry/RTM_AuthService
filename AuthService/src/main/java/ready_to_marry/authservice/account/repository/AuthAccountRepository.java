package ready_to_marry.authservice.account.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.common.enums.AccountStatus;
import ready_to_marry.authservice.common.enums.Role;

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

    /**
     * 특정 역할이면서 특정 상태인 계정을 생성 시각 오름차순으로 페이징 조회
     *
     * @param role     조회할 계정의 역할 (예: PARTNER)
     * @param status   조회할 계정의 상태 (예: PENDING_ADMIN_APPROVAL)
     * @param pageable 페이징 정보
     * @return 지정된 role과 status를 만족하며 createdAt 오름차순 정렬된 페이징 결과
     */
    Page<AuthAccount> findAllByRoleAndStatusOrderByCreatedAtAsc(Role role, AccountStatus status, Pageable pageable);
}
