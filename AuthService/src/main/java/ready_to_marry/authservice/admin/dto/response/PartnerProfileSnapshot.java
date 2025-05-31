package ready_to_marry.authservice.admin.dto.response;

import lombok.*;

/**
 * 파트너 탈퇴/승인거부 시점에 저장할 파트너 최소 프로필 스냅샷 DTO
 *
 * 파트너 탈퇴/승인거부 시 INTERNAL API 로부터 전달받을 최소 프로필 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerProfileSnapshot {
    // 회사 대표자 이름
    private String name;

    // 회사 이름
    private String companyName;

    // 담당자 연락처
    private String phone;

    // 사업자번호
    private String businessNum;
}
