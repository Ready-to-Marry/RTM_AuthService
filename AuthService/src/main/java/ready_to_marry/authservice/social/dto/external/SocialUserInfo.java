package ready_to_marry.authservice.social.dto.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

/**
 * 소셜 인증 서버로부터 사용자 정보를 조회할 때 사용하는 DTO
 *
 * 액세스 토큰을 이용해 사용자 정보를 요청하면,
 * 응답으로 제공되는 사용자 고유 ID를 매핑
 */
@Getter
public class SocialUserInfo {
    private final String id;

    /**
     * @JsonCreator를 붙인 생성자에서 JsonNode를 통째로 받아와서,
     * 각 소셜 로그인 응답 형태에 따라 id를 직접 추출
     */
    @JsonCreator
    public SocialUserInfo(JsonNode rootNode) {
        String extractedId = null;

        // 1) 네이버 응답: 최상위가 아니라 "response" 객체 내부에 "id"가 있음
        if (rootNode.has("response") && rootNode.get("response").has("id")) {
            extractedId = rootNode.get("response").get("id").asText();

            // 2) 카카오(또는 네이버 일부 버전) 응답: 최상위에 바로 "id" 필드가 있음
        } else if (rootNode.has("id")) {
            extractedId = rootNode.get("id").asText();

            // 3) 구글 응답: 최상위에 "sub" 필드가 있음
        } else if (rootNode.has("sub")) {
            extractedId = rootNode.get("sub").asText();
        }

        this.id = extractedId;
    }
}