package ready_to_marry.authservice.partner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 파트너 회원가입 요청 DTO
 *
 * 파트너 회원가입 요청 시 클라이언트로부터 전달받을 회원가입 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerSignupRequest {
    // 로그인 아이디: 이메일 형식 문자열
    @NotBlank
    @Email
    @Size(max = 255)
    private String loginId;

    // 로그인 비밀번호: 8자 이상, 100자 이하 문자열
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    // 회사 대표자 이름
    @NotBlank
    @Size(max = 20)
    private String name;

    // 회사 이름
    @NotBlank
    @Size(max = 255)
    private String companyName;

    // 회사 주소
    @NotBlank
    @Size(max = 1000)
    private String address;

    // 담당자 연락처: 맨 앞에 + 가 0~1회 올 수 있고 그 뒤에는 숫자나 하이픈만 조합
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\-]{1,20}$")
    private String phone;

    // 회사 연락처: 맨 앞에 + 가 0~1회 올 수 있고 그 뒤에는 숫자나 하이픈만 조합
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\-]{1,20}$")
    private String companyNum;

    // 사업자번호: 10자리 숫자로 이루어진 문자열
    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$")
    private String businessNum;
}
