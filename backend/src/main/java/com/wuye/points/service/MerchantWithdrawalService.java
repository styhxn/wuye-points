package com.wuye.points.service;

import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.withdrawal.ApplyWithdrawalRequest;
import com.wuye.points.dto.merchant.withdrawal.WithdrawalItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantWithdrawal;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantWithdrawalRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.spec.MerchantWithdrawalSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.MerchantSupport;
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
public class MerchantWithdrawalService {

    private static final BigDecimal DEFAULT_WITHDRAWAL_FEE_RATE = new BigDecimal("0.0060");
    private static final Set<String> WITHDRAWAL_AUDIT_STATUS = Set.of(
            "pending", "approved", "rejected", "completed");

    private static final BigDecimal MAX_WITHDRAWAL_AMOUNT = new BigDecimal("999999.99");

    private final MerchantWithdrawalRepository merchantWithdrawalRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final MerchantSupport merchantSupport;
    private final SnowflakeIdGenerator idGenerator;
    private final WithdrawableService withdrawableService;
    private final UnifiedEarningsService unifiedEarningsService;
    private final MerchantCoinReceivableService merchantCoinReceivableService;

    /** 商家物业币应收独立结算入口：强制 source=coin_receivable，避免与内部现金口径混用。 */
    @Transactional
    public WithdrawalItemResponse applyCoinReceivable(BigDecimal amount) {
        ApplyWithdrawalRequest request = new ApplyWithdrawalRequest();
        request.setAmount(amount);
        request.setSource("coin_receivable");
        return apply(request);
    }

    @Transactional
    public WithdrawalItemResponse apply(ApplyWithdrawalRequest request) {
        Merchant merchant = merchantSupport.requireCurrentWithdrawableMerchant();
        PropertyCompany propertyCompany = propertyCompanyRepository.findById(merchant.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        BigDecimal amount = request.getAmount();
        if (amount.compareTo(MAX_WITHDRAWAL_AMOUNT) > 0) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE,
                    "单笔提现金额不能超过 " + MAX_WITHDRAWAL_AMOUNT);
        }
        String source = StringUtils.hasText(request.getSource()) ? request.getSource() : "legacy";
        if (!Set.of("legacy", "coin_receivable").contains(source)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "source值无效");
        }

        boolean withdrawalBlocked;
        if ("coin_receivable".equals(source)) {
            if (merchantCoinReceivableService.isInternalEarningsMerchant(merchant)) {
                throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID,
                        "该店铺收益走内部收益提现，不单独开设物业币应收入口");
            }
            MerchantCoinReceivableService.MerchantIncome coinIncome =
                    merchantCoinReceivableService.calc(merchant);
            withdrawalBlocked = Boolean.TRUE.equals(merchant.getWithdrawalBlocked());
            if (withdrawalBlocked) {
                throw new BizException(ErrorCode.WITHDRAWAL_BLOCKED);
            }
            if (amount.compareTo(coinIncome.coinAvailableAmount()) > 0) {
                throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INSUFFICIENT,
                        "物业币应收可结算余额不足（最多 ¥"
                                + coinIncome.coinAvailableAmount().toPlainString() + "）");
            }
        } else {
            // 资金闸门：申请时必须校验商户真实可提现余额（含 pending/approved 占用），
            // 不能只信任前端展示或本地校验。标准商家此入口只结算平台内部现金，
            // 微信侧现金由商家特约商户号自行提现，物业币应收走独立入口。
            WithdrawableService.MerchantWithdrawable withdrawable =
                    withdrawableService.calcMerchantWithdrawable(merchant);
            withdrawalBlocked = withdrawable.withdrawalBlocked();
            if (withdrawalBlocked) {
                throw new BizException(ErrorCode.WITHDRAWAL_BLOCKED);
            }
            if (amount.compareTo(withdrawable.withdrawableAmount()) > 0) {
                throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INSUFFICIENT,
                        "可提现余额不足（最多 ¥" + withdrawable.withdrawableAmount().toPlainString() + "）");
            }
        }
        // v8.21：账号统一收益账户兜底，防止同一住户先用其他角色把同一笔钱提走
        if (merchant.getResidentId() != null) {
            BigDecimal unifiedWithdrawable = unifiedEarningsService
                    .calc(merchant.getResidentId()).withdrawableAmount();
            if (amount.compareTo(unifiedWithdrawable) > 0) {
                throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INSUFFICIENT,
                        "账号统一收益余额不足（最多 ¥" + unifiedWithdrawable.toPlainString() + "）");
            }
        }

        BigDecimal feeRate = resolveFeeRate(propertyCompany);
        BigDecimal feeAmount = amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = amount.subtract(feeAmount);

        MerchantWithdrawal withdrawal = new MerchantWithdrawal();
        withdrawal.setId(idGenerator.generateId("mw"));
        withdrawal.setMerchantId(merchant.getId());
        withdrawal.setPropertyCompanyId(merchant.getPropertyCompanyId());
        withdrawal.setAmount(amount);
        withdrawal.setSource(source);
        withdrawal.setFeeRate(feeRate);
        withdrawal.setFeeAmount(feeAmount);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setAuditStatus("pending");
        merchantWithdrawalRepository.save(withdrawal);

        return toItemResponse(withdrawal, merchant.getName());
    }

    @Transactional(readOnly = true)
    public PageResponse<WithdrawalItemResponse> listWithdrawals(
            int page,
            int pageSize,
            String auditStatus,
            String startDate,
            String endDate) {
        Merchant merchant = merchantSupport.requireCurrentWithdrawableMerchant();
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(auditStatus) && !WITHDRAWAL_AUDIT_STATUS.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus值无效");
        }

        Pageable pageable = buildPageable(page, pageSize);
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<MerchantWithdrawal> withdrawalPage = merchantWithdrawalRepository.findAll(
                MerchantWithdrawalSpecifications.withFilters(
                        merchant.getPropertyCompanyId(), merchant.getId(), auditStatus, start, end),
                pageable);

        return PageResponse.of(
                withdrawalPage.getContent().stream()
                        .map(withdrawal -> toItemResponse(withdrawal, merchant.getName()))
                        .toList(),
                page,
                pageSize,
                withdrawalPage.getTotalElements());
    }

    private WithdrawalItemResponse toItemResponse(MerchantWithdrawal withdrawal, String merchantName) {
        return WithdrawalItemResponse.builder()
                .id(withdrawal.getId())
                .merchantId(withdrawal.getMerchantId())
                .merchantName(merchantName)
                .amount(withdrawal.getAmount())
                .source(withdrawal.getSource())
                .feeRate(withdrawal.getFeeRate())
                .feeAmount(withdrawal.getFeeAmount())
                .actualAmount(withdrawal.getActualAmount())
                .status(withdrawal.getAuditStatus())
                .auditedAt(DateTimeUtil.toIsoUtc(withdrawal.getAuditedAt()))
                .completedAt(DateTimeUtil.toIsoUtc(withdrawal.getCompletedAt()))
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
