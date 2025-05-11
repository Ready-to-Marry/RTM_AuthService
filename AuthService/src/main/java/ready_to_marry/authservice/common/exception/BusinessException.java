package ready_to_marry.authservice.common.exception;

import lombok.Getter;

/**
 * 비즈니스 예외를 나타내는 런타임 예외
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    /**
     * @param code    비즈니스 오류 코드 (0이 아닌 값)
     * @param message 사용자에게 보여줄 설명 메시지
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
