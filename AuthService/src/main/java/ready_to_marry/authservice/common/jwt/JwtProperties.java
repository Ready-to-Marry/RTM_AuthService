package ready_to_marry.authservice.common.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 설정 바인딩 클래스
 *
 * - application.properties의 jwt.secretKey, jwt.accessExpiry, jwt.refreshExpiry, jwt.verificationExpiry 프로퍼티를 매핑
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    // Base64 인코딩된 서명용 비밀키
    private String secretKey;

    // Access Token 만료 시간(초 단위)
    private long accessExpiry;

    // Refresh Token 만료 시간(초 단위)
    private long refreshExpiry;

    // 인증 Token 만료 시간(초 단위)
    private long verificationExpiry;
}
