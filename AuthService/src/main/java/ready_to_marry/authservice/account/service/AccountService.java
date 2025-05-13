package ready_to_marry.authservice.account.service;

import ready_to_marry.authservice.account.entity.AuthAccount;

import java.util.Optional;

/**
 * AuthAccount 엔티티에 대한 생성/조회/업데이트/삭제 기능 제공
 */
public interface AccountService {
    /**
     * loginId로 계정 조회
     */
    Optional<AuthAccount> findByLoginId(String loginId);

    /**
     * 신규 계정 저장
     */
    AuthAccount save(AuthAccount account);
}
