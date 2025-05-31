package ready_to_marry.authservice.social.util;

import lombok.extern.slf4j.Slf4j;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.common.util.MaskingUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class OAuth2Utils {
    private static final SecureRandom random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private OAuth2Utils() {
        // 유틸 클래스이므로 인스턴스 생성 방지
    }

    /**
     * PKCE code_verifier 생성 (RFC7636 권장: 43~128자, URL-safe Base64)
     */
    public static String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }

    /**
     * SHA-256 해시 후 URL-safe Base64 인코딩하여 code_challenge 생성
     *
     * @throws InfrastructureException PKCE_CHALLENGE_GENERATION_FAILURE
     */
    public static String toCodeChallenge(String verifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return encoder.encodeToString(digest);
        } catch (Exception ex) {
            log.error("{}: identifierType=verifier, identifierValue={}", ErrorCode.PKCE_CHALLENGE_GENERATION_FAILURE.getMessage(), MaskingUtil.maskVerifier(verifier), ex);
            throw new InfrastructureException(ErrorCode.PKCE_CHALLENGE_GENERATION_FAILURE, ex);
        }
    }

    /**
     * CSRF 방지를 위한 state 토큰 생성
     */
    public static String generateState() {
        return UUID.randomUUID().toString();
    }
}
