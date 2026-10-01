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

    private final MerchantWithdrawalRepository merchantWithdrawalRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final MerchantSupport merchantSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public WithdrawalItemResponse apply(ApplyWithdrawalRequest request) {
        Merchant merchant = merchantSupport.requireCurrentWithdrawableMerchant();
        PropertyCompany propertyCompany = propertyCompanyRepository.findById(merchant.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        BigDecimal feeRate = resolveFeeRate(propertyCompany);
        BigDecimal feeAmount = request.getAmount().multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = request.getAmount().subtract(feeAmount);

        MerchantWithdrawal withdrawal = new MerchantWithdrawal();
        withdrawal.setId(idGenerator.generateId("mw"));
        withdrawal.setMerchantId(merchant.getId());
        withdrawal.setPropertyCompanyId(merchant.getPropertyCompanyId());
        withdrawal.setAmount(request.getAmount());
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
