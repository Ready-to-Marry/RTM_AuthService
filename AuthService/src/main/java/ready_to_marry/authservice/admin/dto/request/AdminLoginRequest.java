package ready_to_marry.authservice.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 관리자 로그인 요청 DTO
 *
 * 관리자 로그인 시 클라이언트로부터 전달받을 로그인 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginRequest {
    // 로그인 아이디: 4자 이상, 50자 이하 문자열
    @NotBlank
    @Size(min=4, max=50)
    private String loginId;

    // 로그인 비밀번호: 8자 이상, 100자 이하 문자열
    @NotBlank
    @Size(min=8, max=100)
    private String password;
}
