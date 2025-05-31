package ready_to_marry.authservice.partner.dto.request;

import lombok.*;

/**
 * 파트너 프로필 저장 INTERNAL API 요청시 보낼 DTO
 *
 * 파트너 회원가입 요청 시 클라이언트로부터 전달받을 회원가입 정보 중 파트너 프로필 저장에 필요한 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerProfileRequest {
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
