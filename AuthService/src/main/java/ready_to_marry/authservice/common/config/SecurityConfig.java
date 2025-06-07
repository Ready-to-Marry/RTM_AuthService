package ready_to_marry.authservice.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import ready_to_marry.authservice.common.jwt.JwtTokenProvider;
import ready_to_marry.authservice.common.security.JwtRefreshTokenFilter;
import ready_to_marry.authservice.common.security.PreAuthHeaderFilter;
import ready_to_marry.authservice.common.security.RestAccessDeniedHandler;
import ready_to_marry.authservice.common.security.RestAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final RestAuthenticationEntryPoint authEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 특성상 CSRF 불필요
                .csrf(csrf -> csrf.disable())

                // 세션을 사용하지 않음
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // REST API 이므로 기본 로그인 폼, HTTP Basic 모두 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 인증·인가 예외를 JSON 구조로 응답
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 인증/인가 설정
                .authorizeHttpRequests(authz -> authz
                        // 스프링 에러 핸들러 경로는 인증 없이 허용
                        .requestMatchers("/error").permitAll()

                        // Actuator 주요 엔드포인트(prometheus, health, info, metrics)를 인증 없이 허용
                        .requestMatchers(
                                "/actuator/prometheus",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/metrics"
                        ).permitAll()

                        // 인증 없이 접근 가능한 Auth 서비스 엔드포인트
                        .requestMatchers(
                                // FIXME: permitAll인 요청 주소 변경 확인
                                "/auth/oauth2/authorize/**",
                                "/auth/oauth2/callback/**",
                                "/auth/users/profile/complete",
                                "/auth/partners/login",
                                "/auth/partners/signup",
                                "/auth/partners/verify",
                                "/auth/partners/verify/result",
                                "/auth/admins/login",
                                "/auth/token/refresh"
                        ).permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 1) refresh 토큰 검증 필터
                .addFilterAfter(
                        jwtRefreshTokenFilter(),
                        ExceptionTranslationFilter.class
                )

                // 2) 컨텍스트 세팅 필터
                .addFilterAfter(
                        preAuthHeaderFilter(),
                        JwtRefreshTokenFilter.class
                );

        return http.build();
    }

    /**
     * refresh 토큰 유효성(서명+만료)만 검사하는
     * 리프레시 엔드포인트 전용 필터
     */
    @Bean
    public JwtRefreshTokenFilter jwtRefreshTokenFilter() {
        return new JwtRefreshTokenFilter(jwtTokenProvider);
    }

    /**
     * Gateway가 삽입한 X-헤더만 신뢰해서 SecurityContext에 인증정보를 세팅하는 필터
     */
    @Bean
    public PreAuthHeaderFilter preAuthHeaderFilter() {
        return new PreAuthHeaderFilter();
    }

    /**
     * 로컬 로그인(Controller 내 수동 검증)용
     * PasswordEncoder 빈
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}