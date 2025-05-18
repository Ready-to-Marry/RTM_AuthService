package ready_to_marry.authservice.partner.service;

import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.partner.dto.request.PartnerLoginRequest;
import ready_to_marry.authservice.partner.dto.request.PartnerSignupRequest;

/**
 * 파트너 계정 관련 비즈니스 로직을 제공하는 서비스 인터페이스
 */
public interface PartnerAuthService {
    /**
     * 파트너 회원가입 처리
     * 0) 같은 loginId의 만료된 대기 계정 정리
     * 1) loginId 중복 검사
     * 2) password 암호화
     * 3) auth_account 저장 (WAITING_EMAIL_VERIFICATION)
     * 4) partner_profile 저장 (내부 API 호출)
     * 5) auth_account에 partnerId 업데이트
     * 6) 이메일 verification token 발급 및 Redis 저장
     * 7) 이메일 인증 메일 전송
     *
     * @param request 파트너 회원가입 요청 DTO
     * @throws BusinessException        DUPLICATE_LOGIN_ID
     * @throws InfrastructureException  DB_RETRIEVE_FAILURE
     * @throws InfrastructureException  DB_SAVE_FAILURE
     * @throws InfrastructureException  DB_DELETE_FAILURE
     * @throws InfrastructureException  VERIFICATION_TOKEN_SAVE_FAILURE
     * @throws InfrastructureException  EMAIL_SEND_FAILURE
     */
    void registerPartner(PartnerSignupRequest request);

    /**
     * 이메일 인증 처리
     * 1) 이메일 verification token 으로 accountId 조회
     * 2) auth_account에 status 업데이트 (WAITING_EMAIL_VERIFICATION -> PENDING_ADMIN_APPROVAL)
     * 3) 이메일 verification token 삭제
     *
     * @param token 이메일 인증 토큰
     * @throws BusinessException        INVALID_VERIFICATION_TOKEN
     * @throws InfrastructureException  DB_SAVE_FAILURE
     * @throws InfrastructureException  VERIFICATION_TOKEN_RETRIEVE_FAILURE
     * @throws InfrastructureException  VERIFICATION_TOKEN_DELETE_FAILURE
     */
    void verifyEmail(String token);

    /**
     * 파트너 로그인 처리
     * 1) 계정 조회 및 승인 상태 확인
     * 2) 비밀번호 검증
     * 3) Access Token 생성
     * 4) Refresh Token 생성
     * 5) Refresh Token Redis에 저장
     * 6) 응답 DTO
     *
     * @param request 로그인 요청 DTO
     * @return 발급된 JWT 토큰 정보
     * @throws BusinessException        INVALID_CREDENTIALS
     * @throws BusinessException        EMAIL_NOT_VERIFIED
     * @throws BusinessException        PENDING_ADMIN_APPROVAL
     * @throws InfrastructureException  DB_RETRIEVE_FAILURE
     * @throws InfrastructureException  REFRESH_TOKEN_SAVE_FAILURE
     */
    JwtResponse login(PartnerLoginRequest request);
}
