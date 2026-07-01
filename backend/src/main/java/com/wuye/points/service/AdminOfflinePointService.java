package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.offlinepoint.GrantOfflinePointRequest;
import com.wuye.points.dto.admin.offlinepoint.GrantOfflinePointResponse;
import com.wuye.points.dto.admin.offlinepoint.OfflinePointRecordItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.AdminPointRecordSpecifications;
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
public class AdminOfflinePointService {

    private static final Set<String> VALID_SOURCES = Set.of("manual", "offline_consume", "activity_reward");

    private final PointService pointService;
    private final AdminPointPoolService adminPointPoolService;
    private final PointRecordRepository pointRecordRepository;
    private final ResidentRepository residentRepository;
    private final FamilyRepository familyRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final OperationLogService operationLogService;

    @Transactional
    public GrantOfflinePointResponse grant(GrantOfflinePointRequest request) {
        permissionGuard.requirePermissionWithFallback("points.input", "data.view");
        if (!request.getResidentId().startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }
        if (!VALID_SOURCES.contains(request.getSource())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "source值无效");
        }

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());

        String operatorId = UserContext.currentUserId();
        String sourceId = StringUtils.hasText(request.getRelatedOrderId())
                ? request.getRelatedOrderId()
                : null;

        PointRecord record = pointService.earnPoints(
                resident,
                request.getPointAmount(),
                request.getSource(),
                sourceId,
                request.getDescription(),
                operatorId);

        adminPointPoolService.deductFromPool(
                resident.getPropertyCompanyId(),
                BigDecimal.valueOf(request.getPointAmount()),
                "spend",
                record.getId(),
                request.getDescription(),
                operatorId);

        operationLogService.log("offline_point", "grant", "point_record", record.getId(),
                null, Map.of(
                        "residentId", resident.getId(),
                        "pointAmount", request.getPointAmount(),
                        "source", request.getSource()));

        Integer familyBalanceAfter = null;
        if (StringUtils.hasText(resident.getFamilyId())) {
            familyBalanceAfter = familyRepository.findById(resident.getFamilyId())
                    .map(this::defaultFamilyPointBalance)
                    .orElse(null);
        }

        return GrantOfflinePointResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .pointAmount(request.getPointAmount())
                .balanceAfter(record.getBalance())
                .familyBalanceAfter(familyBalanceAfter)
                .source(request.getSource())
                .operatorId(operatorId)
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<OfflinePointRecordItemResponse> listRecords(
            int page,
            int pageSize,
            String residentId,
            String operatorId,
            String source,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermissionWithFallback("points.input", "data.view");

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<PointRecord> recordPage = pointRecordRepository.findAll(
                AdminPointRecordSpecifications.offlineGrantRecords(
                        scopedPropertyCompanyId, residentId, operatorId, source, start, end),
                pageable);

        Map<String, Resident> residentMap = resolveResidents(recordPage.getContent());
        Map<String, String> operatorNames = resolveOperatorNames(recordPage.getContent());

        List<OfflinePointRecordItemResponse> list = recordPage.getContent().stream()
                .map(record -> {
                    Resident resident = residentMap.get(record.getResidentId());
                    return OfflinePointRecordItemResponse.builder()
                            .id(record.getId())
                            .residentId(record.getResidentId())
                            .residentName(resident != null ? resident.getName() : null)
                            .pointAmount(record.getAmount())
                            .balanceAfter(record.getBalance())
                            .source(record.getSource())
                            .description(record.getDescription())
                            .operatorId(record.getOperatorId())
                            .operatorName(operatorNames.get(record.getOperatorId()))
                            .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                            .build();
                })
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
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

    private Map<String, String> resolveOperatorNames(List<PointRecord> records) {
        Set<String> operatorIds = records.stream()
                .map(PointRecord::getOperatorId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, String> names = new HashMap<>();
        if (!operatorIds.isEmpty()) {
            residentRepository.findAllById(operatorIds).forEach(r -> names.put(r.getId(), r.getName()));
        }
        return names;
    }

    private int defaultFamilyPointBalance(Family family) {
        return family.getPointBalance() != null ? family.getPointBalance() : 0;
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
