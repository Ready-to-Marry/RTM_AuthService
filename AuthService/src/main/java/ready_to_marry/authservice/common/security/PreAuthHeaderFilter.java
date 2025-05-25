package ready_to_marry.authservice.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gateway가 삽입한 X-헤더만 신뢰해서 SecurityContext에 인증정보를 세팅
 *
 * Access Token 흐름: JWT 파싱 없이 Gateway가 삽입한 X-Account-Id, X-Role, X-User-Id/X-Partner-Id/X-Admin-Id/X-Admin-Role 헤더만 사용
 * Refresh Token 흐름: JwtRefreshTokenFilter가 먼저 처리하므로 스킵
 */
@Slf4j
@RequiredArgsConstructor
public class PreAuthHeaderFilter extends OncePerRequestFilter {
    // 스킵할 URL 패턴
    private static final List<String> EXCLUDE_URLS = List.of(
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
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDE_URLS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String accountId  = request.getHeader("X-Account-Id");
        String userId    = request.getHeader("X-User-Id");
        String partnerId = request.getHeader("X-Partner-Id");
        String adminId   = request.getHeader("X-Admin-Id");
        String role      = request.getHeader("X-Role");
        String adminRole = request.getHeader("X-Admin-Role");

        if (accountId != null && role != null) {
            // 권한 리스트 생성
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            if ("ADMIN".equals(role) && adminRole != null) {
                // 예: adminRole="SUPER_ADMIN" → "ROLE_SUPER_ADMIN"
                authorities.add(new SimpleGrantedAuthority("ROLE_" + adminRole));
            }

            PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken(accountId, null, authorities);

            // 내부 식별자 정보는 details로 설정
            Map<String,String> details = new HashMap<>();
            if (userId != null) {
                details.put("userId", userId);
            }
            if (partnerId != null) {
                details.put("partnerId", partnerId);
            }
            if (adminId != null) {
                details.put("adminId", adminId);
            }
            auth.setDetails(details);

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("PreAuthHeaderFilter set authentication: accountId={} role={}", accountId, role);
        }

        chain.doFilter(request, response);
    }
}
