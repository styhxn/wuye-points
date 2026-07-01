package com.wuye.points.service;

import com.wuye.points.dto.admin.operationlog.OperationLogItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.OperationLog;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.OperationLogRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.OperationLogSpecifications;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;

    @Transactional(readOnly = true)
    public PageResponse<OperationLogItemResponse> list(
            int page,
            int pageSize,
            String operatorId,
            String module,
            String action,
            String targetType,
            String targetId,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermissionWithFallback("log.read", "data.view");

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<OperationLog> logPage = operationLogRepository.findAll(
                OperationLogSpecifications.withFilters(
                        scopedPropertyCompanyId,
                        operatorId,
                        module,
                        action,
                        targetType,
                        targetId,
                        start,
                        end),
                pageable);

        Map<String, Resident> operatorMap = resolveOperators(logPage.getContent());
        Map<String, PropertyCompany> companyMap = resolveCompanies(logPage.getContent());

        List<OperationLogItemResponse> list = logPage.getContent().stream()
                .map(log -> toItem(log, operatorMap, companyMap))
                .toList();

        return PageResponse.of(list, page, pageSize, logPage.getTotalElements());
    }

    private OperationLogItemResponse toItem(
            OperationLog log,
            Map<String, Resident> operatorMap,
            Map<String, PropertyCompany> companyMap) {
        Resident operator = operatorMap.get(log.getOperatorId());
        PropertyCompany company = companyMap.get(log.getPropertyCompanyId());

        return OperationLogItemResponse.builder()
                .id(log.getId())
                .operatorId(log.getOperatorId())
                .operatorName(operator != null ? operator.getName() : null)
                .operatorRole(operator != null ? operator.getRole() : null)
                .propertyCompanyId(log.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .module(log.getModule())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .beforeData(log.getBeforeData())
                .afterData(log.getAfterData())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(DateTimeUtil.toIsoUtc(log.getCreatedAt()))
                .build();
    }

    private Map<String, Resident> resolveOperators(List<OperationLog> logs) {
        Set<String> operatorIds = logs.stream()
                .map(OperationLog::getOperatorId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Resident> map = new HashMap<>();
        if (!operatorIds.isEmpty()) {
            residentRepository.findAllById(operatorIds).forEach(r -> map.put(r.getId(), r));
        }
        return map;
    }

    private Map<String, PropertyCompany> resolveCompanies(List<OperationLog> logs) {
        Set<String> companyIds = logs.stream()
                .map(OperationLog::getPropertyCompanyId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, PropertyCompany> map = new HashMap<>();
        if (!companyIds.isEmpty()) {
            propertyCompanyRepository.findAllById(companyIds).forEach(c -> map.put(c.getId(), c));
        }
        return map;
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
