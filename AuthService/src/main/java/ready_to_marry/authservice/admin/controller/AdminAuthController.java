package ready_to_marry.authservice.admin.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ready_to_marry.authservice.admin.dto.request.AdminLoginRequest;
import ready_to_marry.authservice.admin.dto.request.AdminSignupRequest;
import ready_to_marry.authservice.admin.service.AdminAuthService;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.dto.response.JwtResponse;

/**
 * 관리자 인증·회원가입을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/auth/admins")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    /**
     * 관리자 계정 사전 등록 (ROLE_SUPER_ADMIN 권한 필요)
     *
     * @param request 관리자 가입 요청 정보
     * @return 성공 시 code=0, data=null
     */
    @PostMapping("/signup")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> registerAdmin(@Valid @RequestBody AdminSignupRequest request) {
        // 패스워드 암호화 및 auth_account(authDB), admin_profile(adminDB) 저장
        adminAuthService.registerAdmin(request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(0)
                .message("Admin account created")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

}
