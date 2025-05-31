package ready_to_marry.authservice.common.exception;

import lombok.Getter;

/**
 * 비즈니스 예외를 나타내는 런타임 예외
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    /**
     * @param errorCode 사전 정의된 에러 코드와 메시지를 담고 있는 {@link ErrorCode} enum
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
