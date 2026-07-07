package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.merchantaudit.AuditMerchantPointPurchaseRequest;
import com.wuye.points.dto.admin.merchantaudit.AuditMerchantPointPurchaseResponse;
import com.wuye.points.dto.admin.merchantaudit.AuditMerchantWithdrawalRequest;
import com.wuye.points.dto.admin.merchantaudit.AuditMerchantWithdrawalResponse;
import com.wuye.points.dto.admin.merchantaudit.MerchantPointPurchaseListItemResponse;
import com.wuye.points.dto.admin.merchantaudit.MerchantWithdrawalListItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPointPurchase;
import com.wuye.points.entity.MerchantWithdrawal;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantPointPurchaseRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantWithdrawalRepository;
import com.wuye.points.repository.spec.MerchantPointPurchaseSpecifications;
import com.wuye.points.repository.spec.MerchantWithdrawalSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMerchantAuditService {

    private static final Set<String> PURCHASE_AUDIT_RESULTS = Set.of("approved", "rejected");
    private static final Set<String> WITHDRAWAL_AUDIT_RESULTS = Set.of("approved", "rejected", "completed");
    private static final Set<String> PURCHASE_AUDIT_STATUS = Set.of("pending", "pending_audit", "approved", "rejected");
    private static final Set<String> WITHDRAWAL_AUDIT_STATUS = Set.of(
            "pending", "approved", "rejected", "completed");

    private final MerchantPointPurchaseRepository merchantPointPurchaseRepository;
    private final MerchantWithdrawalRepository merchantWithdrawalRepository;
    private final MerchantRepository merchantRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public PageResponse<MerchantPointPurchaseListItemResponse> listPointPurchases(
            int page,
            int pageSize,
            String merchantId,
            String auditStatus,
            String status,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermission("merchant.audit");
        String resolvedStatus = resolveAuditStatus(auditStatus, status, PURCHASE_AUDIT_STATUS);
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<MerchantPointPurchase> purchasePage = merchantPointPurchaseRepository.findAll(
                MerchantPointPurchaseSpecifications.withFilters(
                        scopedPropertyCompanyId, merchantId, resolvedStatus, start, end),
                pageable);

        Map<String, Merchant> merchantMap = resolveMerchants(purchasePage.getContent().stream()
                .map(MerchantPointPurchase::getMerchantId)
                .collect(Collectors.toSet()));

        List<MerchantPointPurchaseListItemResponse> list = purchasePage.getContent().stream()
                .map(purchase -> {
                    Merchant merchant = merchantMap.get(purchase.getMerchantId());
                    return MerchantPointPurchaseListItemResponse.builder()
                            .id(purchase.getId())
                            .merchantId(purchase.getMerchantId())
                            .merchantName(merchant != null ? merchant.getName() : null)
                            .pointAmount(purchase.getPointAmount())
                            .payAmount(purchase.getPayAmount())
                            .auditStatus(purchase.getAuditStatus())
                            .remainingPoints(purchase.getRemainingPoints())
                            .auditedAt(DateTimeUtil.toIsoUtc(purchase.getAuditedAt()))
                            .auditorId(purchase.getAuditorId())
                            .createdAt(DateTimeUtil.toIsoUtc(purchase.getCreatedAt()))
                            .build();
                })
                .toList();

        return PageResponse.of(list, page, pageSize, purchasePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<MerchantWithdrawalListItemResponse> listWithdrawals(
            int page,
            int pageSize,
            String merchantId,
            String auditStatus,
            String status,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermission("merchant.audit");
        String resolvedStatus = resolveAuditStatus(auditStatus, status, WITHDRAWAL_AUDIT_STATUS);
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<MerchantWithdrawal> withdrawalPage = merchantWithdrawalRepository.findAll(
                MerchantWithdrawalSpecifications.withFilters(
                        scopedPropertyCompanyId, merchantId, resolvedStatus, start, end),
                pageable);

        Map<String, Merchant> merchantMap = resolveMerchants(withdrawalPage.getContent().stream()
                .map(MerchantWithdrawal::getMerchantId)
                .collect(Collectors.toSet()));

        List<MerchantWithdrawalListItemResponse> list = withdrawalPage.getContent().stream()
                .map(withdrawal -> {
                    Merchant merchant = merchantMap.get(withdrawal.getMerchantId());
                    return MerchantWithdrawalListItemResponse.builder()
                            .id(withdrawal.getId())
                            .merchantId(withdrawal.getMerchantId())
                            .merchantName(merchant != null ? merchant.getName() : null)
                            .amount(withdrawal.getAmount())
                            .feeRate(withdrawal.getFeeRate())
                            .feeAmount(withdrawal.getFeeAmount())
                            .actualAmount(withdrawal.getActualAmount())
                            .auditStatus(withdrawal.getAuditStatus())
                            .auditedAt(DateTimeUtil.toIsoUtc(withdrawal.getAuditedAt()))
                            .completedAt(DateTimeUtil.toIsoUtc(withdrawal.getCompletedAt()))
                            .createdAt(DateTimeUtil.toIsoUtc(withdrawal.getCreatedAt()))
                            .build();
                })
                .toList();

        return PageResponse.of(list, page, pageSize, withdrawalPage.getTotalElements());
    }

    @Transactional
    public AuditMerchantPointPurchaseResponse auditPointPurchase(
            String id,
            AuditMerchantPointPurchaseRequest request) {
        permissionGuard.requirePermission("merchant.audit");
        adminSupport.validateIdPrefix(id, "mpp");

        MerchantPointPurchase purchase = merchantPointPurchaseRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "购买记录不存在"));
        adminSupport.assertSameProperty(purchase.getPropertyCompanyId());

        if (!"pending".equals(purchase.getAuditStatus())) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
        }
        if (!PURCHASE_AUDIT_RESULTS.contains(request.getAuditResult())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditResult值无效");
        }
        if ("rejected".equals(request.getAuditResult()) && !StringUtils.hasText(request.getRejectReason())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "拒绝时必须填写rejectReason");
        }

        int actualPointAmount = request.getActualPointAmount() != null
                ? request.getActualPointAmount()
                : purchase.getPointAmount();

        if ("approved".equals(request.getAuditResult())) {
            if (actualPointAmount <= 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "actualPointAmount必须大于0");
            }
            purchase.setAuditStatus("approved");
            purchase.setRemainingPoints(actualPointAmount);
        } else {
            purchase.setAuditStatus("rejected");
            purchase.setRemainingPoints(0);
        }

        purchase.setAuditedAt(LocalDateTime.now());
        purchase.setAuditorId(UserContext.currentUserId());
        merchantPointPurchaseRepository.save(purchase);

        operationLogService.log("merchant_point_purchase", "audit", "merchant_point_purchase", id,
                null, request);

        int merchantPointBalance = sumMerchantRemainingPoints(purchase.getMerchantId());

        return AuditMerchantPointPurchaseResponse.builder()
                .id(purchase.getId())
                .auditStatus(purchase.getAuditStatus())
                .pointAmount(purchase.getPointAmount())
                .actualPointAmount("approved".equals(purchase.getAuditStatus()) ? actualPointAmount : null)
                .merchantPointBalance(merchantPointBalance)
                .auditedAt(DateTimeUtil.toIsoUtc(purchase.getAuditedAt()))
                .auditorId(purchase.getAuditorId())
                .build();
    }

    @Transactional
    public AuditMerchantWithdrawalResponse auditWithdrawal(
            String id,
            AuditMerchantWithdrawalRequest request) {
        permissionGuard.requirePermission("merchant.audit");
        adminSupport.validateIdPrefix(id, "mw");

        MerchantWithdrawal withdrawal = merchantWithdrawalRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "提现记录不存在"));
        adminSupport.assertSameProperty(withdrawal.getPropertyCompanyId());

        if (!WITHDRAWAL_AUDIT_RESULTS.contains(request.getAuditResult())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditResult值无效");
        }
        if ("rejected".equals(request.getAuditResult()) && !StringUtils.hasText(request.getRejectReason())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "拒绝时必须填写rejectReason");
        }

        String currentStatus = withdrawal.getAuditStatus();
        assertWithdrawalTransition(currentStatus, request.getAuditResult());

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
        }

        merchantWithdrawalRepository.save(withdrawal);

        operationLogService.log("merchant_withdrawal", "audit", "merchant_withdrawal", id,
                null, request);

        return AuditMerchantWithdrawalResponse.builder()
                .id(withdrawal.getId())
                .auditStatus(withdrawal.getAuditStatus())
                .transferProof(request.getTransferProof())
                .auditedAt(DateTimeUtil.toIsoUtc(withdrawal.getAuditedAt()))
                .auditorId(withdrawal.getAuditorId())
                .completedAt(DateTimeUtil.toIsoUtc(withdrawal.getCompletedAt()))
                .build();
    }

    private void assertWithdrawalTransition(String currentStatus, String auditResult) {
        if ("rejected".equals(currentStatus) || "completed".equals(currentStatus)) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
        }
        if ("pending".equals(currentStatus)) {
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

    private int sumMerchantRemainingPoints(String merchantId) {
        List<MerchantPointPurchase> purchases = merchantPointPurchaseRepository.findByMerchantId(
                merchantId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        return purchases.stream()
                .filter(p -> "approved".equals(p.getAuditStatus()))
                .mapToInt(p -> p.getRemainingPoints() != null ? p.getRemainingPoints() : 0)
                .sum();
    }

    private Map<String, Merchant> resolveMerchants(Set<String> merchantIds) {
        Map<String, Merchant> map = new HashMap<>();
        if (merchantIds.isEmpty()) {
            return map;
        }
        merchantRepository.findAllById(merchantIds).forEach(m -> map.put(m.getId(), m));
        return map;
    }

    private String resolveAuditStatus(String auditStatus, String status, Set<String> allowed) {
        String value = StringUtils.hasText(auditStatus) ? auditStatus : status;
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (!allowed.contains(value)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus值无效");
        }
        if ("pending_audit".equals(value)) {
            return "pending";
        }
        return value;
    }

    private LocalDateTime parseStartDate(String value) {
        LocalDate date = adminSupport.parseDate(value);
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime parseEndDate(String value) {
        LocalDate date = adminSupport.parseDate(value);
        return date != null ? date.plusDays(1).atStartOfDay() : null;
    }
}
