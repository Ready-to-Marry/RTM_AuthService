package ready_to_marry.authservice.partner.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * application.properties의 auth.partner.* 설정을 바인딩
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth.partner")
public class AuthPartnerProperties {
    // Partner 이메일 인증(verify) 경로
    private String verifyPath;
}
