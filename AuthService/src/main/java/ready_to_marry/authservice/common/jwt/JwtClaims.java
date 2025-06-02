package ready_to_marry.authservice.common.jwt;

import lombok.*;

/**
 * JWT에 담을 사용자 role별 커스텀 클레임 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    // "USER", "PARTNER", "ADMIN"
    private String role;

    // 일반 유저(USER)일 때만 사용
    private Long userId;

    // 파트너(PARTNER)일 때만 사용
    private Long partnerId;

    // 관리자(ADMIN)일 때만 사용
    private Long adminId;
    private String adminRole;
}
