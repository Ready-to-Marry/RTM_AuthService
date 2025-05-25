package ready_to_marry.authservice.token.service;

import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.InfrastructureException;

/**
 * 리프레시 기능 관련 비즈니스 로직을 제공하는 서비스 인터페이스
 */
public interface TokenService {
    /**
     * 클라이언트가 가진 refresh token 으로 새로운 토큰 쌍을 발급
     * 1) 리프레시 토큰 서명·만료 검증은 JwtRefreshTokenFilter가 이미 담당
     * 2) subject(accountId) 추출 및 형식 검증
     * 3) 저장소 검증: 저장된 토큰 조회 및 비교
     * 4) 계정 정보 조회
     * 5) 새 Access Token 생성
     * 6) 새 Refresh Token 생성
     * 7) 새 Refresh Token Redis에 저장 (기존 덮어쓰기)
     * 8) 응답 DTO
     *
     * @param token 리프레시 토큰
     * @return 새로 발급된 JWT 토큰 정보
     * @throws BusinessException        REFRESH_TOKEN_INVALID
     * @throws BusinessException        REFRESH_TOKEN_NOT_FOUND
     * @throws BusinessException        REFRESH_TOKEN_MISMATCH
     * @throws BusinessException        ACCOUNT_NOT_FOUND
     * @throws InfrastructureException  DB_RETRIEVE_FAILURE
     * @throws InfrastructureException  REFRESH_TOKEN_RETRIEVE_FAILURE
     * @throws InfrastructureException  REFRESH_TOKEN_SAVE_FAILURE
     */
    JwtResponse refresh(String token);
}