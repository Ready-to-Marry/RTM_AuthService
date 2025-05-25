package ready_to_marry.authservice.token.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.token.service.TokenService;

/**
 * 리프레시 토큰을 사용한 JWT 토큰 재발급을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class RefreshTokenController {
    private final TokenService tokenService;

    /**
     * 유저, 파트너, 어드민의 리프레시
     *
     * @param authorizationHeader Authorization 헤더 (Bearer <refreshToken>)
     * @return 성공 시 code=0, data=새로 발급된 JWT 토큰 정보
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        // "Bearer " 접두어 제거하여 순수 토큰 문자열만 추출
        String refreshToken = authorizationHeader.replaceFirst("^Bearer ", "");

        // 리프레시 토큰 검증 후 JWT 토큰 재생성 및 Redis에 저장
        JwtResponse tokens = tokenService.refresh(refreshToken);

        ApiResponse<JwtResponse> response = ApiResponse.<JwtResponse>builder()
                .code(0)
                .message("Refresh successful")
                .data(tokens)
                .build();

        return ResponseEntity.ok(response);
    }
}
