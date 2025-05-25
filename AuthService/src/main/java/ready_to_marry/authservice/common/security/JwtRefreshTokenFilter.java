package ready_to_marry.authservice.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;
import ready_to_marry.authservice.common.jwt.JwtTokenProvider;

import java.io.IOException;

/**
 * Authorization 헤더의 refresh 토큰 유효성(서명+만료)만 검사
 *
 * 리프레시 엔드포인트 전용 필터
 * JWT Signature + 만료(exp) 검증만 수행
 * Redis 등 저장소 검증은 Service 레이어에서 처리
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
            throw new InsufficientAuthenticationException("Refresh token missing");
        }

        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        // 단순 검증만 통과하면 비즈니스 로직으로 넘김
        chain.doFilter(request, response);
    }
}
