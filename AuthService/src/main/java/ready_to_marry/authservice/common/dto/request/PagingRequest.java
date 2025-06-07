package ready_to_marry.authservice.common.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * 리스트 조회 시 페이징 요청 정보 DTO
 *
 * - page : 조회할 페이지 번호 (0부터 시작)
 * - size : 한 페이지당 조회할 데이터 개수 (최소 1)
 */
@Getter
@Setter
public class PagingRequest {

    // 조회할 페이지 번호 (0부터 시작)
    @Min(0)
    private int page = 0;

    // 한 페이지당 조회할 데이터 개수 (최소 1)
    @Min(1)
    private int size = 20;
}
