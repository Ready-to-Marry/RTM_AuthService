package ready_to_marry.authservice.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ready_to_marry.authservice.admin.dto.request.PartnerRejectionRequest;
import ready_to_marry.authservice.admin.service.PartnerApprovalService;
import ready_to_marry.authservice.common.dto.response.ApiResponse;

import java.util.UUID;

/**
 * SUPER_ADMIN용 파트너 회원가입 승인·거부를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/auth/admins/partners")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminPartnerApprovalController {
    private final PartnerApprovalService partnerApprovalService;

    /**
     * 슈퍼관리자가 파트너 계정 승인 (ROLE_SUPER_ADMIN 권한 필요)
     *
     * @param accountId 승인 대상 계정 ID
     * @return 성공 시 code=0, data=null
     */
    @PostMapping("/{accountId}/approval")
    public ResponseEntity<ApiResponse<Void>> approvePartner(@PathVariable UUID accountId) {
        partnerApprovalService.approvePartner(accountId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(0)
                .message("Partner approved")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 슈퍼관리자가 파트너 계정 거부 (ROLE_SUPER_ADMIN 권한 필요)
     *
     * @param accountId 거부 대상 계정 ID
     * @param request 관리자 파트너 승인 거절 요청 정보 (reason)
     * @return 성공 시 code=0, data=null
     */
    @PostMapping("/{accountId}/rejection")
    public ResponseEntity<ApiResponse<Void>> rejectPartner(@PathVariable UUID accountId, @Valid @RequestBody PartnerRejectionRequest request) {
        partnerApprovalService.rejectPartner(accountId, request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(0)
                .message("Partner rejected and deleted")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
