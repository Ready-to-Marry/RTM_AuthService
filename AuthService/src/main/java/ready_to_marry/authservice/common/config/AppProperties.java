package ready_to_marry.authservice.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * application.properties의 app.* 설정을 바인딩
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    // 서비스 기본 URL
    private String urlBase;
}