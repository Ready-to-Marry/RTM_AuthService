package ready_to_marry.authservice.token.service;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.account.service.AccountService;
import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.common.jwt.JwtClaims;
import ready_to_marry.authservice.common.jwt.JwtProperties;
import ready_to_marry.authservice.common.jwt.JwtTokenProvider;
import ready_to_marry.authservice.common.util.MaskingUtil;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final AccountService accountService;

    @Override
    @Transactional
    public JwtResponse refresh(String token) {
        // 1) 리프레시 토큰 서명·만료 검증은 JwtRefreshTokenFilter가 이미 담당

        // 2) subject(accountId) 추출 및 형식 검증
        UUID accountId;
        try {
            // subject 추출 + UUID 변환
            accountId = UUID.fromString(jwtTokenProvider.getSubject(token));
        } catch (JwtException | IllegalArgumentException ex) {
            // 서명·만료 검증은 이미 필터에서, 여기서는 subject가 없거나 UUID 형식이 아닐 때
            log.error("{}: identifierType=token, identifierValue={}", ErrorCode.REFRESH_TOKEN_INVALID.getMessage(), MaskingUtil.maskToken(token));
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3) 저장소 검증: 저장된 토큰 조회 및 비교
        String savedRefreshToken;
        try {
            savedRefreshToken = refreshTokenService.findRefreshToken(accountId)
                    .orElseThrow(() -> {
                        log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage(), accountId);
                        return new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.REFRESH_TOKEN_RETRIEVE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.REFRESH_TOKEN_RETRIEVE_FAILURE, ex);
        }

        if (!savedRefreshToken.equals(token)) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.REFRESH_TOKEN_MISMATCH.getMessage(), accountId);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 4) 계정 정보 조회
        AuthAccount account;
        try {
            account = accountService.findById(accountId)
                    .orElseThrow(() -> {
                        log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.ACCOUNT_NOT_FOUND.getMessage(), accountId);
                        return new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        // 5) 새 Access Token 생성
        JwtClaims.JwtClaimsBuilder claimsBuilder = JwtClaims.builder()
                .role(account.getRole().name());

        switch (account.getRole()) {
            case USER:
                claimsBuilder.userId(account.getUserId());
                break;
            case PARTNER:
                claimsBuilder.partnerId(account.getPartnerId());
                break;
            case ADMIN:
                claimsBuilder.adminRole(account.getAdminRole().name());
                break;
            default:
                break;
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                accountId.toString(),
                claimsBuilder.build()
        );

        // 6) 새 Refresh Token 생성
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                accountId.toString()
        );

        // 7) 새 Refresh Token Redis에 저장 (기존 덮어쓰기)
        try {
            refreshTokenService.save(accountId, newRefreshToken);
        }  catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.REFRESH_TOKEN_SAVE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.REFRESH_TOKEN_SAVE_FAILURE, ex);
        }

        // 8) 응답 DTO
        long expiresIn = jwtProperties.getAccessExpiry(); // 초 단위 만료 시간

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
