package ready_to_marry.authservice.admin.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ready_to_marry.authservice.account.entity.AuthAccount;
import ready_to_marry.authservice.account.entity.WithdrawalHistory;
import ready_to_marry.authservice.account.service.AccountService;
import ready_to_marry.authservice.account.service.WithdrawalHistoryService;
import ready_to_marry.authservice.admin.dto.request.PartnerRejectionRequest;
import ready_to_marry.authservice.admin.dto.response.PartnerPendingResponse;
import ready_to_marry.authservice.admin.dto.response.PartnerProfileAll;
import ready_to_marry.authservice.admin.dto.response.PartnerProfileSnapshot;
import ready_to_marry.authservice.common.dto.request.PagingRequest;
import ready_to_marry.authservice.common.enums.AccountStatus;
import ready_to_marry.authservice.common.enums.DeletionType;
import ready_to_marry.authservice.common.enums.Role;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.common.util.JsonUtil;
import ready_to_marry.authservice.partner.dto.request.PartnerResponseDto;
import ready_to_marry.authservice.partner.email.EmailService;
import ready_to_marry.authservice.partner.service.PartnerClient;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerApprovalServiceImpl implements PartnerApprovalService{
    private final AccountService accountService;
    private final WithdrawalHistoryService withdrawalHistoryService;
    private final EmailService emailService;
    private final PartnerClient partnerClient;

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
        // TODO: INTERNAL API 호출 로직 추가 O
        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가 O
        PartnerResponseDto partnerResponseDto;
        try {
            partnerResponseDto = partnerClient.getPartnerProfile(account.getPartnerId()); // 예: partnerId가 있다면
        } catch (Exception e) {
            throw new InfrastructureException(ErrorCode.EXTERNAL_API_FAILURE, e);
        }
        // FIXME: INTERNAL API 호출 결과에서 가져오는 partner_profile로 변경 (임시 코드) O
        PartnerProfileSnapshot profileSnapshot = PartnerProfileSnapshot.builder()
                .name(partnerResponseDto.getName())
                .companyName(partnerResponseDto.getCompanyName())
                .phone(partnerResponseDto.getPhone())
                .businessNum(partnerResponseDto.getBusinessNum())
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
        // TODO: INTERNAL API 호출 로직 추가 O
        // TODO: INTERNAL API 호출 에러 시 처리 로직 추가 O
        try {
            partnerClient.deletePartnerProfile(account.getPartnerId());
        } catch (Exception e) {
            log.error("{}: Failed to delete partner profile for partnerId={}", ErrorCode.EXTERNAL_API_FAILURE, account.getPartnerId(), e);
            throw new InfrastructureException(ErrorCode.EXTERNAL_API_FAILURE, e);
        }

        // 6) 계정 거부 안내 메일 전송 (비동기, 실패 무시)
        emailService.sendPartnerRejected(account.getLoginId(), request.getReason());
    }

    @Override
    public Page<PartnerPendingResponse> getPendingPartners(PagingRequest pagingRequest) {
        // 1) 페이징 요청 정보 생성
        PageRequest pageRequest = PageRequest.of(pagingRequest.getPage(), pagingRequest.getSize());

        // 2) 관리자 승인 대기 중인 파트너 계정 목록을 생성 시각 기준으로 오름차순 정렬하여 조회
        Page<AuthAccount> page = fetchPendingAccounts(pageRequest);

        // 3) 각 AuthAccount마다 PARTNER SERVICE에 요청 (INTERNAL API) -> partner_profile(partnerDB)에서 조회
        // 4) AuthAccount + PartnerProfileAll → PartnerPendingResponse 매핑
        return mapToPendingResponse(page);
    }

    @Transactional(readOnly = true)
    public Page<AuthAccount> fetchPendingAccounts(PageRequest pageRequest) {
        Page<AuthAccount> page;
        try {
            page = accountService.findByRoleAndStatus(Role.PARTNER, AccountStatus.PENDING_ADMIN_APPROVAL, pageRequest);
        } catch (DataAccessException ex) {
            log.error("{}: identifierType=none, identifierValue=none", ErrorCode.DB_RETRIEVE_FAILURE.getMessage(), ex);
            throw new InfrastructureException(ErrorCode.DB_RETRIEVE_FAILURE, ex);
        }

        return page;
    }

    public Page<PartnerPendingResponse> mapToPendingResponse(Page<AuthAccount> page) {
        return page.map(account -> {
            // TODO: INTERNAL API 호출 로직 추가 o
            // TODO: INTERNAL API 호출 에러 시 처리 로직 추가 o
            PartnerResponseDto partnerResponseDto;
            try {
                partnerResponseDto = partnerClient.getPartnerProfile(account.getPartnerId()); // 예: partnerId가 있다면
            } catch (Exception e) {
                throw new InfrastructureException(ErrorCode.EXTERNAL_API_FAILURE, e);
            }
            // FIXME: INTERNAL API 호출 결과에서 가져오는 파트너 프로필 정보로 변경 (임시 코드) o
            PartnerProfileAll partnerProfileAll = PartnerProfileAll.builder()
                    .name(partnerResponseDto.getName())
                    .companyName(partnerResponseDto.getCompanyName())
                    .address(partnerResponseDto.getAddress())
                    .phone(partnerResponseDto.getPhone())
                    .companyNum(partnerResponseDto.getCompanyNum())
                    .businessNum(partnerResponseDto.getBusinessNum())
                    .build();

            return PartnerPendingResponse.builder()
                    .accountId(account.getAccountId())
                    .createdAt(account.getCreatedAt())
                    .name(partnerProfileAll.getName())
                    .companyName(partnerProfileAll.getCompanyName())
                    .address(partnerProfileAll.getAddress())
                    .phone(partnerProfileAll.getPhone())
                    .companyNum(partnerProfileAll.getCompanyNum())
                    .businessNum(partnerProfileAll.getBusinessNum())
                    .build();
        });
    }
}
