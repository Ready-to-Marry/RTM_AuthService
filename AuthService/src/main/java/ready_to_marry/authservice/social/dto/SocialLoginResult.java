package ready_to_marry.authservice.social.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 소셜 로그인 처리 결과를 나타내는 내부 상태 DTO
 *
 * 소셜 로그인 시, 계정이 이미 존재하고 프로필이 완료된 경우 → JWT 토큰 발급 (ACTIVE 상태)
 * 계정이 처음 생성되었거나 프로필이 미완료된 경우 → 프로필 입력 요청 (WAITING_PROFILE_COMPLETION 상태)
 * JWT 토큰 응답을 내려줄지, 프로필 입력 요청을 보낼지 판단하는 기준으로 사용
 */
@Getter
@RequiredArgsConstructor
public class SocialLoginResult {
    // 프로필이 미완료된 상태인지 여부 (true인 경우 프로필 입력이 필요한 상태)
    private final boolean profileIncomplete;

    //가입된 계정의 식별자 (profileIncomplete=true인 경우 사용)
    private final UUID accountId;

    // 발급된 액세스 토큰 (profileIncomplete=false인 경우 사용)
    private final String accessToken;

    // 발급된 리프레시 토큰 (profileIncomplete=false인 경우 사용)
    private final String refreshToken;

    // 액세스 토큰 만료 시간 (초 단위, profileIncomplete=false인 경우 사용)
    private final long expiresIn;

    /**
     * 프로필 미완료 상태 결과 생성
     *
     * @param accountId             가입 혹은 조회된 계정 ID
     * @return SocialLoginResult    프로필 미완료 상태의 로그인 결과 객체
     */
    public static SocialLoginResult incomplete(UUID accountId) {
        return new SocialLoginResult(true, accountId, null, null, 0);
    }

    /**
     * 프로필 완료 상태 결과 생성
     *
     * @param accessToken           발급된 액세스 토큰
     * @param refreshToken          발급된 리프레시 토큰
     * @param expiresIn             액세스 토큰 만료 시간(초)
     * @return SocialLoginResult    프로필이 완료된 사용자에 대한 로그인 결과 객체
     */
    public static SocialLoginResult active(String accessToken, String refreshToken, long expiresIn) {
        return new SocialLoginResult(false, null, accessToken, refreshToken, expiresIn);
    }
}
