package ready_to_marry.authservice.token.service;

import java.util.Optional;
import java.util.UUID;

/**
 * Refresh Token 저장·조회·삭제 기능 제공
 */
public interface RefreshTokenService {
    /**
     * 계정ID에 해당하는 리프레시 토큰을 저장
     *
     * @param accountId 계정 고유 ID
     * @param token     발급된 refresh 토큰 문자열
     */
    void save(UUID accountId, String token);

    /**
     * 계정ID에 해당하는 리프레시 토큰을 조회
     *
     * @param accountId 조회할 refresh 토큰 계정의 고유 ID
     * @return 저장된 refresh 토큰 문자열 (없으면 Optional.empty())
     */
    Optional<String> findRefreshToken(UUID accountId);

    /**
     * 계정ID에 해당하는 리프레시 토큰을 삭제
     *
     * @param accountId 삭제할 refresh 토큰 계정의 고유 ID
     */
    void delete(UUID accountId);
}
