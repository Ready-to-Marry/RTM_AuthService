package ready_to_marry.authservice.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.account.service.AccountService;
import ready_to_marry.authservice.admin.dto.request.AdminLoginRequest;
import ready_to_marry.authservice.admin.dto.request.AdminProfileRequest;
import ready_to_marry.authservice.admin.dto.request.AdminSignupRequest;
import ready_to_marry.authservice.common.dto.response.JwtResponse;
import ready_to_marry.authservice.common.enums.AccountStatus;
import ready_to_marry.authservice.common.enums.AdminRole;
import ready_to_marry.authservice.common.enums.AuthMethod;
import ready_to_marry.authservice.common.enums.Role;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.common.jwt.JwtClaims;
import ready_to_marry.authservice.common.jwt.JwtProperties;
import ready_to_marry.authservice.common.jwt.JwtTokenProvider;
import ready_to_marry.authservice.token.service.RefreshTokenService;

import static ready_to_marry.authservice.common.util.MaskingUtil.maskGenericLoginId;

/**
 * 관리자 계정 관련 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    /**
     * SUPER_ADMIN 권한으로 관리자 계정을 사전 등록
     *
     * @param request 관리자 가입 요청 DTO
     * @throws BusinessException 동일한 loginId가 이미 존재할 때 (code=1001)
     */
    @Transactional
    public void registerAdmin(AdminSignupRequest request) {
        // 1) loginId 중복 검사
        accountService.findByLoginId(request.getLoginId())
                .ifPresent(acc -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
                });
        // 2) 비밀번호 암호화
        String encoded = passwordEncoder.encode(request.getPassword());

        // 3) AuthAccount 엔티티 생성
        AuthAccount account = AuthAccount.builder()
                // ADMIN은 INTERNAL 방식
                .authMethod(AuthMethod.INTERNAL)
                .loginId(request.getLoginId())
                .password(encoded)
                .role(Role.ADMIN)
                .adminRole(AdminRole.valueOf(request.getAdminRole().name()))
                // 승인 불필요하므로 바로 ACTIVE
                .status(AccountStatus.ACTIVE)
                .build();

        // 4) auth_account(authDB)에 저장
        AuthAccount savedAccount;
        try {
            savedAccount = accountService.save(account);
        } catch (DataAccessException ex) {
            String maskedLoginId = maskGenericLoginId(request.getLoginId());
            log.error("{}: identifierType=loginId, identifierValue={}", ErrorCode.DB_WRITE_FAILURE.getMessage(), maskedLoginId, ex);
            throw new InfrastructureException(ErrorCode.DB_WRITE_FAILURE, ex);
        }

        // 5) ADMIN SERVICE에 요청할 DTO 생성 (INTERNAL API)
        AdminProfileRequest internalRequest = AdminProfileRequest.builder()
                .accountId(savedAccount.getAccountId())
                .name(request.getName())
                .department(request.getDepartment())
                .phone(request.getPhone())
                .build();

        // 5) ADMIN SERVICE에 요청 (INTERNAL API) -> admin_profile(adminDB)에 저장
        // TODO: INTERNAL API 호출 로직 추가
        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가

    }

    /**
     * 관리자 로그인 처리
     *
     * @param request 로그인 요청 DTO
     * @return 발급된 JWT 토큰 정보
     * @throws BusinessException 인증 실패 시(code=1002)
     */
    @Transactional
    public JwtResponse login(AdminLoginRequest request) {
        AuthAccount account = accountService.findByLoginId(request.getLoginId())
                .filter(a -> a.getAuthMethod().name().equals("INTERNAL") && a.getRole().name().equals("ADMIN"))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 1) Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                account.getAccountId().toString(),
                // role, adminRole 설정
                JwtClaims.builder()
                        .role(account.getRole().name())
                        .adminRole(account.getAdminRole().name())
                        .build()
        );

        // 2) Refresh Token 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                account.getAccountId().toString()
        );

        // 3) Refresh Token Redis에 저장
        try {
            refreshTokenService.save(account.getAccountId(), refreshToken);
        }  catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.REDIS_SAVE_FAILURE.getMessage(), account.getAccountId(), ex);
            throw new InfrastructureException(ErrorCode.REDIS_SAVE_FAILURE, ex);
        }

        // 3) 응답 DTO
        long expiresIn = jwtProperties.getAccessExpiry(); // 초 단위 만료 시간

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
