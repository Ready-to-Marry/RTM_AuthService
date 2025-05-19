package ready_to_marry.authservice.admin.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.account.entity.WithdrawalHistory;
import ready_to_marry.authservice.account.service.AccountService;
import ready_to_marry.authservice.account.service.WithdrawalHistoryService;
import ready_to_marry.authservice.admin.dto.request.PartnerRejectionRequest;
import ready_to_marry.authservice.admin.dto.response.PartnerProfileSnapshot;
import ready_to_marry.authservice.common.enums.AccountStatus;
import ready_to_marry.authservice.common.enums.DeletionType;
import ready_to_marry.authservice.common.enums.Role;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.common.util.JsonUtil;
import ready_to_marry.authservice.partner.email.EmailService;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerApprovalServiceImpl implements PartnerApprovalService{
    private final AccountService accountService;
    private final WithdrawalHistoryService withdrawalHistoryService;
    private final EmailService emailService;

    @Override
    @Transactional
    public void approvePartner(UUID accountId) {
        // 1) 계정 조회 및 상태 확인
        AuthAccount account;
        try {
            account = accountService.findById(accountId)
                    .filter(a -> a.getRole() == Role.PARTNER)
                    .orElseThrow(() -> {
                        log.error("Account not found: identifierType=accountId, identifierValue={}", accountId);
                        return new EntityNotFoundException("Account not found");
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        if (account.getStatus() != AccountStatus.PENDING_ADMIN_APPROVAL) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.PENDING_ADMIN_APPROVAL_REQUIRED.getMessage(), accountId);
            throw new BusinessException(ErrorCode.PENDING_ADMIN_APPROVAL_REQUIRED);
        }

        // 2) auth_account에 status 업데이트 (PENDING_ADMIN_APPROVAL -> ACTIVE)
        try {
            accountService.updateStatus(accountId, AccountStatus.ACTIVE);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
        }

        // 3) 계정 승인 안내 메일 전송 (비동기, 실패 무시)
        emailService.sendPartnerApproved(account.getLoginId());
    }

    @Override
    @Transactional
    public void rejectPartner(UUID accountId, PartnerRejectionRequest request) {
        // 1) 계정 조회 및 상태 확인
        AuthAccount account;
        try {
            account = accountService.findById(accountId)
                    .filter(a -> a.getRole() == Role.PARTNER)
                    .orElseThrow(() -> {
                        log.error("Account not found: identifierType=accountId, identifierValue={}", accountId);
                        return new EntityNotFoundException("Account not found");
                    });
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        if (account.getStatus() != AccountStatus.PENDING_ADMIN_APPROVAL) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.PENDING_ADMIN_APPROVAL_REQUIRED.getMessage(), accountId);
            throw new BusinessException(ErrorCode.PENDING_ADMIN_APPROVAL_REQUIRED);
        }

        // 2) PARTNER SERVICE에 요청 (INTERNAL API) -> partner_profile(partnerDB) 조회
        // TODO: INTERNAL API 호출 로직 추가
        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가
        // FIXME: INTERNAL API 호출 결과에서 가져오는 partner_profile로 변경 (임시 코드)
        PartnerProfileSnapshot profileSnapshot = PartnerProfileSnapshot.builder()
                .name("파트너1")
                .companyName("하늘메이크업")
                .phone("+82-10-0000-0000")
                .businessNum("1234567890")
                .build();

        // 3) withdrawal_history 기록
        WithdrawalHistory history = WithdrawalHistory.builder()
                .accountId(accountId)
                .authMethod(account.getAuthMethod())
                .loginId(account.getLoginId())
                .role(account.getRole())
                .partnerId(account.getPartnerId())
                .profileSnapshot(JsonUtil.toJson(profileSnapshot))
                .reason(request.getReason())
                .deletedBy(DeletionType.ADMIN)
                .joinedAt(account.getCreatedAt())
                .build();

        try {
            withdrawalHistoryService.save(history);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_SAVE_FAILURE.getMessage(), accountId, ex);
            throw new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, ex);
        }

        // 4) auth_account 삭제
        try {
            accountService.deleteById(accountId);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=accountId, identifierValue={}", ErrorCode.DB_DELETE_FAILURE, accountId, ex);
            throw new InfrastructureException(ErrorCode.DB_DELETE_FAILURE, ex);
        }

        // 5) PARTNER SERVICE에 요청 (INTERNAL API) -> partner_profile(partnerDB) 삭제
        // TODO: INTERNAL API 호출 로직 추가
        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가

        // 6) 계정 거부 안내 메일 전송 (비동기, 실패 무시)
        emailService.sendPartnerRejected(account.getLoginId(), request.getReason());
    }
}
