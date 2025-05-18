package ready_to_marry.authservice.partner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.partner.dto.request.PartnerSignupRequest;
import ready_to_marry.authservice.partner.service.PartnerAuthService;

/**
 * 파트너 인증·회원가입을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/auth/partners")
@RequiredArgsConstructor
public class PartnerAuthController {
    private final PartnerAuthService partnerAuthService;

    /**
     * 파트너 회원가입: 가입 폼 제출 → 이메일 인증 메일 전송
     *
     * @param request 파트너 회원가입 요청 정보
     * @return 성공 시 code=0, data=null
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> registerPartner(@Valid @RequestBody PartnerSignupRequest request) {
        // 패스워드 암호화 및 auth_account(authDB), partner_profile(partnerDB) 저장 + 인증 이메일 전송
        partnerAuthService.registerPartner(request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(0)
                .message("Partner account created + Verification email sent")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
