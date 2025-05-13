package ready_to_marry.authservice.common.exception;

import lombok.Getter;

/**
 * 인프라(시스템) 오류 예외를 나타내는 런타임 예외
 */
@Getter
public class InfrastructureException extends RuntimeException {
    private final int code;

    /**
     * @param errorCode 사전 정의된 에러 코드와 메시지를 담고 있는 {@link ErrorCode} enum
     * @param cause     이 예외를 발생시킨 근본 원인 예외
     */
    public InfrastructureException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }
}
