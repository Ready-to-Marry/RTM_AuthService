package ready_to_marry.authservice.user.dto.request;

import lombok.*;

/**
 * 유저 프로필 등록 + FCM 토큰 등록 INTERNAL API 요청시 보낼 DTO
 *
 * 유저 소셜 로그인 후 유저 프로필 등록 완료 요청 시 클라이언트로부터 전달받을 정보 중 유저 프로필 등록 및 FCM 토큰 등록에 필요한 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequest {

    // 유저 실명(또는 표시명)
    private String name;

    // 유저 연락처
    private String phone;

    // 유저 FCM 토큰(푸시 알림 허용 시에만 전달됨)
    private String fcmToken;
}
