package ready_to_marry.authservice.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import ready_to_marry.authservice.common.enums.AdminRole;

/**
 * 관리자 계정 사전 등록 요청 DTO
 *
 * 관리자 계정 생성 시 클라이언트로부터 전달받을 회원가입 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSignupRequest {
    // 로그인 아이디: 4자 이상, 50자 이하 문자열
    @NotBlank
    @Size(min=4, max=50)
    private String loginId;

    // 로그인 비밀번호: 8자 이상, 100자 이하 문자열
    @NotBlank
    @Size(min=8, max=100)
    private String password;

    // 관리자 이름
    @NotBlank
    @Size(max=20)
    private String name;

    // 관리자 소속 부서명
    @NotBlank
    @Size(max=20)
    private String department;

    // 관리자 연락처: 맨 앞에 + 가 0~1회 올 수 있고 그 뒤에는 숫자나 하이픈만 조합
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\-]{1,20}$")
    private String phone;

    // 관리자 역할 코드: SUPER_ADMIN, CONTENT_ADMIN, MONITOR_ADMIN
    @NotNull
    private AdminRole adminRole;
}
