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
}
