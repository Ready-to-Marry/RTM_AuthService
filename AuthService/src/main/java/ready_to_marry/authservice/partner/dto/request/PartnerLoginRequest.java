package ready_to_marry.authservice.partner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 파트너 로그인 요청 DTO
 *
 * 파트너 로그인 시 클라이언트로부터 전달받을 로그인 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerLoginRequest {
    // 로그인 아이디: 이메일 형식 문자열
    @NotBlank
    @Email
    @Size(max = 255)
    private String loginId;

    // 로그인 비밀번호: 8자 이상, 100자 이하 문자열
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
}
