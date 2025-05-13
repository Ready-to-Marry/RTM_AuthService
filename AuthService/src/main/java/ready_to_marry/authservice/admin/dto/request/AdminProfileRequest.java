package ready_to_marry.authservice.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * 관리자 프로필 저장 INTERNAL API 요청시 보낼 DTO
 *
 * 관리자 계정 생성 시 클라이언트로부터 전달받을 회원가입 정보 중 관리자 프로필 저장에 필요한 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfileRequest {
    // 계정 고유 ID: auth_account의 account_id
    private UUID accountId;

    // 관리자 이름
    private String name;

    // 관리자 소속 부서명
    private String department;

    // 관리자 연락처: 맨 앞에 + 가 0~1회 올 수 있고 그 뒤에는 숫자나 하이픈만 조합
    private String phone;
}
