package ready_to_marry.authservice.partner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.partner.dto.request.PartnerLoginRequest;
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

    /**
     * 파트너 로그인
     *
     * @param request 로그인 요청 정보 (login, password)
     * @return 성공 시 code=0, data=발급된 JWT 토큰 정보
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> loginPartner(@Valid @RequestBody PartnerLoginRequest request) {
        // 로그인 -> 토큰 생성 및 Redis에 저장
        JwtResponse tokens = partnerAuthService.login(request);

        ApiResponse<JwtResponse> response = ApiResponse.<JwtResponse>builder()
                .code(0)
                .message("Partner login successful")
                .data(tokens)
                .build();

        return ResponseEntity.ok(response);
    }
}
