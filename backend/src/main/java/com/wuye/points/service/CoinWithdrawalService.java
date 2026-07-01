package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.coinwithdrawal.ApplyCoinWithdrawalRequest;
import com.wuye.points.dto.coinwithdrawal.CoinWithdrawalItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.CoinWithdrawal;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoinWithdrawalRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.MerchantSupport;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CoinWithdrawalService {

    private static final BigDecimal DEFAULT_WITHDRAWAL_FEE_RATE = new BigDecimal("0.0060");
    private static final Set<String> AUDIT_STATUS = Set.of("pending", "approved", "rejected", "completed");

    private final CoinWithdrawalRepository coinWithdrawalRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyCoinService propertyCoinService;
    private final MerchantSupport merchantSupport;
    private final RoleGuard roleGuard;
    private final PermissionGuard permissionGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public CoinWithdrawalItemResponse apply(ApplyCoinWithdrawalRequest request) {
        Merchant merchant = merchantSupport.requireCurrentMerchant();
        Resident merchantUser = merchantSupport.requireCurrentMerchantUser();

        if (coinWithdrawalRepository.existsByMerchantIdAndAuditStatus(merchant.getId(), "pending")) {
            throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "存在待审核的兑换申请");
        }

        PropertyCompany propertyCompany = propertyCompanyRepository.findById(merchant.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        BigDecimal feeRate = resolveFeeRate(propertyCompany);
        BigDecimal feeAmount = request.getCoinAmount().multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = request.getCoinAmount().subtract(feeAmount);

        propertyCoinService.spendCoins(
                merchantUser,
                request.getCoinAmount(),
                "manual",
                null,
                "物业币兑换申请冻结",
                UserContext.currentUserId(),
                false);

        CoinWithdrawal withdrawal = new CoinWithdrawal();
        withdrawal.setId(idGenerator.generateId("cw"));
        withdrawal.setMerchantId(merchant.getId());
        withdrawal.setPropertyCompanyId(merchant.getPropertyCompanyId());
        withdrawal.setCoinAmount(request.getCoinAmount());
        withdrawal.setCashAmount(request.getCoinAmount());
        withdrawal.setFeeRate(feeRate);
        withdrawal.setFeeAmount(feeAmount);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setAuditStatus("pending");
        coinWithdrawalRepository.save(withdrawal);

        return toItemResponse(withdrawal);
    }

    @Transactional(readOnly = true)
    public PageResponse<CoinWithdrawalItemResponse> listWithdrawals(
            int page,
            int pageSize,
            String merchantId,
            String auditStatus,
            String startDate,
            String endDate) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(auditStatus) && !AUDIT_STATUS.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus值无效");
        }

        String scopedMerchantId = resolveMerchantScope(merchantId);
        Pageable pageable = buildPageable(page, pageSize);
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<CoinWithdrawal> pageResult = coinWithdrawalRepository.findAll(
                com.wuye.points.repository.spec.CoinWithdrawalSpecifications.withFilters(
                        resolvePropertyCompanyScope(), scopedMerchantId, auditStatus, start, end),
                pageable);

        return PageResponse.of(
                pageResult.getContent().stream().map(this::toItemResponse).toList(),
                page,
                pageSize,
                pageResult.getTotalElements());
    }

    private String resolveMerchantScope(String merchantId) {
        if (roleGuard.isMerchant()) {
            Merchant merchant = merchantSupport.requireCurrentMerchant();
            return merchant.getId();
        }
        if (roleGuard.isPlatformAdmin() || roleGuard.isPropertyAdmin()) {
            permissionGuard.requirePermissionOrAdmin("coin.audit");
            return merchantId;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private String resolvePropertyCompanyScope() {
        if (roleGuard.isPlatformAdmin()) {
            return null;
        }
        if (roleGuard.isPropertyAdmin()) {
            return UserContext.currentPropertyCompanyId();
        }
        Merchant merchant = merchantSupport.requireCurrentMerchant();
        return merchant.getPropertyCompanyId();
    }

    private CoinWithdrawalItemResponse toItemResponse(CoinWithdrawal withdrawal) {
        return CoinWithdrawalItemResponse.builder()
                .id(withdrawal.getId())
                .merchantId(withdrawal.getMerchantId())
                .coinAmount(withdrawal.getCoinAmount())
                .cashAmount(withdrawal.getCashAmount())
                .feeRate(withdrawal.getFeeRate())
                .feeAmount(withdrawal.getFeeAmount())
                .actualAmount(withdrawal.getActualAmount())
                .auditStatus(withdrawal.getAuditStatus())
                .createdAt(DateTimeUtil.toIsoUtc(withdrawal.getCreatedAt()))
                .build();
    }

    private BigDecimal resolveFeeRate(PropertyCompany propertyCompany) {
        BigDecimal feeRate = propertyCompany.getWithdrawalFeeRate();
        if (feeRate == null || feeRate.compareTo(BigDecimal.ZERO) < 0) {
            return DEFAULT_WITHDRAWAL_FEE_RATE;
        }
        return feeRate;
    }

    private Pageable buildPageable(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private LocalDateTime parseStartDate(String value) {
        LocalDate date = parseDate(value);
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime parseEndDate(String value) {
        LocalDate date = parseDate(value);
        return date != null ? date.plusDays(1).atStartOfDay() : null;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式应为YYYY-MM-DD");
        }
    }
}
