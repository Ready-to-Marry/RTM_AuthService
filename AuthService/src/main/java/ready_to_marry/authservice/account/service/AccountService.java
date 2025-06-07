package ready_to_marry.authservice.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.common.enums.AccountStatus;
import ready_to_marry.authservice.common.enums.Role;

import java.util.Optional;
import java.util.UUID;

/**
 * AuthAccount 엔티티에 대한 생성/조회/업데이트/삭제 기능 제공
 */
public interface AccountService {
    /**
     * loginId로 계정 조회
     *
     * @param loginId 로그인 식별자
     * @return AuthAccount가 담긴 Optional
     */
    Optional<AuthAccount> findByLoginId(String loginId);

    /**
     * accountId로 계정 조회
     *
     * @param accountId 계정의 UUID
     * @return AuthAccount가 담긴 Optional
     */
    Optional<AuthAccount> findById(UUID accountId);

    /**
     * 신규 계정 생성
     *
     * @param account 저장할 AuthAccount 엔티티
     * @return 저장된 AuthAccount
     */
    AuthAccount save(AuthAccount account);

    /**
     * 계정에 연결된 adminId 업데이트
     *
     * @param accountId 계정의 UUID
     * @param adminId 어드민 서비스의 식별자
     */
    void updateAdminId(UUID accountId, Long adminId);

    /**
     * 계정에 연결된 partnerId 업데이트
     *
     * @param accountId 계정의 UUID
     * @param partnerId 파트너 서비스의 식별자
     */
    void updatePartnerId(UUID accountId, Long partnerId);

    /**
     * 계정에 연결된 userId, status 업데이트
     *
     * @param accountId 계정의 UUID
     * @param userId 유저 서비스의 식별자
     * @param status 적용할 AccountStatus
     */
    void updateUserIdAndStatus(UUID accountId, Long userId, AccountStatus status);

    /**
     * 계정 상태 업데이트
     *
     * @param accountId 계정의 UUID
     * @param status 적용할 AccountStatus
     */
    void updateStatus(UUID accountId, AccountStatus status);

    /**
     * 지정된 계정 삭제
     *
     * @param accountId 삭제할 계정의 UUID
     */
    void deleteById(UUID accountId);

    /**
     * 지정된 역할(role) + 상태(status)를 가진 계정을 생성 시각 오름차순으로 페이징 조회
     *
     * @param role 페이징 조회할 역할
     * @param status 페이징 조회할 계정 상태
     * @param pageable 페이징 정보
     * @return 조회된 Page<AuthAccount>
     */
    Page<AuthAccount> findByRoleAndStatus(Role role, AccountStatus status, Pageable pageable);
}
