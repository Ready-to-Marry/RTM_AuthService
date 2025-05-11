package ready_to_marry.authservice.admin.service;

import lombok.RequiredArgsConstructor;
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
import ready_to_marry.authservice.common.jwt.JwtClaims;
import ready_to_marry.authservice.common.jwt.JwtTokenProvider;

/**
 * 관리자 계정 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

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
                    throw new BusinessException(1001, "Duplicate login ID");
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
        AuthAccount savedAccount = accountService.save(account);

        // 5) ADMIN SERVICE에 요청할 DTO 생성 (INTERNAL API)
        AdminProfileRequest internalRequest = AdminProfileRequest.builder()
                .accountId(savedAccount.getAccountId())
                .name(request.getName())
                .department(request.getDepartment())
                .phone(request.getPhone())
                .build();

        // 5) ADMIN SERVICE에 요청 (INTERNAL API) -> admin_profile(adminDB)에 저장
        // TODO: INTERNAL API 호출 로직 추가
        // TODO: 각 DB의 테이블에서 저장 실패시 로직 추가

    }

}
