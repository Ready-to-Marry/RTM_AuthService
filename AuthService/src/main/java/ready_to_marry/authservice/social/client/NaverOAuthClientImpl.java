package ready_to_marry.authservice.social.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.social.config.OAuthProviderProperties;
import ready_to_marry.authservice.social.dto.external.OAuth2Token;
import ready_to_marry.authservice.social.dto.external.SocialUserInfo;

@Slf4j
@Component("naver")
@RequiredArgsConstructor
public class NaverOAuthClientImpl implements SocialOAuthClient {
    private static final String PROVIDER_NAME = "naver";

    private final OAuthProviderProperties oAuthProviderProperties;
    private final WebClient webClient;

    @Override
    public String getAuthorizationUrl(String state, String codeChallenge) {
        // 1) PROVIDER 설정 정보 조회
        OAuthProviderProperties.Provider prop = oAuthProviderProperties.getProvider(PROVIDER_NAME);

        if (prop == null) {
            log.error("{}: identifierType=provider, identifierValue={}", ErrorCode.PROVIDER_NOT_SUPPORTED.getMessage(), PROVIDER_NAME);
            throw new BusinessException(ErrorCode.PROVIDER_NOT_SUPPORTED);
        }

        // 2) 인가 요청 URL을 구성하여 반환
        return UriComponentsBuilder.fromUriString(prop.getAuthUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", prop.getClientId())
                .queryParam("redirect_uri", prop.getRedirectUri())
                .queryParam("state", state)
                // PKCE 미지원: code_challenge 무시, state만 포함
                .build().toUriString();
    }

    @Override
    // WebClientRequestException 또는 5xx 응답 발생 시, 최대 3회까지 재시도(backoff 2000ms)한 뒤 예외를 전파
    @Retryable(
            include = {
                    WebClientRequestException.class,
                    WebClientResponseException.InternalServerError.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public OAuth2Token getToken(String code, String state) {
        // 1) PROVIDER 설정 정보 조회
        OAuthProviderProperties.Provider prop = oAuthProviderProperties.getProvider(PROVIDER_NAME);

        if (prop == null) {
            log.error("{}: identifierType=provider, identifierValue={}", ErrorCode.PROVIDER_NOT_SUPPORTED.getMessage(), PROVIDER_NAME);
            throw new BusinessException(ErrorCode.PROVIDER_NOT_SUPPORTED);
        }

        return webClient.post()
                // 2) HTTP 요청 데이터 생성
                // 3) 소셜 인증서버에 액세스 토큰 요청 (HTTP POST)
                .uri(prop.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", prop.getClientId())
                        .with("client_secret", prop.getClientSecret())
                        .with("redirect_uri", prop.getRedirectUri())
                        .with("code", code)
                        .with("state", state)
                )

                // 4) 소셜 인증서버로부터 액세스 토큰 응답 수신
                .retrieve()

                // 5) 응답 데이터를 OAuth2Token 객체로 역직렬화
                .bodyToMono(OAuth2Token.class)

                // 6) OAuth2Token 객체 반환
                .block();
    }

    @Override
    // WebClientRequestException 또는 5xx 응답 발생 시, 최대 3회까지 재시도(backoff 2000ms)한 뒤 예외를 전파
    @Retryable(
            include = {
                    WebClientRequestException.class,
                    WebClientResponseException.InternalServerError.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public SocialUserInfo getUserInfo(String accessToken) {
        // 1) PROVIDER 설정 정보 조회
        OAuthProviderProperties.Provider prop = oAuthProviderProperties.getProvider(PROVIDER_NAME);

        if (prop == null) {
            log.error("{}: identifierType=provider, identifierValue={}", ErrorCode.PROVIDER_NOT_SUPPORTED.getMessage(), PROVIDER_NAME);
            throw new BusinessException(ErrorCode.PROVIDER_NOT_SUPPORTED);
        }

        return webClient.get()
                // 2) HTTP 요청 데이터 생성 (Authorization 헤더에 Bearer 토큰 설정)
                // 3) 소셜 인증서버에 사용자 정보 요청 (HTTP GET)
                .uri(prop.getUserInfoUri())
                .header("Authorization","Bearer " + accessToken)

                // 4) 소셜 인증서버로부터 사용자 정보 응답 수신
                .retrieve()

                // 5) 응답 데이터를 SocialUserInfo 객체로 역직렬화
                .bodyToMono(SocialUserInfo.class)

                // 6) SocialUserInfo 객체 반환
                .block();
    }
}
