package ready_to_marry.authservice.common.util;

public final class MaskingUtil {
    private MaskingUtil() {
        // 유틸 클래스이므로 인스턴스 생성 방지
    }

    /**
     * 이메일 형태의 loginId를 부분 마스킹
     * 예: "loginId.123@example.com" → "lo****@example.com"
     */
    public static String maskEmailLoginId(String loginId) {
        if (loginId == null || !loginId.contains("@")) {
            return loginId;
        }
        String[] parts = loginId.split("@", 2);
        String local = parts[0];
        String domain = parts[1];

        int visible = Math.max(1, Math.min(2, local.length() / 3));
        String prefix = local.substring(0, visible);
        String masked = "*".repeat(local.length() - visible);

        return prefix + masked + "@" + domain;
    }

    /**
     * 일반 형태의 loginId를 부분 마스킹
     * 예: "loginId123" → "l********3"
     */
    public static String maskGenericLoginId(String loginId) {
        if (loginId == null || loginId.length() <= 2) {
            return loginId;
        }
        // 첫 글자와 마지막 글자는 남기고, 중간만 '*' 처리
        String prefix = loginId.substring(0, 1);
        String suffix = loginId.substring(loginId.length() - 1);
        String masked = "*".repeat(loginId.length() - 2);

        return prefix + masked + suffix;
    }

    /**
     * 긴 토큰 문자열을 부분 마스킹
     * 예: "abcdef1234567890" → "abcd********7890"
     */
    public static String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return token;
        }
        int unmasked = 4; // 앞뒤로 보여줄 길이
        String prefix = token.substring(0, unmasked);
        String suffix = token.substring(token.length() - unmasked);
        String maskedMiddle = "*".repeat(token.length() - 2 * unmasked);
        return prefix + maskedMiddle + suffix;
    }

    /**
     * verifier 문자열 부분 마스킹
     */
    public static String maskVerifier(String verifier) {
        if (verifier == null || verifier.length() < 8) {
            return "****";
        }
        String prefix = verifier.substring(0, 4);
        String suffix = verifier.substring(verifier.length() - 4);
        return prefix + "..." + suffix;
    }

    /**
     * state 문자열 부분 마스킹
     */
    public static String maskState(String state) {
        if (state == null || state.length() < 8) {
            return "****";
        }
        String prefix = state.substring(0, 4);
        String suffix = state.substring(state.length() - 4);
        return prefix + "..." + suffix;
    }

    /**
     * code 문자열 부분 마스킹
     */
    public static String maskCode(String code) {
        if (code == null || code.length() < 4) {
            return "****";
        }
        String prefix = code.substring(0, 2);
        String suffix = code.substring(code.length() - 2);
        return prefix + "..." + suffix;
    }

    /**
     * 소셜 로그인 형태의 loginId를 부분 마스킹
     * 예: "kakao|123456789" → "kakao|12*****89"
     */
    public static String maskSocialLoginId(String socialLoginId) {
        if (socialLoginId == null || !socialLoginId.contains("|")) {
            return "****";
        }

        String[] parts = socialLoginId.split("\\|", 2);
        String provider = parts[0];
        String userId = parts[1];

        String maskedUserId;
        if (userId == null || userId.length() <= 4) {
            maskedUserId = "*".repeat(userId.length());
        } else {
            String prefix = userId.substring(0, 2);
            String suffix = userId.substring(userId.length() - 2);
            String masked = "*".repeat(userId.length() - 4);
            maskedUserId = prefix + masked + suffix;
        }

        return provider + "|" + maskedUserId;
    }
}
