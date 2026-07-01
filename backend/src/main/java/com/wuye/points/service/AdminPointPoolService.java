package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.pointpool.AdjustPointPoolRequest;
import com.wuye.points.dto.admin.pointpool.AdjustPointPoolResponse;
import com.wuye.points.dto.admin.pointpool.PointPoolBalanceResponse;
import com.wuye.points.dto.admin.pointpool.PointPoolRecordItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PointPoolRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PointPoolRecordRepository;
import com.wuye.points.repository.PointPoolRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PointPoolRecordSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPointPoolService {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");
    private static final Set<String> VALID_POOL_TYPES = Set.of("pool_in", "pool_out");
    private static final Set<String> VALID_SOURCES = Set.of(
            "exchange_diff", "coin_expired", "fee_clear", "spend", "manual_adjust", "pool_diff");

    private final PointPoolRepository pointPoolRepository;
    private final PointPoolRecordRepository pointPoolRecordRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ResidentRepository residentRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final OperationLogService operationLogService;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PointPoolBalanceResponse getBalance(String propertyCompanyId) {
        permissionGuard.requirePermissionWithFallback("pool.read", "points.input", "data.view");
        String scopedPropertyCompanyId = resolveRequiredPropertyCompanyId(propertyCompanyId);
        PointPool pool = getPoolOrThrow(scopedPropertyCompanyId);
        PropertyCompany company = propertyCompanyRepository.findById(scopedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        BigDecimal balance = defaultDecimal(pool.getBalance());
        BigDecimal rate = resolvePointToFeeRate(company);

        return PointPoolBalanceResponse.builder()
                .propertyCompanyId(scopedPropertyCompanyId)
                .propertyCompanyName(company.getName())
                .balance(balance)
                .totalIn(defaultDecimal(pool.getTotalIn()))
                .totalOut(defaultDecimal(pool.getTotalOut()))
                .equivalentAmount(balance.multiply(rate).setScale(2, RoundingMode.HALF_UP))
                .updatedAt(DateTimeUtil.toIsoUtc(pool.getUpdatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PointPoolRecordItemResponse> listRecords(
            int page,
            int pageSize,
            String poolType,
            String source,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermissionWithFallback("pool.read", "points.input", "data.view");
        String scopedPropertyCompanyId = resolveRequiredPropertyCompanyId(propertyCompanyId);
        validatePoolType(poolType);
        validateSource(source);

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<PointPoolRecord> recordPage = pointPoolRecordRepository.findAll(
                PointPoolRecordSpecifications.withFilters(
                        scopedPropertyCompanyId, poolType, source, start, end),
                pageable);

        Map<String, String> operatorNames = resolveOperatorNames(recordPage.getContent());
        List<PointPoolRecordItemResponse> list = recordPage.getContent().stream()
                .map(record -> toRecordItem(record, operatorNames.get(record.getOperatorId())))
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    @Transactional
    public AdjustPointPoolResponse adjust(AdjustPointPoolRequest request) {
        permissionGuard.requirePermissionWithFallback("pool.adjust", "points.input", "data.view");
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "调整金额不能为0");
        }

        String scopedPropertyCompanyId = resolveRequiredPropertyCompanyId(request.getPropertyCompanyId());
        PointPool pool = getPoolOrThrow(scopedPropertyCompanyId);

        BigDecimal absAmount = request.getAmount().abs();
        String poolType;
        BigDecimal newBalance;
        BigDecimal newTotalIn = defaultDecimal(pool.getTotalIn());
        BigDecimal newTotalOut = defaultDecimal(pool.getTotalOut());

        if (request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            poolType = "pool_in";
            newBalance = defaultDecimal(pool.getBalance()).add(absAmount);
            newTotalIn = newTotalIn.add(absAmount);
        } else {
            poolType = "pool_out";
            newBalance = defaultDecimal(pool.getBalance()).subtract(absAmount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.POINT_POOL_INSUFFICIENT);
            }
            newTotalOut = newTotalOut.add(absAmount);
        }

        pool.setBalance(newBalance);
        pool.setTotalIn(newTotalIn);
        pool.setTotalOut(newTotalOut);
        pointPoolRepository.save(pool);

        PointPoolRecord record = new PointPoolRecord();
        record.setId(idGenerator.generateId("ppr"));
        record.setPropertyCompanyId(scopedPropertyCompanyId);
        record.setPoolType(poolType);
        record.setAmount(absAmount);
        record.setBalance(newBalance);
        record.setSource("manual_adjust");
        record.setDescription(request.getDescription());
        record.setOperatorId(UserContext.currentUserId());
        pointPoolRecordRepository.save(record);

        operationLogService.log("point_pool", "adjust", "point_pool_record", record.getId(),
                null, Map.of("amount", request.getAmount(), "description", request.getDescription()));

        return AdjustPointPoolResponse.builder()
                .id(record.getId())
                .poolType(poolType)
                .amount(absAmount)
                .balance(newBalance)
                .description(request.getDescription())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    /**
     * 供其他管理模块调用：从积分池扣减
     */
    @Transactional
    public PointPoolRecord deductFromPool(String propertyCompanyId,
                                          BigDecimal amount,
                                          String source,
                                          String sourceId,
                                          String description,
                                          String operatorId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "扣减金额必须大于0");
        }
        PointPool pool = getPoolOrThrow(propertyCompanyId);
        BigDecimal newBalance = defaultDecimal(pool.getBalance()).subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.POINT_POOL_INSUFFICIENT);
        }
        pool.setBalance(newBalance);
        pool.setTotalOut(defaultDecimal(pool.getTotalOut()).add(amount));
        pointPoolRepository.save(pool);

        PointPoolRecord record = new PointPoolRecord();
        record.setId(idGenerator.generateId("ppr"));
        record.setPropertyCompanyId(propertyCompanyId);
        record.setPoolType("pool_out");
        record.setAmount(amount);
        record.setBalance(newBalance);
        record.setSource(source);
        record.setSourceId(sourceId);
        record.setDescription(description);
        record.setOperatorId(operatorId);
        return pointPoolRecordRepository.save(record);
    }

    /**
     * 供其他管理模块调用：向积分池入账
     */
    @Transactional
    public PointPoolRecord addToPool(String propertyCompanyId,
                                     BigDecimal amount,
                                     String source,
                                     String sourceId,
                                     String description,
                                     String operatorId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "入账金额必须大于0");
        }
        PointPool pool = getPoolOrThrow(propertyCompanyId);
        BigDecimal newBalance = defaultDecimal(pool.getBalance()).add(amount);
        pool.setBalance(newBalance);
        pool.setTotalIn(defaultDecimal(pool.getTotalIn()).add(amount));
        pointPoolRepository.save(pool);

        PointPoolRecord record = new PointPoolRecord();
        record.setId(idGenerator.generateId("ppr"));
        record.setPropertyCompanyId(propertyCompanyId);
        record.setPoolType("pool_in");
        record.setAmount(amount);
        record.setBalance(newBalance);
        record.setSource(source);
        record.setSourceId(sourceId);
        record.setDescription(description);
        record.setOperatorId(operatorId);
        return pointPoolRecordRepository.save(record);
    }

    private PointPool getPoolOrThrow(String propertyCompanyId) {
        return pointPoolRepository.findByPropertyCompanyId(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.POINT_POOL_NOT_FOUND));
    }

    private String resolveRequiredPropertyCompanyId(String requestedPropertyCompanyId) {
        String scoped = adminSupport.resolvePropertyCompanyId(requestedPropertyCompanyId);
        if (!StringUtils.hasText(scoped)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }
        adminSupport.validateIdPrefix(scoped, "pc");
        if (!propertyCompanyRepository.existsById(scoped)) {
            throw new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND);
        }
        return scoped;
    }

    private BigDecimal resolvePointToFeeRate(PropertyCompany company) {
        BigDecimal rate = company.getPointToFeeRate();
        if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
            return rate;
        }
        return DEFAULT_POINT_TO_FEE_RATE;
    }

    private PointPoolRecordItemResponse toRecordItem(PointPoolRecord record, String operatorName) {
        return PointPoolRecordItemResponse.builder()
                .id(record.getId())
                .propertyCompanyId(record.getPropertyCompanyId())
                .poolType(record.getPoolType())
                .source(record.getSource())
                .amount(record.getAmount())
                .balance(record.getBalance())
                .description(record.getDescription())
                .sourceId(record.getSourceId())
                .operatorId(record.getOperatorId())
                .operatorName(operatorName)
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    private Map<String, String> resolveOperatorNames(List<PointPoolRecord> records) {
        Set<String> operatorIds = records.stream()
                .map(PointPoolRecord::getOperatorId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (operatorIds.isEmpty()) {
            return Map.of();
        }
        Map<String, String> names = new HashMap<>();
        residentRepository.findAllById(operatorIds).forEach(resident ->
                names.put(resident.getId(), resident.getName()));
        return names;
    }

    private void validatePoolType(String poolType) {
        if (StringUtils.hasText(poolType) && !VALID_POOL_TYPES.contains(poolType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "poolType值无效");
        }
    }

    private void validateSource(String source) {
        if (StringUtils.hasText(source) && !VALID_SOURCES.contains(source)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "source值无效");
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

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
