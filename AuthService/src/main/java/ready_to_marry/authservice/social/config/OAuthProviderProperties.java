package ready_to_marry.authservice.social.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * 소셜 로그인 관련 설정 바인딩 클래스
 *
 * - auth.oauth.* 프로퍼티를 매핑
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth.oauth")
public class OAuthProviderProperties {
    // 각 provider별 설정
    private Map<String, Provider> providers;

    // PKCE state → verifier 저장 TTL
    private Duration stateTtl;

    @Getter
    @Setter
    public static class Provider {
        private String clientId;
        private String clientSecret;
        private String authUri;
        private String tokenUri;
        private String userInfoUri;
        private String redirectUri;
    }

    public Provider getProvider(String name) {
        return providers.get(name);
    }
}