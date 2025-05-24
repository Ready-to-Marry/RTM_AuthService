package ready_to_marry.authservice.social.dto.response;

import lombok.*;
import ready_to_marry.authservice.common.dto.response.JwtResponse;

import java.util.UUID;

/**
 * 소셜 로그인/회원가입 후 클라이언트에 반환할 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAuthResponse {
    // 인증 흐름 상태
    public enum Status {
        // 프로필 미완료
        INCOMPLETE,

        // 인증 및 회원가입 완료 (프로필 완료)
        SUCCESS
    }

    // AUTH 흐름 상태
    private Status status;

    // status == INCOMPLETE 일 때만 세팅 (프론트가 이 accountId 로 프로필 등록 호출)
    private UUID accountId;

    // status == SUCCESS 일 때만 세팅 (JWT 토큰 정보)
    private JwtResponse tokens;
}
