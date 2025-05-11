package ready_to_marry.authservice.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import ready_to_marry.authservice.common.jwt.JwtTokenProvider;

import java.io.IOException;
import java.util.Map;

/**
 * access/refresh 흐름 모두 처리하여 SecurityContext에 인증정보를 세팅
 *
 * Gateway가 삽입한 X-Role, X-User-Id/X-Partner-Id/X-Admin-Role 헤더와
 * 토큰에 담긴 accountId로 SecurityContext에 인증정보를 세팅
 */
@Slf4j
@RequiredArgsConstructor
public class PreAuthHeaderFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // access Token 흐름: Gateway가 검증·헤더 셋팅
        // refresh Token 흐름: JwtRefreshTokenFilter에서 이미 검증
        String accountId  = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accountId = jwtTokenProvider.getSubject(authHeader.substring(7));
        }

        if (accountId != null) {
            String role      = request.getHeader("X-Role");
            String userId    = request.getHeader("X-User-Id");
            String partnerId = request.getHeader("X-Partner-Id");
            String adminRole = request.getHeader("X-Admin-Role");

            PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken(accountId, null, null);
            auth.setDetails(Map.of(
                    "role",      role,
                    "userId",    userId,
                    "partnerId", partnerId,
                    "adminRole", adminRole
            ));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("PreAuthHeaderFilter set authentication: accountId={}, role={}", accountId, role);
        }

        chain.doFilter(request, response);
    }
}
