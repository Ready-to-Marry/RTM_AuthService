package ready_to_marry.authservice.social.dto.external;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 인증 서버로부터 사용자 정보를 조회할 때 사용하는 DTO
 *
 * 액세스 토큰을 이용해 사용자 정보를 요청하면,
 * 응답으로 제공되는 사용자 고유 ID를 매핑
 */
@Getter
@NoArgsConstructor
public class SocialUserInfo {
    // 소셜 사용자 고유 ID
    // - Kakao/Naver: "id"
    // - Google:      "sub"
    @JsonProperty("id")
    @JsonAlias("sub")
    private String id;
}