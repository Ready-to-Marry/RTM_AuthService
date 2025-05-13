package ready_to_marry.authservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 1xxx: 비즈니스 오류
    DUPLICATE_LOGIN_ID(1001, "Duplicate login ID"),
    INVALID_CREDENTIALS(1002, "Invalid login ID or password"),

    // 2xxx: 인프라(시스템) 오류
    REDIS_SAVE_FAILURE(2002, "System error occurred while saving refresh token");

    private final int code;
    private final String message;
}
