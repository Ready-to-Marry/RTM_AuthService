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
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
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
                        // Auth Service 인증 엔드포인트만 모두 허용
                        .requestMatchers(
                                // FIXME: 임시로 로그인 및 회원가입 엔드포인트 추가함. 실제 엔드포인트로 변경 필요
                                "/auth/users/oauth2",
                                "/auth/partners/login",
                                "/auth/partners/signup",
                                "/auth/partners/verify",
                                "/auth/partners/verify/result",
                                "/auth/admins/login"
                        ).permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 1) refresh 토큰 검증 필터
                .addFilterBefore(
                        jwtRefreshTokenFilter(),
                        AbstractPreAuthenticatedProcessingFilter.class
                )

                // 2) access/refresh 토큰 파싱(accountId) & 컨텍스트 세팅 필터
                .addFilterAfter(
                        preAuthHeaderFilter(),
                        JwtRefreshTokenFilter.class
                );

        return http.build();
    }

    /**
     * refresh 토큰 유효성(서명+만료)만 검사하는
     * refresh 흐름에서만 동작하는 필터
     */
    @Bean
    public JwtRefreshTokenFilter jwtRefreshTokenFilter() {
        return new JwtRefreshTokenFilter(jwtTokenProvider);
    }

    /**
     * access 또는 refresh 토큰을 직접 파싱(accountId)하고
     * Gateway가 덮어쓴 X-헤더를 꺼내
     * SecurityContext에 세팅하는 필터
     */
    @Bean
    public PreAuthHeaderFilter preAuthHeaderFilter() {
        return new PreAuthHeaderFilter(jwtTokenProvider);
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
