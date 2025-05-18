package ready_to_marry.authservice.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 관리자 파트너 승인 거절 요청 DTO
 *
 * 관리자가 파트너 승인 거절 시 클라이언트로부터 전달받을 승인 거절 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerRejectionRequest {
    // 거부 사유
    @NotBlank
    @Size(max=100)
    private String reason;
}
