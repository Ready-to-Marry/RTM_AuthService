package ready_to_marry.authservice.social.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 인증 서버(OAuth2 Provider)로부터 토큰 응답을 받을 때 사용하는 DTO
 *
 * 인가 코드(code)와 code_verifier를 이용해 액세스 토큰을 요청하면,
 * 소셜 서버가 반환하는 access_token, refresh_token, expires_in 값을 매핑
 */
@Getter
@NoArgsConstructor
public class OAuth2Token {
    // 액세스 토큰 (사용자 정보 조회 시 사용)
    @JsonProperty("access_token")
    private String accessToken;

    // 리프레시 토큰 (토큰 만료 시 재발급에 사용)
    @JsonProperty("refresh_token")
    private String refreshToken;

    // 액세스 토큰 만료 시간 (초 단위)
    @JsonProperty("expires_in")
    private long expiresIn;
}