package ready_to_marry.authservice.common.dto.response;

import lombok.*;

/**
 * JWT 토큰 응답 DTO
 *
 * - accessToken  : 리소스 서버 접근을 위한 짧은 수명(Short-lived) 액세스 토큰
 * - refreshToken : 액세스 토큰 갱신에 사용하는 장기 수명(Long-lived) 리프레시 토큰
 * - expiresIn    : 액세스 토큰 만료까지 남은 시간(초 단위)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    // 리소스 접근을 위한 액세스 토큰 (짧은 수명)
    private String accessToken;

    // 액세스 토큰 갱신용 리프레시 토큰 (장기 수명)
    private String refreshToken;

    // 액세스 토큰 만료까지 남은 시간 (초 단위)
    private long expiresIn;
}
