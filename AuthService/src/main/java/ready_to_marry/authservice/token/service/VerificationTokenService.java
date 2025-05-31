package ready_to_marry.authservice.token.service;

import java.util.Optional;
import java.util.UUID;

/**
 * 이메일 인증 Token 저장·조회·삭제 기능 제공
 */
public interface VerificationTokenService {
    /**
     * 인증 토큰을 저장
     *
     * @param token     인증 토큰 문자열
     * @param accountId 계정 고유 ID
     */
    void save(String token, UUID accountId);

    /**
     * 토큰으로부터 연관된 accountId 조회
     *
     * @param token 인증 토큰 문자열
     * @return accountId (없으면 Optional.empty)
     */
    Optional<UUID> findAccountId(String token);

    /**
     * 인증 토큰 삭제
     *
     * @param token 인증 토큰 문자열
     */
    void delete(String token);
}
