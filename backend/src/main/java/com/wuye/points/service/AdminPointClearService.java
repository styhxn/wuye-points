package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.pointclear.PointClearRecordItemResponse;
import com.wuye.points.dto.admin.pointclear.RunPointClearRequest;
import com.wuye.points.dto.admin.pointclear.RunPointClearResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.AdminPointRecordSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPointClearService {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");

    private final PropertyFeeRepository propertyFeeRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PointRecordRepository pointRecordRepository;
    private final AdminPointPoolService adminPointPoolService;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final OperationLogService operationLogService;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public RunPointClearResponse run(RunPointClearRequest request) {
        permissionGuard.requirePermissionWithFallback("point.clear_run", "points.input", "data.view");

        LocalDate clearDate = adminSupport.parseDate(request.getClearDate());
        if (clearDate == null) {
            throw new BizException(ErrorCode.PARAM_MISSING, "clearDate不能为空");
        }
        LocalDate lookbackDate = clearDate.minusYears(2);
        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(request.getPropertyCompanyId());
        if (!StringUtils.hasText(scopedPropertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }

        PropertyCompany company = propertyCompanyRepository.findById(scopedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        if (!Boolean.TRUE.equals(company.getTwoYearClearEnabled())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该物业公司未启用2年欠费清零");
        }

        Set<String> eligibleResidentIds = findEligibleResidentIds(
                scopedPropertyCompanyId, lookbackDate, request.getResidentIds());

        BigDecimal rate = resolvePointToFeeRate(company);
        int processedCount = 0;
        int totalClearedPoints = 0;
        BigDecimal clearedToPool = BigDecimal.ZERO;
        String operatorId = UserContext.currentUserId();

        for (String residentId : eligibleResidentIds) {
            Resident resident = residentRepository.findById(residentId).orElse(null);
            if (resident == null || !scopedPropertyCompanyId.equals(resident.getPropertyCompanyId())) {
                continue;
            }
            int balance = defaultPointBalance(resident);
            if (balance <= 0) {
                continue;
            }

            processedCount++;
            totalClearedPoints += balance;
            BigDecimal poolAmount = BigDecimal.valueOf(balance)
                    .multiply(rate)
                    .setScale(2, RoundingMode.HALF_UP);
            clearedToPool = clearedToPool.add(poolAmount);

            if (!dryRun) {
                resident.setPointBalance(0);
                residentRepository.save(resident);

                PointRecord record = new PointRecord();
                record.setId(idGenerator.generateId("pt"));
                record.setResidentId(resident.getId());
                record.setFamilyId(resident.getFamilyId());
                record.setPropertyCompanyId(resident.getPropertyCompanyId());
                record.setPointType("expire");
                record.setAmount(-balance);
                record.setBalance(0);
                record.setSource("fee_clear");
                record.setSourceId(null);
                record.setDescription("2年欠费积分清零");
                record.setOperatorId(operatorId);
                pointRecordRepository.save(record);

                adminPointPoolService.addToPool(
                        scopedPropertyCompanyId,
                        poolAmount,
                        "fee_clear",
                        record.getId(),
                        "2年欠费积分清零入池",
                        operatorId);
            }
        }

        if (!dryRun) {
            operationLogService.log("point_clear", "run", "point_clear_task", idGenerator.generateId("task"),
                    null, Map.of(
                            "clearDate", request.getClearDate(),
                            "processedCount", processedCount,
                            "totalClearedPoints", totalClearedPoints));
        }

        return RunPointClearResponse.builder()
                .taskId(idGenerator.generateId("task"))
                .clearDate(clearDate.toString())
                .lookbackDate(lookbackDate.toString())
                .dryRun(dryRun)
                .processedCount(processedCount)
                .totalClearedPoints(totalClearedPoints)
                .clearedToPool(clearedToPool)
                .status("completed")
                .executedAt(DateTimeUtil.nowIsoUtc())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PointClearRecordItemResponse> listRecords(
            int page,
            int pageSize,
            String residentId,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermissionWithFallback("point.clear_run", "points.input", "data.view");

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<PointRecord> recordPage = pointRecordRepository.findAll(
                AdminPointRecordSpecifications.feeClearRecords(
                        scopedPropertyCompanyId, residentId, start, end),
                pageable);

        Map<String, Resident> residentMap = resolveResidents(recordPage.getContent());
        PropertyCompany company = StringUtils.hasText(scopedPropertyCompanyId)
                ? propertyCompanyRepository.findById(scopedPropertyCompanyId).orElse(null)
                : null;
        BigDecimal rate = company != null ? resolvePointToFeeRate(company) : DEFAULT_POINT_TO_FEE_RATE;

        List<PointClearRecordItemResponse> list = recordPage.getContent().stream()
                .map(record -> {
                    int clearedPoints = Math.abs(record.getAmount());
                    BigDecimal equivalent = BigDecimal.valueOf(clearedPoints)
                            .multiply(rate)
                            .setScale(2, RoundingMode.HALF_UP);
                    Resident resident = residentMap.get(record.getResidentId());
                    return PointClearRecordItemResponse.builder()
                            .id(record.getId())
                            .residentId(record.getResidentId())
                            .residentName(resident != null ? resident.getName() : null)
                            .clearedPoints(clearedPoints)
                            .equivalentAmount(equivalent)
                            .poolInAmount(equivalent)
                            .operatorId(record.getOperatorId())
                            .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                            .build();
                })
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    private Set<String> findEligibleResidentIds(
            String propertyCompanyId,
            LocalDate lookbackDate,
            List<String> requestedResidentIds) {
        Specification<PropertyFee> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("dueDate"), lookbackDate));
            predicates = cb.and(predicates, cb.notEqual(root.get("paymentStatus"), "paid"));
            return predicates;
        };

        List<PropertyFee> fees = propertyFeeRepository.findAll(spec);
        Set<String> residentIds = fees.stream()
                .map(PropertyFee::getResidentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        if (requestedResidentIds != null && !requestedResidentIds.isEmpty()) {
            Set<String> requested = new HashSet<>(requestedResidentIds);
            residentIds.retainAll(requested);
        }
        return residentIds;
    }

    private Map<String, Resident> resolveResidents(List<PointRecord> records) {
        Set<String> residentIds = records.stream()
                .map(PointRecord::getResidentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Resident> map = new HashMap<>();
        if (!residentIds.isEmpty()) {
            residentRepository.findAllById(residentIds).forEach(r -> map.put(r.getId(), r));
        }
        return map;
    }

    private BigDecimal resolvePointToFeeRate(PropertyCompany company) {
        BigDecimal rate = company.getPointToFeeRate();
        if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
            return rate;
        }
        return DEFAULT_POINT_TO_FEE_RATE;
    }

    private int defaultPointBalance(Resident resident) {
        return resident.getPointBalance() != null ? resident.getPointBalance() : 0;
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
