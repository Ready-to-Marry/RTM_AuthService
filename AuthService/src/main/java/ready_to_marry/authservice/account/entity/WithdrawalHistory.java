package ready_to_marry.authservice.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ready_to_marry.authservice.common.enums.AdminRole;
import ready_to_marry.authservice.common.enums.AuthMethod;
import ready_to_marry.authservice.common.enums.DeletionType;
import ready_to_marry.authservice.common.enums.Role;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * auth_db.withdrawal_history 테이블 매핑 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "withdrawal_history")
public class WithdrawalHistory {
    // PK (자동 생성)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdrawal_history_id", updatable = false, nullable = false)
    private Long id;

    // 고유 계정 식별자 (auth_account의 account_id)
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    // 로그인 및 회원가입 방식 (auth_account의 auth_method)
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method", length = 20, nullable = false)
    private AuthMethod authMethod;

    // 로그인 및 회원가입 시 고유 ID (auth_account의 login_id)
    @Column(name = "login_id", length = 255, nullable = false)
    private String loginId;

    // 역할 (auth_account의 role)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    // 어드민 역할 (auth_account의 admin_role)
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_role", length = 20)
    private AdminRole adminRole;

    // user_service 연동 ID (auth_account의 user_id)
    @Column(name = "user_id")
    private Long userId;

    // partner_service 연동 ID (auth_account의 partner_id)
    @Column(name = "partner_id")
    private Long partnerId;

    // USER, ADMIN, PARTNER의 보관 필수인 최소 프로필 정보
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_snapshot", columnDefinition = "jsonb", nullable = false)
    private String profileSnapshot;

    // 탈퇴 사유
    @Column(name = "reason", length = 100)
    private String reason;

    // 탈퇴 주체 타입 (SELF / ADMIN / SYSTEM)
    @Enumerated(EnumType.STRING)
    @Column(name = "deleted_by", length = 20, nullable = false)
    private DeletionType deletedBy;

    // 가입시각(auth_account의 created_at)
    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    // 탈퇴 시각
    @CreationTimestamp
    @Column(name = "withdrawn_at", updatable = false, nullable = false)
    private OffsetDateTime withdrawnAt;
}
