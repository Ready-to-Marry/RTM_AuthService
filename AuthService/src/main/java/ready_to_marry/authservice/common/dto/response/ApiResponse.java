package ready_to_marry.authservice.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * 공통 응답 래퍼
 *
 * @param <T> 실제 응답 데이터 타입
 * - code    : 0 = 정상, 그 외 = 비즈니스 오류 코드
 * - message : 사람이 읽기 좋은 설명 메시지
 * - data    : 항상 포함되며, 단건 또는 배열 형태의 실제 응답 데이터
 * - meta    : 리스트 조회 시 페이징 정보(선택). null이면 JSON에서 제외
 * - errors  : 검증 오류 발생 시 필드별 상세 메시지(선택). 빈 리스트 또는 null이면 JSON에서 제외
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    // 0 = 정상, 그 외 = 비즈니스 오류 코드
    private int code;

    // 처리 결과 메시지
    private String message;

    // 응답 데이터 (단건 또는 배열), 항상 포함됨
    private T data;

    // 리스트 조회 시 페이징 정보, null 시 JSON에서 제외
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Meta meta;

    // 검증 오류 발생 시 필드별 상세 메시지, 빈 리스트 또는 null 시 JSON에서 제외
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ErrorDetail> errors;
}
