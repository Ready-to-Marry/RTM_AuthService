package ready_to_marry.authservice.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import ready_to_marry.authservice.common.jwt.JwtTokenProvider;

import java.io.IOException;

/**
 * Authorization 헤더의 refresh 토큰 유효성(서명+만료)만 검사
 *
 * refresh Token 흐름에서만 동작
 */
@RequiredArgsConstructor
public class JwtRefreshTokenFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // refresh Token 흐름 이외의 요청은 스킵
        // FIXME: refresh Token 흐름인 요청 주소 변경 확인
        return !"/auth/token/refresh".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Refresh token missing");
            return;
        }

        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token");
            return;
        }

        // 다음 필터로 토큰 파싱+컨텍스트 설정 위임
        chain.doFilter(request, response);
    }
}
