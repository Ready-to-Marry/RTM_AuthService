package ready_to_marry.authservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 1xxx: 비즈니스 오류
    DUPLICATE_LOGIN_ID(1301, "Duplicate login ID"),
    INVALID_CREDENTIALS(1302, "Invalid login ID or password"),
    INVALID_VERIFICATION_TOKEN(1303, "Invalid verification token"),
    EMAIL_NOT_VERIFIED(1304, "Email not verified"),
    PENDING_ADMIN_APPROVAL(1305, "Pending admin approval"),
    PENDING_ADMIN_APPROVAL_REQUIRED(1306, "Pending admin approval status required"),

    // 2xxx: 인프라(시스템) 오류
    DB_SAVE_FAILURE(2301, "System error occurred while saving data to the database"),
    DB_DELETE_FAILURE(2302, "System error occurred while deleting data from the database"),
    DB_RETRIEVE_FAILURE(2303, "System error occurred while retrieving data from the database"),
    REFRESH_TOKEN_SAVE_FAILURE(2304, "System error occurred while saving refresh token to redis"),
    VERIFICATION_TOKEN_SAVE_FAILURE(2305, "System error occurred while saving verification token to redis"),
    VERIFICATION_TOKEN_DELETE_FAILURE(2306, "System error occurred while deleting verification token from redis"),
    VERIFICATION_TOKEN_RETRIEVE_FAILURE(2307, "System error occurred while retrieving verification token from redis"),
    EMAIL_SEND_FAILURE(2308, "System error occurred while sending verification email"),
    JSON_SERIALIZATION_FAILURE(2309, "System error occurred while serializing object to JSON");

    private final int code;
    private final String message;
}
