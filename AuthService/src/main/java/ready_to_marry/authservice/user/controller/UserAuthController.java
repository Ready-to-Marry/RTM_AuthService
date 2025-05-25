package ready_to_marry.authservice.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.user.dto.request.UserProfileCompletionRequest;
import ready_to_marry.authservice.user.service.UserAuthService;

/**
 * 유저 프로필 등록 완료 및 JWT 토큰 발급 컨트롤러
 */
@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
public class UserAuthController {
    private final UserAuthService userAuthService;

    /**
     * 프로필 등록 완료 후 JWT 토큰 발급
     *
     * @param request accountId 및 추가 프로필 정보
     * @return 성공 시 code=0, data=발급된 JWT 토큰 정보
     */
    @PostMapping("/profile/complete")
    public ResponseEntity<ApiResponse<JwtResponse>> completeProfile(@Valid @RequestBody UserProfileCompletionRequest request) {
        // 프로필 등록 완료 후 JWT 토큰 발급
        JwtResponse tokens = userAuthService.completeUserProfile(request);

        ApiResponse<JwtResponse> body = ApiResponse.<JwtResponse>builder()
                .code(0)
                .message("User profile completed + User login successful")
                .data(tokens)
                .build();

        return ResponseEntity.ok(body);
    }
}