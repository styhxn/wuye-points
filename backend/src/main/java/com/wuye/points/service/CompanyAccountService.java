package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.companyaccount.CompanyAccountBalanceResponse;
import com.wuye.points.dto.companyaccount.CompanyAccountRecordItemResponse;
import com.wuye.points.entity.CompanyAccountRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CompanyAccountRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
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
import java.util.List;
import java.util.Set;

/**
 * 公司账户资金流服务（A10）
 * 用于记录物业领导账户资金流水：领导账户资金计入公司账户余额。
 */
@Service
@RequiredArgsConstructor
public class CompanyAccountService {

    private static final Set<String> SOURCES = Set.of("leader_share", "withdrawal_deposit", "manual");

    private final CompanyAccountRecordRepository recordRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public CompanyAccountBalanceResponse getBalance(String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);

        PropertyCompany company = propertyCompanyRepository.findById(scopedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        BigDecimal balance = company.getCompanyAccountBalance() != null
                ? company.getCompanyAccountBalance() : BigDecimal.ZERO;

        String lastUpdatedAt = recordRepository
                .findFirstByPropertyCompanyIdOrderByCreatedAtDesc(scopedPropertyCompanyId)
                .map(record -> DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .orElse(null);

        return CompanyAccountBalanceResponse.builder()
                .propertyCompanyId(company.getId())
                .propertyCompanyName(company.getName())
                .balance(balance)
                .lastUpdatedAt(lastUpdatedAt)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanyAccountRecordItemResponse> listRecords(
            String propertyCompanyId, int page, int pageSize) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);

        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<CompanyAccountRecord> recordPage = recordRepository
                .findByPropertyCompanyIdOrderByCreatedAtDesc(scopedPropertyCompanyId, pageable);

        List<CompanyAccountRecordItemResponse> list = recordPage.getContent().stream()
                .map(this::toItem)
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    /**
     * 资金注入（内部接口，给订单分成时调用）。
     * 领导账户资金计入公司账户余额。仅允许正数注入，负数调整请走 adminAdjust。
     */
    @Transactional
    public CompanyAccountRecord deposit(String propertyCompanyId, String orderId,
                                        BigDecimal amount, String source, String remark) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "amount必须大于0");
        }
        validateSource(source);

        // 幂等校验：同一 orderId + source 已处理过则直接返回
        if (StringUtils.hasText(orderId)
                && recordRepository.existsByOrderIdAndSource(orderId, source)) {
            return null;
        }

        // 校验公司存在
        propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        // 原子更新余额，避免并发读-改-写丢失更新
        propertyCompanyRepository.addCompanyAccountBalance(propertyCompanyId, amount);

        CompanyAccountRecord record = new CompanyAccountRecord();
        record.setId(idGenerator.generateId("car"));
        record.setPropertyCompanyId(propertyCompanyId);
        record.setOrderId(orderId);
        record.setAmount(amount);
        record.setSource(source);
        record.setRemark(remark);
        return recordRepository.save(record);
    }

    /**
     * 平台管理员手动调整公司账户余额（允许正负数）。
     */
    @Transactional
    public CompanyAccountRecordItemResponse adminAdjust(String propertyCompanyId, BigDecimal amount, String remark) {
        roleGuard.requireAnyRole("platform_admin");
        if (!StringUtils.hasText(propertyCompanyId) || !propertyCompanyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "amount不能为空");
        }

        // 校验公司存在
        propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        // 原子更新余额（允许正负数）
        propertyCompanyRepository.addCompanyAccountBalance(propertyCompanyId, amount);

        CompanyAccountRecord record = new CompanyAccountRecord();
        record.setId(idGenerator.generateId("car"));
        record.setPropertyCompanyId(propertyCompanyId);
        record.setOrderId(null);
        record.setAmount(amount);
        record.setSource("manual");
        record.setRemark(StringUtils.hasText(remark) ? remark : "平台管理员手动调整");
        CompanyAccountRecord saved = recordRepository.save(record);
        return toItem(saved);
    }

    private CompanyAccountRecordItemResponse toItem(CompanyAccountRecord record) {
        return CompanyAccountRecordItemResponse.builder()
                .id(record.getId())
                .propertyCompanyId(record.getPropertyCompanyId())
                .orderId(record.getOrderId())
                .amount(record.getAmount())
                .source(record.getSource())
                .remark(record.getRemark())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    private String resolveScopedPropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (!StringUtils.hasText(requestedPropertyCompanyId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
            }
            if (!requestedPropertyCompanyId.startsWith("pc_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
            }
            return requestedPropertyCompanyId;
        }
        // property_admin 限定本公司
        String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(currentPropertyCompanyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return currentPropertyCompanyId;
    }

    private void validateSource(String source) {
        if (!StringUtils.hasText(source) || !SOURCES.contains(source)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "source值无效");
        }
    }
}
