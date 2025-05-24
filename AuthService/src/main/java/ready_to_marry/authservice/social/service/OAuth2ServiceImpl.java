package ready_to_marry.authservice.social.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.common.util.MaskingUtil;
import ready_to_marry.authservice.social.client.SocialOAuthClient;
import ready_to_marry.authservice.social.dto.SocialLoginResult;
import ready_to_marry.authservice.social.dto.external.OAuth2Token;
import ready_to_marry.authservice.social.dto.external.SocialUserInfo;
import ready_to_marry.authservice.social.util.OAuth2Utils;
import ready_to_marry.authservice.user.service.UserAuthService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {
    private final Map<String, SocialOAuthClient> oauthClients;
    private final UserAuthService userAuthService;
    private final OAuthStateService oAuthStateService;

    @Override
    public String buildAuthUrl(String provider) {
        // 1) CSRF + PKCE 방지를 위한 state 및 code_verifier 생성
        String state    = OAuth2Utils.generateState();
        String verifier = OAuth2Utils.generateCodeVerifier();
        String challenge= OAuth2Utils.toCodeChallenge(verifier);

        // 2) Redis에 (state→verifier) 저장
        try {
            oAuthStateService.saveVerifier(state, verifier);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=state, identifierValue={}", ErrorCode.OAUTH_STATE_SAVE_FAILURE.getMessage(), MaskingUtil.maskState(state), ex);
            throw new InfrastructureException(ErrorCode.OAUTH_STATE_SAVE_FAILURE, ex);
        }

        // 3) Provider 로 SocialOAuthClient 조회
        SocialOAuthClient client = oauthClients.get(provider);

        if (client == null) {
            log.error("{}: identifierType=provider, identifierValue={}", ErrorCode.PROVIDER_NOT_SUPPORTED.getMessage(), provider);
            throw new BusinessException(ErrorCode.PROVIDER_NOT_SUPPORTED);
        }

        // 4) Provider별 구현체에서 인가 요청 URL을 구성하여 반환
        return client.getAuthorizationUrl(state, challenge);
    }

    @Override
    @Transactional
    public SocialLoginResult handleCallback(String provider, String code, String state) {
        // 1) Redis에서 state로 PKCE verifier(code_verifier) 조회 → → CSRF/state 검증 겸 만료 검증
        String verifier;
        try {
            verifier = oAuthStateService.retrieveAndRemoveVerifier(state)
                    .orElseThrow(() -> {
                        log.error("{}: identifierType=state, identifierValue={}", ErrorCode.INVALID_OAUTH2_STATE.getMessage(), MaskingUtil.maskState(state));
                        return new BusinessException(ErrorCode.INVALID_OAUTH2_STATE);
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=state, identifierValue={}", ErrorCode.OAUTH_STATE_RETRIEVE_REMOVE_FAILURE.getMessage(), MaskingUtil.maskState(state), ex);
            throw new InfrastructureException(ErrorCode.OAUTH_STATE_RETRIEVE_REMOVE_FAILURE, ex);
        }

        // 2) Provider 로 SocialOAuthClient 조회
        SocialOAuthClient client = oauthClients.get(provider);

        if (client == null) {
            log.error("{}: identifierType=provider, identifierValue={}", ErrorCode.PROVIDER_NOT_SUPPORTED.getMessage(), provider);
            throw new BusinessException(ErrorCode.PROVIDER_NOT_SUPPORTED);
        }

        // 3) 인가 코드 → 액세스 토큰 교환
        OAuth2Token token;
        try {
            token = client.getToken(code, verifier);
        } catch (WebClientRequestException | WebClientResponseException ex) {
            log.error("{}: identifierType=code, identifierValue={}", ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILURE.getMessage(), MaskingUtil.maskCode(code), ex);
            throw new InfrastructureException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILURE, ex);
        }

        // 4) 액세스 토큰 → 사용자 정보 조회
        SocialUserInfo userInfo;
        try {
            userInfo = client.getUserInfo(token.getAccessToken());
        } catch (WebClientRequestException | WebClientResponseException ex) {
            log.error("{}: identifierType=accessToken, identifierValue={}", ErrorCode.OAUTH_USERINFO_FAILURE.getMessage(), MaskingUtil.maskToken(token.getAccessToken()), ex);
            throw new InfrastructureException(ErrorCode.OAUTH_USERINFO_FAILURE, ex);
        }

        // 5) socialId 생성 (provider|id)
        String socialId = provider + "|" + userInfo.getId();

        // 6) 2단계 가입 흐름 수행
        return userAuthService.socialLogin(provider, socialId);
    }
}
