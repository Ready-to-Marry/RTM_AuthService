package ready_to_marry.authservice.common.dto.response;

import lombok.*;

/**
 * 검증 오류 발생 시 필드별 상세 메시지
 *
 * - field  : 오류가 발생한 필드 이름
 * - reason : 해당 필드의 오류 사유
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {

    // 오류가 발생한 필드 이름
    private String field;

    // 해당 필드의 오류 사유
    private String reason;
}
