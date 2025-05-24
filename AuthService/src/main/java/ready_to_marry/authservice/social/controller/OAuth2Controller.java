package ready_to_marry.authservice.social.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.social.dto.SocialLoginResult;
import ready_to_marry.authservice.social.dto.response.SocialAuthResponse;
import ready_to_marry.authservice.social.service.OAuth2Service;

/**
 * 소셜 로그인(PKCE + state) 시작 및 콜백을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {
    private final OAuth2Service oauth2Service;

    /**
     * 소셜 인증 요청 URL 생성
     *
     * @param provider 소셜 로그인 제공자 (예: kakao)
     * @return 성공 시 code=0, data=authorization URL
     */
    @GetMapping("/authorize/{provider}")
    public ResponseEntity<ApiResponse<String>> authorize(@PathVariable String provider) {
        // 소셜 로그인 인증 요청 URL을 생성
        String authUrl = oauth2Service.buildAuthUrl(provider);

        ApiResponse<String> body = ApiResponse.<String>builder()
                .code(0)
                .message("Social authentication URL generated")
                .data(authUrl)
                .build();

        return ResponseEntity.ok(body);
    }

    /**
     * 소셜 인증 서버 콜백 처리
     *
     * @param provider 소셜 로그인 제공자 (예: kakao)
     * @param code     발급된 인가 코드
     * @param state    CSRF 방지용 state
     * @return 성공 시 code=0, data=SocialAuthResponse
     * - status = INCOMPLETE: data.accountId에 값 세팅 (프론트가 프로필 완성 화면으로 이동)
     * - status = SUCCESS: data.tokens에 JWT 토큰 정보 세팅 (JWT 토큰 발급 완료)
     */
    @GetMapping("/callback/{provider}")
    public ResponseEntity<ApiResponse<SocialAuthResponse>> callback(@PathVariable String provider, @RequestParam("code")  String code, @RequestParam("state") String state) {
        // 1) 소셜 인증 서버로부터 받은 인가 코드와 state를 처리하여 소셜 로그인 로그인 또는 2단계 가입 흐름을 수행
        SocialLoginResult result = oauth2Service.handleCallback(provider, code, state);

        SocialAuthResponse resp;
        String message;
        if (result.isProfileIncomplete()) {
            // 2) 프로필 미완료: accountId만 담아서 응답
            resp = SocialAuthResponse.builder()
                    .status(SocialAuthResponse.Status.INCOMPLETE)
                    .accountId(result.getAccountId())
                    .build();
            message = "User profile not completed";
        } else {
            // 3) 프로필 완료: JwtResponse로 JWT 토큰 담아서 응답
            JwtResponse tokens = JwtResponse.builder()
                    .accessToken(result.getAccessToken())
                    .refreshToken(result.getRefreshToken())
                    .expiresIn(result.getExpiresIn())
                    .build();
            resp = SocialAuthResponse.builder()
                    .status(SocialAuthResponse.Status.SUCCESS)
                    .tokens(tokens)
                    .build();
            message = "User login successful";
        }

        ApiResponse<SocialAuthResponse> body = ApiResponse.<SocialAuthResponse>builder()
                .code(0)
                .message(message)
                .data(resp)
                .build();

        return ResponseEntity.ok(body);
    }
}
