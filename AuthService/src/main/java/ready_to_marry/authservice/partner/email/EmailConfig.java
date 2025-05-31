package ready_to_marry.authservice.partner.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class EmailConfig {
    @Bean
    public ClassLoaderTemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(ClassLoaderTemplateResolver resolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(resolver);
        return engine;
    }
}
