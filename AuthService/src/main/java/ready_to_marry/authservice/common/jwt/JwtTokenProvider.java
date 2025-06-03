package ready_to_marry.authservice.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 컴포넌트
 *
 * - Auth Service 내에서는 주로 Refresh Token 검증 및 신규 토큰 발급에 사용
 * - Gateway가 Access Token을 검증·파싱하여 헤더로 전달하므로, 비즈니스 로직에서는 헤더 값만 활용
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties props;
    private Key key;

    @PostConstruct
    public void init() {
        // Base64 디코딩 후 HMAC-SHA256 키 생성
        byte[] secretBytes = Decoders.BASE64.decode(props.getSecretKey());
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    /**
     * Access Token 생성
     *
     * @param subject JWT 토큰 subject (accountId)
     * @param claims  사용자별 추가 클레임 (userId/partnerId/adminRole)
     * @return 서명된 JWT 토큰 문자열
     */
    public String generateAccessToken(String subject, JwtClaims claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getAccessExpiry() * 1000);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("role", claims.getRole());

        if ("USER".equals(claims.getRole()) && claims.getUserId() != null) {
            builder.claim("userId", claims.getUserId());
        }
        if ("PARTNER".equals(claims.getRole()) && claims.getPartnerId() != null) {
            builder.claim("partnerId", claims.getPartnerId());
        }
        if ("ADMIN".equals(claims.getRole()) && claims.getAdminRole() != null) {
            builder.claim("adminId", claims.getAdminId());
            builder.claim("adminRole", claims.getAdminRole());
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성 (subject만 담음)
     *
     * @param subject JWT 토큰 subject (accountId)
     * @return 서명된 JWT 토큰 문자열
     */
    public String generateRefreshToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getRefreshExpiry() * 1000);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 유효성 검증 (리프레시 플로우에서 사용)
     *
     * @param token 검증할 JWT 토큰 문자열
     * @return 유효하면 true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * JWT 파싱 후 Claims 반환
     *
     * @param token 파싱할 JWT 토큰 문자열
     * @return Jws<Claims> 객체
     */
    public Jws<Claims> parseClaims(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
    }

    /**
     * subject(accountId) 추출
     *
     * @param token JWT 토큰 문자열
     * @return subject 값
     */
    public String getSubject(String token) {
        return parseClaims(token).getBody().getSubject();
    }
}