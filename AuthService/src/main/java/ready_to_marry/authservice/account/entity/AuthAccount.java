package ready_to_marry.authservice.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import ready_to_marry.authservice.common.enums.AccountStatus;
import ready_to_marry.authservice.common.enums.AdminRole;
import ready_to_marry.authservice.common.enums.AuthMethod;
import ready_to_marry.authservice.common.enums.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * auth_db.auth_account 테이블 매핑 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auth_account", uniqueConstraints = @UniqueConstraint(columnNames = "login_id"))
public class AuthAccount {
    // PK (UUID 자동 생성)
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "account_id", updatable = false, nullable = false)
    private UUID accountId;

    // 로그인 및 회원가입 방식 (USER: KAKAO, NAVER, GOOGLE / PARTNER: EMAIL / ADMIN: INTERNAL)
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method", length = 20, nullable = false)
    private AuthMethod authMethod;

    // 로그인 및 회원가입 시 고유 ID (USER: 소셜 식별자  / PARTNER: EMAIL / ADMIN:  관리자 로그인 아이디)
    @Column(name = "login_id", length = 255, nullable = false, unique = true)
    private String loginId;

    // 비밀번호 (USER: NULL / PARTNER, ADMIN: 암호화해서 저장)
    @Column(name = "password", length = 100)
    private String password;

    // 역할 (USER / PARTNER / ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    // 어드민 역할 (SUPER_ADMIN  / CONTENT_ADMIN  / MONITOR_ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_role", length = 20)
    private AdminRole adminRole;

    // user_service 연동 ID (user_db.user_profile의 user_id)
    @Column(name = "user_id")
    private Long userId;

    // partner_service 연동 ID (partner_db.partner_profile의 partner_id)
    @Column(name = "partner_id")
    private Long partnerId;

    // admin_service 연동 ID (auth_account의 admin_id)
    @Column(name = "admin_id")
    private Long adminId;

    // 계정 상태 (ACTIVE / WAITING_PROFILE_COMPLETION / WITHDRAWN / WAITING_EMAIL_VERIFICATION / PENDING_ADMIN_APPROVAL)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private AccountStatus status;

    // 계정 생성 시각
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}
