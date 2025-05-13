package ready_to_marry.authservice.token.service;

import java.util.UUID;

/**
 * Refresh Token 저장·검증·삭제 기능 제공
 */
public interface RefreshTokenService {
    /**
     * 계정ID에 해당하는 리프레시 토큰을 저장
     *
     * @param accountId 계정 고유 ID
     * @param token     발급된 refresh 토큰 문자열
     */
    void save(UUID accountId, String token);
}
