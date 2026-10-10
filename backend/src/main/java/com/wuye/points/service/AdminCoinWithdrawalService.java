package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.coinwithdrawal.AuditCoinWithdrawalRequest;
import com.wuye.points.dto.admin.coinwithdrawal.AuditCoinWithdrawalResponse;
import com.wuye.points.dto.admin.coinwithdrawal.CoinWithdrawalListItemResponse;
import com.wuye.points.dto.admin.coinwithdrawal.CoinWithdrawalSummaryResponse;
import com.wuye.points.dto.coinwithdrawal.BlockWithdrawalRequest;
import com.wuye.points.dto.coinwithdrawal.UpdateWithdrawalSettingsRequest;
import com.wuye.points.dto.coinwithdrawal.WithdrawalSettingsResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.CoinWithdrawal;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.CoinType;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoinWithdrawalRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.CoinWithdrawalSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCoinWithdrawalService {

    private static final Set<String> AUDIT_RESULTS = Set.of("approved", "rejected", "completed");
    private static final Set<String> AUDIT_STATUS = Set.of("pending", "approved", "rejected", "completed");
    private static final int DEFAULT_AUTO_WITHDRAWAL_PERIOD_DAYS = 7;

    private final CoinWithdrawalRepository coinWithdrawalRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyCoinService propertyCoinService;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final OperationLogService operationLogService;
    private final PlatformSettingService platformSetting;

    @Transactional(readOnly = true)
    public PageResponse<CoinWithdrawalListItemResponse> listWithdrawals(
            int page,
            int pageSize,
            String merchantId,
            String auditStatus,
            String status,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermission("coin.audit");
        String resolvedStatus = resolveAuditStatus(auditStatus, status);
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<CoinWithdrawal> pageResult = coinWithdrawalRepository.findAll(
                CoinWithdrawalSpecifications.withFilters(
                        scopedPropertyCompanyId, merchantId, resolvedStatus, start, end),
                pageable);

        Map<String, Merchant> merchantMap = resolveMerchants(pageResult.getContent().stream()
                .map(CoinWithdrawal::getMerchantId)
                .collect(Collectors.toSet()));

        List<CoinWithdrawalListItemResponse> list = pageResult.getContent().stream()
                .map(withdrawal -> {
                    Merchant merchant = merchantMap.get(withdrawal.getMerchantId());
                    return CoinWithdrawalListItemResponse.builder()
                            .id(withdrawal.getId())
                            .merchantId(withdrawal.getMerchantId())
                            .merchantName(merchant != null ? merchant.getName() : null)
                            .coinAmount(withdrawal.getCoinAmount())
                            .cashAmount(withdrawal.getCashAmount())
                            .feeRate(withdrawal.getFeeRate())
                            .feeAmount(withdrawal.getFeeAmount())
                            .actualAmount(withdrawal.getActualAmount())
                            .auditStatus(withdrawal.getAuditStatus())
                            .createdAt(DateTimeUtil.toIsoUtc(withdrawal.getCreatedAt()))
                            .build();
                })
                .toList();

        return PageResponse.of(list, page, pageSize, pageResult.getTotalElements());
    }

    @Transactional
    public AuditCoinWithdrawalResponse audit(String id, AuditCoinWithdrawalRequest request) {
        permissionGuard.requirePermission("coin.audit");
        adminSupport.validateIdPrefix(id, "cw");

        CoinWithdrawal withdrawal = coinWithdrawalRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "兑换记录不存在"));
        adminSupport.assertSameProperty(withdrawal.getPropertyCompanyId());

        if (!AUDIT_RESULTS.contains(request.getAuditResult())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditResult值无效");
        }
        if ("rejected".equals(request.getAuditResult()) && !StringUtils.hasText(request.getRejectReason())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "拒绝时必须填写rejectReason");
        }

        String currentStatus = withdrawal.getAuditStatus();
        assertTransition(currentStatus, request.getAuditResult());

        if ("rejected".equals(request.getAuditResult())
                && ("pending".equals(currentStatus) || "approved".equals(currentStatus))) {
            refundCoins(withdrawal);
        }

        withdrawal.setAuditStatus(request.getAuditResult());
        coinWithdrawalRepository.save(withdrawal);

        operationLogService.log("coin_withdrawal", "audit", "coin_withdrawal", id,
                null, request);

        return AuditCoinWithdrawalResponse.builder()
                .id(withdrawal.getId())
                .auditStatus(withdrawal.getAuditStatus())
                .coinAmount(withdrawal.getCoinAmount())
                .actualAmount(withdrawal.getActualAmount())
                .auditedAt(DateTimeUtil.nowIsoUtc())
                .auditorId(UserContext.currentUserId())
                .build();
    }

    @Transactional(readOnly = true)
    public CoinWithdrawalSummaryResponse getSummary(String propertyCompanyId) {
        permissionGuard.requirePermission("coin.audit");
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();

        return CoinWithdrawalSummaryResponse.builder()
                .pendingCount(coinWithdrawalRepository.countByStatuses(
                        scopedPropertyCompanyId, List.of("pending")))
                .pendingAmount(nz(coinWithdrawalRepository.sumActualAmountByStatuses(
                        scopedPropertyCompanyId, List.of("pending"))))
                .todayCompletedCount(coinWithdrawalRepository.countCompletedBetween(
                        scopedPropertyCompanyId, todayStart, todayEnd))
                .todayCompletedAmount(nz(coinWithdrawalRepository.sumCompletedBetween(
                        scopedPropertyCompanyId, todayStart, todayEnd)))
                .approvedCount(coinWithdrawalRepository.countByStatuses(
                        scopedPropertyCompanyId, List.of("approved", "completed")))
                .approvedAmount(nz(coinWithdrawalRepository.sumActualAmountByStatuses(
                        scopedPropertyCompanyId, List.of("approved", "completed"))))
                .build();
    }

    @Transactional(readOnly = true)
    public WithdrawalSettingsResponse getSettings(String propertyCompanyId) {
        permissionGuard.requirePermission("coin.audit");
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        if (!StringUtils.hasText(scopedPropertyCompanyId)) {
            return WithdrawalSettingsResponse.builder()
                    .propertyCompanyId(null)
                    .autoEnabled(false)
                    .periodDays(DEFAULT_AUTO_WITHDRAWAL_PERIOD_DAYS)
                    .feeRate(platformSetting.resolveWithdrawalFeeRate(
                            null, PlatformSettingService.FEE_CATEGORY_PROPERTY))
                    .build();
        }
        PropertyCompany company = propertyCompanyRepository.findById(scopedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        return WithdrawalSettingsResponse.builder()
                .propertyCompanyId(company.getId())
                .autoEnabled(Boolean.TRUE.equals(company.getAutoWithdrawalEnabled()))
                .periodDays(company.getAutoWithdrawalPeriodDays() != null
                        ? company.getAutoWithdrawalPeriodDays() : DEFAULT_AUTO_WITHDRAWAL_PERIOD_DAYS)
                .feeRate(platformSetting.resolveWithdrawalFeeRate(
                        company.getWithdrawalFeeRate(), PlatformSettingService.FEE_CATEGORY_PROPERTY))
                .build();
    }

    @Transactional
    public WithdrawalSettingsResponse updateSettings(String propertyCompanyId, UpdateWithdrawalSettingsRequest request) {
        permissionGuard.requirePermission("coin.audit");
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        if (!StringUtils.hasText(scopedPropertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }
        PropertyCompany company = propertyCompanyRepository.findById(scopedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        company.setAutoWithdrawalEnabled(request.getAutoEnabled());
        company.setAutoWithdrawalPeriodDays(request.getPeriodDays());
        propertyCompanyRepository.save(company);
        operationLogService.log("property_company", "update_withdrawal_settings",
                "property_company", company.getId(), null, request);
        return WithdrawalSettingsResponse.builder()
                .propertyCompanyId(company.getId())
                .autoEnabled(Boolean.TRUE.equals(company.getAutoWithdrawalEnabled()))
                .periodDays(company.getAutoWithdrawalPeriodDays())
                .feeRate(platformSetting.resolveWithdrawalFeeRate(
                        company.getWithdrawalFeeRate(), PlatformSettingService.FEE_CATEGORY_PROPERTY))
                .build();
    }

    /** 阻止/解阻止居民（住户）提现（§80.3），仅影响其提现资格，不影响其他功能。 */
    @Transactional
    public void blockResident(String residentId, BlockWithdrawalRequest request) {
        permissionGuard.requirePermission("coin.audit");
        adminSupport.validateIdPrefix(residentId, "res");
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        resident.setWithdrawalBlocked(request.getBlocked());
        residentRepository.save(resident);
        operationLogService.log("resident", request.getBlocked() ? "block_withdrawal" : "unblock_withdrawal",
                "resident", residentId, null, request);
    }

    /** 阻止/解阻止商家提现（§80.4），仅影响其提现资格，不影响其他功能。 */
    @Transactional
    public void blockMerchant(String merchantId, BlockWithdrawalRequest request) {
        permissionGuard.requirePermission("coin.audit");
        adminSupport.validateIdPrefix(merchantId, "mch");
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        adminSupport.assertSameProperty(merchant.getPropertyCompanyId());
        merchant.setWithdrawalBlocked(request.getBlocked());
        merchantRepository.save(merchant);
        operationLogService.log("merchant", request.getBlocked() ? "block_withdrawal" : "unblock_withdrawal",
                "merchant", merchantId, null, request);
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private void refundCoins(CoinWithdrawal withdrawal) {
        Merchant merchant = merchantRepository.findById(withdrawal.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        Resident merchantUser = residentRepository.findByPhone(merchant.getContactPhone())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "商家账号不存在"));

        propertyCoinService.earnCoins(
                merchantUser,
                withdrawal.getCoinAmount(),
                CoinType.earn.name(),
                "manual",
                withdrawal.getId(),
                "物业币兑换审核拒绝退回",
                UserContext.currentUserId());
    }

    private void assertTransition(String currentStatus, String auditResult) {
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

    private Map<String, Merchant> resolveMerchants(Set<String> merchantIds) {
        Map<String, Merchant> map = new HashMap<>();
        if (merchantIds.isEmpty()) {
            return map;
        }
        merchantRepository.findAllById(merchantIds).forEach(m -> map.put(m.getId(), m));
        return map;
    }

    private String resolveAuditStatus(String auditStatus, String status) {
        String value = StringUtils.hasText(auditStatus) ? auditStatus : status;
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (!AUDIT_STATUS.contains(value)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus值无效");
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
