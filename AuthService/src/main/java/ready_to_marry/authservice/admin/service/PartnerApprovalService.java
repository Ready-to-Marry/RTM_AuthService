package ready_to_marry.authservice.admin.service;

import org.springframework.data.domain.Page;
import ready_to_marry.authservice.admin.dto.request.PartnerRejectionRequest;
import ready_to_marry.authservice.admin.dto.response.PartnerPendingResponse;
import ready_to_marry.authservice.common.dto.request.PagingRequest;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.InfrastructureException;

import java.util.UUID;

/**
 * 파트너 승인·거부·조회 비즈니스 로직을 제공하는 서비스 인터페이스
 */
public interface PartnerApprovalService {
    /**
     * 파트너 계정 승인
     * 1) 계정 조회 및 상태 확인
     * 2) auth_account에 status 업데이트 (PENDING_ADMIN_APPROVAL -> ACTIVE)
     * 3) 계정 승인 안내 메일 전송 (비동기, 실패 무시)
     *
     * @param accountId 승인 대상 계정의 UUID
     * @throws BusinessException        PENDING_ADMIN_APPROVAL_REQUIRED
     * @throws InfrastructureException  DB_RETRIEVE_FAILURE
     * @throws InfrastructureException  DB_SAVE_FAILURE
     */
    void approvePartner(UUID accountId);

    /**
     * 파트너 계정 거부
     * 1) 계정 조회 및 상태 확인
     * 2) PARTNER SERVICE에 요청 (INTERNAL API) -> partner_profile(partnerDB) 조회
     * 3) withdrawal_history 기록
     * 4) auth_account 삭제
     * 5) PARTNER SERVICE에 요청 (INTERNAL API) -> partner_profile(partnerDB) 삭제
     * 6) 계정 거부 안내 메일 전송 (비동기, 실패 무시)
     *
     * @param accountId 거부 대상 계정의 UUID
     * @param request 관리자 파트너 승인 거절 요청 DTO
     * @throws BusinessException        PENDING_ADMIN_APPROVAL_REQUIRED
     * @throws InfrastructureException  DB_RETRIEVE_FAILURE
     * @throws InfrastructureException  DB_SAVE_FAILURE
     * @throws InfrastructureException  DB_DELETE_FAILURE
     * @throws InfrastructureException  JSON_SERIALIZATION_FAILURE
     */
    void rejectPartner(UUID accountId, PartnerRejectionRequest request);

    /**
     * PENDING_ADMIN_APPROVAL 상태인 파트너 계정들을 생성 시각 오름차순으로 페이징 조회
     * 1) 페이징 요청 정보 생성
     * 2) 관리자 승인 대기 중인 파트너 계정 목록을 생성 시각 기준으로 오름차순 정렬하여 조회
     * 3) 각 AuthAccount마다 PARTNER SERVICE에 요청 (INTERNAL API) -> partner_profile(partnerDB)에서 조회
     * 4) AuthAccount + PartnerProfileAll → PartnerPendingResponse 매핑
     *
     * @param pagingRequest                             페이징 요청 정보 (page, size)
     * @return Page<PartnerPendingResponse>             조회된 계정 목록 페이징 결과
     * @throws InfrastructureException                  DB_RETRIEVE_FAILURE
     */
    Page<PartnerPendingResponse> getPendingPartners(PagingRequest pagingRequest);
}
