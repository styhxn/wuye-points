package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.rolewithdrawal.AdminRoleWithdrawalListItemResponse;
import com.wuye.points.dto.admin.rolewithdrawal.AdminRoleWithdrawalSummaryResponse;
import com.wuye.points.dto.admin.rolewithdrawal.AuditRoleWithdrawalRequest;
import com.wuye.points.dto.admin.rolewithdrawal.AuditRoleWithdrawalResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.WithdrawalRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import com.wuye.points.repository.spec.WithdrawalRecordSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端 · 配送员/多角色提现审批
 *
 * <p>快递员/个体负责人/统筹/板块负责人提现申请统一落库 {@code withdrawal_records}
 * （{@link WithdrawalRecord}，withdrawal_type = courier / individual_leader / coordinator / sector_leader），
 * 但原有管理端仅提供 {@code /admin/merchant-withdrawals}（商家提现表），无法查询/审核这些记录，
 * 导致“配送员申请已入库、管理端看不到”。本服务补齐管理端查询与审核闭环。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRoleWithdrawalService {

    private static final Set<String> WITHDRAWAL_TYPES = Set.of(
            "individual_leader", "courier", "coordinator", "sector_leader");
    private static final Set<String> AUDIT_RESULTS = Set.of("approved", "rejected", "completed");
    private static final Set<String> AUDIT_STATUS = Set.of(
            "pending", "pending_audit", "approved", "rejected", "completed");

    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final ResidentRepository residentRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final OperationLogService operationLogService;

    /**
     * 管理端提现审批列表（含配送员/个体负责人/统筹/板块负责人，可按类型/状态/关键词/时间筛选）。
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminRoleWithdrawalListItemResponse> listWithdrawals(
            int page,
            int pageSize,
            String withdrawalType,
            String auditStatus,
            String keyword,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermission("merchant.audit");
        validateType(withdrawalType);
        String resolvedStatus = resolveAuditStatus(auditStatus);
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<WithdrawalRecord> withdrawalPage = withdrawalRecordRepository.findAll(
                WithdrawalRecordSpecifications.withAdminFilters(
                        withdrawalType, scopedPropertyCompanyId, resolvedStatus, keyword, start, end),
                pageable);

        Map<String, Resident> residentMap = loadResidentMap(withdrawalPage.getContent());

        List<AdminRoleWithdrawalListItemResponse> list = withdrawalPage.getContent().stream()
                .map(withdrawal -> {
                    Resident resident = residentMap.get(withdrawal.getResidentId());
                    return toListItem(withdrawal, resident);
                })
                .toList();

        return PageResponse.of(list, page, pageSize, withdrawalPage.getTotalElements());
    }

    /**
     * 配送员/多角色提现审批看板汇总：待审笔数/金额、今日已审、已通过合计。
     */
    @Transactional(readOnly = true)
    public AdminRoleWithdrawalSummaryResponse getSummary(String propertyCompanyId) {
        permissionGuard.requirePermission("merchant.audit");
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);

        long pendingCount = withdrawalRecordRepository
                .countByPropertyCompanyIdAndAuditStatus(scopedPropertyCompanyId, "pending")
                + withdrawalRecordRepository
                .countByPropertyCompanyIdAndAuditStatus(scopedPropertyCompanyId, "pending_audit");
        BigDecimal pendingAmount = withdrawalRecordRepository
                .sumAmountByPropertyCompanyIdAndAuditStatus(scopedPropertyCompanyId, "pending")
                .add(withdrawalRecordRepository
                        .sumAmountByPropertyCompanyIdAndAuditStatus(scopedPropertyCompanyId, "pending_audit"));

        LocalDate today = LocalDate.now();
        Object[] approvedRow = firstRow(withdrawalRecordRepository
                .countAndSumApproved(scopedPropertyCompanyId));
        Object[] todayRow = firstRow(withdrawalRecordRepository
                .countAndSumAuditedBetween(scopedPropertyCompanyId, today.atStartOfDay(), today.plusDays(1).atStartOfDay()));

        return AdminRoleWithdrawalSummaryResponse.builder()
                .pendingCount(pendingCount)
                .pendingAmount(defaultDecimal(pendingAmount))
                .todayProcessedCount(rowCount(todayRow))
                .todayProcessedAmount(rowSum(todayRow))
                .approvedCount(rowCount(approvedRow))
                .approvedAmount(rowSum(approvedRow))
                .build();
    }

    /**
     * 审核配送员/多角色提现：pending → approved/rejected；approved → completed/rejected。
     * completed 视为打款完成（人工凭证），写入 disburseChannel/transferProof。
     */
    @Transactional
    public AuditRoleWithdrawalResponse audit(String id, AuditRoleWithdrawalRequest request) {
        permissionGuard.requirePermission("merchant.audit");
        adminSupport.validateIdPrefix(id, "wr");

        WithdrawalRecord withdrawal = withdrawalRecordRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "提现记录不存在"));
        adminSupport.assertSameProperty(withdrawal.getPropertyCompanyId());

        if (!AUDIT_RESULTS.contains(request.getAuditResult())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditResult值无效");
        }
        if ("rejected".equals(request.getAuditResult()) && !StringUtils.hasText(request.getRejectReason())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "拒绝时必须填写rejectReason");
        }

        String currentStatus = withdrawal.getAuditStatus();
        assertTransition(currentStatus, request.getAuditResult());

        withdrawal.setAuditStatus(request.getAuditResult());
        LocalDateTime now = LocalDateTime.now();
        if ("approved".equals(request.getAuditResult()) || "rejected".equals(request.getAuditResult())) {
            withdrawal.setAuditedAt(now);
            withdrawal.setAuditorId(UserContext.currentUserId());
        }
        if ("completed".equals(request.getAuditResult())) {
            withdrawal.setCompletedAt(now);
            if (withdrawal.getAuditedAt() == null) {
                withdrawal.setAuditedAt(now);
            }
            if (!StringUtils.hasText(withdrawal.getAuditorId())) {
                withdrawal.setAuditorId(UserContext.currentUserId());
            }
            // 人工打款：持久化转账凭证，渠道标记为 manual，打款状态标记为已完成
            withdrawal.setDisburseChannel("manual");
            withdrawal.setDisburseStatus("success");
            withdrawal.setTransferProof(request.getTransferProof());
        }
        withdrawalRecordRepository.save(withdrawal);

        operationLogService.log("role_withdrawal", "audit", "role_withdrawal", id,
                null, request);

        return AuditRoleWithdrawalResponse.builder()
                .id(withdrawal.getId())
                .auditStatus(withdrawal.getAuditStatus())
                .transferProof(withdrawal.getTransferProof())
                .disburseChannel(withdrawal.getDisburseChannel())
                .auditedAt(DateTimeUtil.toIsoUtc(withdrawal.getAuditedAt()))
                .auditorId(withdrawal.getAuditorId())
                .completedAt(DateTimeUtil.toIsoUtc(withdrawal.getCompletedAt()))
                .build();
    }

    private AdminRoleWithdrawalListItemResponse toListItem(WithdrawalRecord w, Resident resident) {
        return AdminRoleWithdrawalListItemResponse.builder()
                .id(w.getId())
                .withdrawalType(w.getWithdrawalType())
                .applicantId(w.getApplicantId())
                .applicantName(resident != null ? resident.getName() : null)
                .residentId(w.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .residentPhone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(w.getPropertyCompanyId())
                .amount(w.getAmount())
                .feeRate(w.getFeeRate())
                .feeAmount(w.getFeeAmount())
                .actualAmount(w.getActualAmount())
                .auditStatus(w.getAuditStatus())
                .auditorId(w.getAuditorId())
                .auditedAt(DateTimeUtil.toIsoUtc(w.getAuditedAt()))
                .completedAt(DateTimeUtil.toIsoUtc(w.getCompletedAt()))
                .disburseChannel(w.getDisburseChannel())
                .disburseStatus(w.getDisburseStatus())
                .createdAt(DateTimeUtil.toIsoUtc(w.getCreatedAt()))
                .build();
    }

    private Map<String, Resident> loadResidentMap(List<WithdrawalRecord> withdrawals) {
        List<String> residentIds = withdrawals.stream()
                .map(WithdrawalRecord::getResidentId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity()));
    }

    private void validateType(String withdrawalType) {
        if (StringUtils.hasText(withdrawalType) && !WITHDRAWAL_TYPES.contains(withdrawalType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "withdrawalType值无效");
        }
    }

    private String resolveAuditStatus(String auditStatus) {
        if (!StringUtils.hasText(auditStatus)) {
            return null;
        }
        String value = "pending_audit".equals(auditStatus) ? "pending" : auditStatus;
        if (!AUDIT_STATUS.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus值无效");
        }
        return value;
    }

    private void assertTransition(String currentStatus, String auditResult) {
        if ("rejected".equals(currentStatus) || "completed".equals(currentStatus)) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
        }
        if ("pending".equals(currentStatus) || "pending_audit".equals(currentStatus)) {
            if ("completed".equals(auditResult)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "pending状态不能直接completed");
            }
            return;
        }
        if ("approved".equals(currentStatus)) {
            if (!"completed".equals(auditResult) && !"rejected".equals(auditResult)) {
                throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
            }
        }
    }

    private LocalDateTime parseStartDate(String value) {
        LocalDate date = adminSupport.parseDate(value);
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime parseEndDate(String value) {
        LocalDate date = adminSupport.parseDate(value);
        return date != null ? date.plusDays(1).atStartOfDay() : null;
    }

    private static Object[] firstRow(List<Object[]> rows) {
        return rows == null || rows.isEmpty() ? null : rows.get(0);
    }

    private static Long rowCount(Object[] row) {
        if (row == null || row.length < 1 || !(row[0] instanceof Number n)) {
            return 0L;
        }
        return n.longValue();
    }

    private static BigDecimal rowSum(Object[] row) {
        if (row == null || row.length < 2 || !(row[1] instanceof Number n)) {
            return BigDecimal.ZERO;
        }
        return n instanceof BigDecimal bd ? bd : new BigDecimal(n.toString());
    }

    private static BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
