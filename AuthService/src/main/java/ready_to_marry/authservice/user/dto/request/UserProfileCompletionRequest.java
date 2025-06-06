package ready_to_marry.authservice.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * 유저 프로필 + FCM 토큰 등록 완료 요청 DTO
 *
 * 소셜 로그인 후 추가로 받아야 할 사용자 프로필 + FCM 토큰 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileCompletionRequest {
    // 유저 계정 고유 ID
    @NotNull
    private UUID accountId;

    // 유저 실명(또는 표시명)
    @NotBlank
    @Size(max = 50)
    private String name;

    // 유저 연락처: 맨 앞에 + 가 0~1회 올 수 있고 그 뒤에는 숫자나 하이픈만 조합
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\-]{1,20}$")
    private String phone;

    // 유저 FCM 토큰(푸시 알림 허용 시에만 전달됨)
    @Size(max = 255)
    private String fcmToken;
}