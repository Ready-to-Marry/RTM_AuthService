package ready_to_marry.authservice.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ready_to_marry.authservice.admin.dto.request.PartnerRejectionRequest;
import ready_to_marry.authservice.admin.dto.response.PartnerPendingResponse;
import ready_to_marry.authservice.admin.service.PartnerApprovalService;
import ready_to_marry.authservice.common.dto.request.PagingRequest;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.dto.response.Meta;

import java.util.List;
import java.util.UUID;

/**
 * 파트너 회원가입 승인·거부·조회를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/auth/admins/partners")
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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectPartner(@PathVariable UUID accountId, @Valid @RequestBody PartnerRejectionRequest request) {
        partnerApprovalService.rejectPartner(accountId, request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(0)
                .message("Partner rejected and deleted")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 관리자 승인 대기 중인 파트너 목록 페이징 조회
     *
     * @param pagingRequest 페이징 요청 정보 (page, size)
     * @return 성공 시 code=0, data=관리자 승인 대기 중인 파트너 내역 페이징 결과 정보
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PartnerPendingResponse>>> getPendingPartners(@Valid @ModelAttribute PagingRequest pagingRequest) {
        Page<PartnerPendingResponse> page = partnerApprovalService.getPendingPartners(pagingRequest);

        ApiResponse<List<PartnerPendingResponse>> response = ApiResponse.<List<PartnerPendingResponse>>builder()
                .code(0)
                .message("Pending partners retrieved successfully")
                .data(page.getContent())
                .meta(Meta.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }
}
