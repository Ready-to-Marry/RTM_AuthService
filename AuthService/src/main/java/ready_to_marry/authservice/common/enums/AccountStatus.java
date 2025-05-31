package ready_to_marry.authservice.common.enums;

public enum AccountStatus {
    // user, partner, admin
    ACTIVE,                      // 로그인 가능 상태

    // user
    WAITING_PROFILE_COMPLETION,  // 소셜 로그인 후 프로필 입력 대기
    WITHDRAWN,                   // 탈퇴 처리된 상태(복구 가능)

    // partner
    WAITING_EMAIL_VERIFICATION,  // 가입 폼 제출 완료 (이메일 인증 대기)
    PENDING_ADMIN_APPROVAL       // 이메일 인증 완료 (관리자 승인 대기)
}