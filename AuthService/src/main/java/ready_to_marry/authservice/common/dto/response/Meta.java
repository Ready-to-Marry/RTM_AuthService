package ready_to_marry.authservice.common.dto.response;

import lombok.*;

/**
 * 페이징 메타 정보
 *
 * - page          : 현재 페이지 번호 (0부터 시작하거나 1부터 시작 여부는 API 설계에 따라)
 * - size          : 페이지당 데이터 개수
 * - totalElements : 전체 데이터 건수
 * - totalPages    : 전체 페이지 수
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meta {

    // 현재 페이지 번호
    private int page;

    // 페이지당 데이터 개수
    private int size;

    // 전체 데이터 건수
    private long totalElements;

    // 전체 페이지 수
    private int totalPages;
}
