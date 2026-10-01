package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertycompany.ApplySettlementWithdrawalRequest;
import com.wuye.points.dto.propertycompany.SettlementBalanceResponse;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.WithdrawalRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import com.wuye.points.repository.spec.WithdrawalRecordSpecifications;
import com.wuye.points.util.DateTimeUtil;
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
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 分成账户服务：物业公司独立结算账户（与虚拟币账户资产隔离）。
 * 业主消费分成自动进入 settlement_balance，物业管理员可发起提现，平台管理员审核后出账。
 */
@Service
@RequiredArgsConstructor
public class PropertyCompanySettlementService {

    private static final String WITHDRAWAL_TYPE_PROPERTY_COMPANY = "property_company";
    private static final Set<String> AUDIT_STATUSES = Set.of("pending", "approved", "rejected", "completed");

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final PlatformSettingService platformSetting;

    @Transactional(readOnly = true)
    public SettlementBalanceResponse getBalance(String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        PropertyCompany company = getScopedCompany(propertyCompanyId);
        return SettlementBalanceResponse.builder()
                .propertyCompanyId(company.getId())
                .settlementBalance(defaultDecimal(company.getSettlementBalance()))
                .build();
    }

    @Transactional
    public RoleWithdrawalItemResponse apply(String propertyCompanyId, ApplySettlementWithdrawalRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        PropertyCompany company = getScopedCompany(propertyCompanyId);

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "提现金额无效");
        }
        BigDecimal balance = defaultDecimal(company.getSettlementBalance());
        if (amount.compareTo(balance) > 0) {
            throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "分成账户余额不足");
        }

        BigDecimal feeRate = resolveFeeRate(company);
        BigDecimal feeAmount = amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = amount.subtract(feeAmount);
        BigDecimal platformFeeShare = resolvePlatformFeeShare(feeAmount, company);

        WithdrawalRecord withdrawal = new WithdrawalRecord();
        withdrawal.setId(idGenerator.generateId("wr"));
        withdrawal.setWithdrawalType(WITHDRAWAL_TYPE_PROPERTY_COMPANY);
        withdrawal.setApplicantId(company.getId());
        withdrawal.setResidentId(UserContext.currentUserId());
        withdrawal.setPropertyCompanyId(company.getId());
        withdrawal.setAmount(amount);
        withdrawal.setFeeRate(feeRate);
        withdrawal.setFeeAmount(feeAmount);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setPlatformFeeShare(platformFeeShare);
        withdrawal.setAuditStatus("pending");
        withdrawalRecordRepository.save(withdrawal);

        return toItemResponse(withdrawal);
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleWithdrawalItemResponse> list(
            String propertyCompanyId, String auditStatus, int page, int pageSize,
            String startDate, String endDate) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(auditStatus) && !AUDIT_STATUSES.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus值无效");
        }
        PropertyCompany company = getScopedCompany(propertyCompanyId);

        LocalDateTime start = parseDate(startDate);
        LocalDateTime end = parseDate(endDate);
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<WithdrawalRecord> recordPage = withdrawalRecordRepository.findAll(
                WithdrawalRecordSpecifications.withFilters(
                        WITHDRAWAL_TYPE_PROPERTY_COMPANY, company.getId(), company.getId(),
                        auditStatus, start, end),
                pageable);

        return PageResponse.of(
                recordPage.getContent().stream().map(this::toItemResponse).toList(),
                page, pageSize, recordPage.getTotalElements());
    }

    /**
     * 平台管理员审核通过：从分成账户余额扣除提现金额。
     */
    @Transactional
    public RoleWithdrawalItemResponse approve(String propertyCompanyId, String withdrawalId) {
        roleGuard.requireAnyRole("platform_admin");
        WithdrawalRecord withdrawal = getPendingWithdrawal(propertyCompanyId, withdrawalId);
        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        BigDecimal balance = defaultDecimal(company.getSettlementBalance());
        if (defaultDecimal(withdrawal.getAmount()).compareTo(balance) > 0) {
            throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "分成账户余额不足，无法审核通过");
        }
        company.setSettlementBalance(balance.subtract(withdrawal.getAmount()));
        propertyCompanyRepository.save(company);

        withdrawal.setAuditStatus("approved");
        withdrawal.setAuditorId(UserContext.currentUserId());
        withdrawal.setAuditedAt(LocalDateTime.now());
        withdrawal.setCompletedAt(LocalDateTime.now());
        withdrawalRecordRepository.save(withdrawal);

        return toItemResponse(withdrawal);
    }

    @Transactional
    public RoleWithdrawalItemResponse reject(String propertyCompanyId, String withdrawalId) {
        roleGuard.requireAnyRole("platform_admin");
        WithdrawalRecord withdrawal = getPendingWithdrawal(propertyCompanyId, withdrawalId);
        withdrawal.setAuditStatus("rejected");
        withdrawal.setAuditorId(UserContext.currentUserId());
        withdrawal.setAuditedAt(LocalDateTime.now());
        withdrawalRecordRepository.save(withdrawal);
        return toItemResponse(withdrawal);
    }

    // ============== 私有工具 ==============

    private WithdrawalRecord getPendingWithdrawal(String propertyCompanyId, String withdrawalId) {
        if (!StringUtils.hasText(withdrawalId) || !withdrawalId.startsWith("wr_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "withdrawalId格式不正确");
        }
        WithdrawalRecord withdrawal = withdrawalRecordRepository.findById(withdrawalId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "提现记录不存在"));
        if (!WITHDRAWAL_TYPE_PROPERTY_COMPANY.equals(withdrawal.getWithdrawalType())
                || !propertyCompanyId.equals(withdrawal.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        if (!"pending".equals(withdrawal.getAuditStatus())) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
        }
        return withdrawal;
    }

    private PropertyCompany getScopedCompany(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId) || !propertyCompanyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
        }
        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        if (!roleGuard.isPlatformAdmin()) {
            roleGuard.assertSamePropertyOrPlatform(company.getId());
        }
        return company;
    }

    private BigDecimal resolveFeeRate(PropertyCompany company) {
        return platformSetting.resolveWithdrawalFeeRate(
                company.getWithdrawalFeeRate(), PlatformSettingService.FEE_CATEGORY_PROPERTY);
    }

    private BigDecimal resolvePlatformFeeShare(BigDecimal feeAmount, PropertyCompany company) {
        BigDecimal shareRate = platformSetting.resolvePlatformFeeShareRate(
                company.getPlatformWithdrawalFeeShareRate());
        return feeAmount.multiply(shareRate).setScale(2, RoundingMode.HALF_UP);
    }

    private RoleWithdrawalItemResponse toItemResponse(WithdrawalRecord withdrawal) {
        return RoleWithdrawalItemResponse.builder()
                .id(withdrawal.getId())
                .withdrawalType(withdrawal.getWithdrawalType())
                .applicantId(withdrawal.getApplicantId())
                .residentId(withdrawal.getResidentId())
                .propertyCompanyId(withdrawal.getPropertyCompanyId())
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

    private LocalDateTime parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            if (value.length() > 10) {
                return LocalDateTime.parse(value,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            return java.time.LocalDate.parse(value).atStartOfDay();
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式不正确，支持：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss");
        }
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
