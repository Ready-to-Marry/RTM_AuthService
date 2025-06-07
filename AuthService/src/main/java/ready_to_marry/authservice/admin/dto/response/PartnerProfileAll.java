package ready_to_marry.authservice.admin.dto.response;

import lombok.*;

/**
 * 관리자 승인 대기 중인 파트너 계정 목록 페이징 조회 시점에 조회할 파트너 프로필 DTO
 *
 * 리자 승인 대기 중인 파트너 계정 목록 페이징 조회 시 INTERNAL API 로부터 전달받을 프로필 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerProfileAll {
    // 회사 대표자 이름
    private String name;

    // 회사 이름
    private String companyName;

    // 회사 주소
    private String address;

    // 담당자 연락처
    private String phone;

    // 회사 연락처
    private String companyNum;

    // 사업자번호
    private String businessNum;
}
