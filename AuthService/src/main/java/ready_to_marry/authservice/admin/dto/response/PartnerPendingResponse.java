package ready_to_marry.authservice.admin.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 관리자 승인 대기 중인 파트너 계정 및 프로필 정보를 한 건씩 담는 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerPendingResponse {
    // auth_account의 PK
    private UUID accountId;

    // 계정 생성 시각
    private OffsetDateTime createdAt;

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
