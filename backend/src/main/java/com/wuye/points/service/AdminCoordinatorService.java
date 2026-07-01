package com.wuye.points.service;

import com.wuye.points.dto.admin.coordinator.AppointCoordinatorRequest;
import com.wuye.points.dto.admin.coordinator.CoordinatorDetailResponse;
import com.wuye.points.dto.admin.coordinator.CoordinatorListItemResponse;
import com.wuye.points.dto.admin.coordinator.UpdateCoordinatorRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.repository.spec.CoordinatorSpecifications;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCoordinatorService {

    private static final Set<String> LEADER_STATUS = Set.of("active", "inactive");
    private static final BigDecimal DEFAULT_COORDINATOR_SHARE_RATE = new BigDecimal("0.7000");

    private final CoordinatorRepository coordinatorRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<CoordinatorListItemResponse> listCoordinators(
            int page,
            int pageSize,
            String keyword,
            String status,
            String propertyCompanyId) {
        permissionGuard.requirePermissionOrAdmin("coordinator.read");
        validateStatus(status);

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<Coordinator> coordinatorPage = coordinatorRepository.findAll(
                CoordinatorSpecifications.withFilters(scopedPropertyCompanyId, status, keyword),
                pageable);

        Map<String, Resident> residentMap = loadResidents(coordinatorPage.getContent().stream()
                .map(Coordinator::getResidentId)
                .toList());

        List<CoordinatorListItemResponse> list = coordinatorPage.getContent().stream()
                .map(coordinator -> toListItem(coordinator, residentMap.get(coordinator.getResidentId())))
                .toList();

        return PageResponse.of(list, page, pageSize, coordinatorPage.getTotalElements());
    }

    @Transactional
    public CoordinatorDetailResponse appointCoordinator(AppointCoordinatorRequest request) {
        permissionGuard.requirePermissionOrAdmin("coordinator.manage");
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        String propertyCompanyId = resident.getPropertyCompanyId();

        coordinatorRepository.findByResidentIdAndPropertyCompanyId(resident.getId(), propertyCompanyId)
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是统筹");
                });

        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        Coordinator coordinator = new Coordinator();
        coordinator.setId(idGenerator.generateId("coo"));
        coordinator.setResidentId(resident.getId());
        coordinator.setPropertyCompanyId(propertyCompanyId);
        coordinator.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company));
        coordinator.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        validateStatus(coordinator.getStatus());
        coordinatorRepository.save(coordinator);

        if (!"platform_admin".equals(resident.getRole())) {
            resident.setRole("coordinator");
            residentRepository.save(resident);
        }

        return toDetail(coordinator, resident);
    }

    @Transactional(readOnly = true)
    public CoordinatorDetailResponse getCoordinator(String id) {
        permissionGuard.requirePermissionOrAdmin("coordinator.read");
        adminSupport.validateIdPrefix(id, "coo_");

        Coordinator coordinator = getAccessibleCoordinator(id);
        Resident resident = residentRepository.findById(coordinator.getResidentId()).orElse(null);
        return toDetail(coordinator, resident);
    }

    @Transactional
    public CoordinatorDetailResponse updateCoordinator(String id, UpdateCoordinatorRequest request) {
        permissionGuard.requirePermissionOrAdmin("coordinator.manage");
        adminSupport.validateIdPrefix(id, "coo_");

        Coordinator coordinator = getAccessibleCoordinator(id);
        PropertyCompany company = propertyCompanyRepository.findById(coordinator.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        if (request.getCommissionRate() != null) {
            coordinator.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company));
        }
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            coordinator.setStatus(request.getStatus());
        }

        coordinatorRepository.save(coordinator);
        Resident resident = residentRepository.findById(coordinator.getResidentId()).orElse(null);
        return toDetail(coordinator, resident);
    }

    @Transactional
    public CoordinatorDetailResponse revokeCoordinator(String id, String reason) {
        permissionGuard.requirePermissionOrAdmin("coordinator.manage");
        adminSupport.validateIdPrefix(id, "coo_");
        if (!StringUtils.hasText(reason)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "reason不能为空");
        }

        Coordinator coordinator = getAccessibleCoordinator(id);
        coordinator.setStatus("inactive");
        coordinatorRepository.save(coordinator);

        revertResidentRoleIfNeeded(coordinator.getResidentId(), coordinator.getPropertyCompanyId());

        Resident resident = residentRepository.findById(coordinator.getResidentId()).orElse(null);
        return toDetail(coordinator, resident);
    }

    private Coordinator getAccessibleCoordinator(String id) {
        Coordinator coordinator = coordinatorRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COORDINATOR_NOT_FOUND));
        adminSupport.assertSameProperty(coordinator.getPropertyCompanyId());
        return coordinator;
    }

    private void revertResidentRoleIfNeeded(String residentId, String propertyCompanyId) {
        Resident resident = residentRepository.findById(residentId).orElse(null);
        if (resident == null || "platform_admin".equals(resident.getRole())) {
            return;
        }
        boolean hasActiveCoordinator = coordinatorRepository
                .findByResidentIdAndPropertyCompanyId(residentId, propertyCompanyId)
                .map(item -> "active".equals(item.getStatus()))
                .orElse(false);
        if (!hasActiveCoordinator && "coordinator".equals(resident.getRole())) {
            resident.setRole("resident");
            residentRepository.save(resident);
        }
    }

    private BigDecimal resolveCommissionRate(BigDecimal requestedRate, PropertyCompany company) {
        if (requestedRate != null) {
            return requestedRate;
        }
        return company.getCoordinatorShareRate() != null
                ? company.getCoordinatorShareRate()
                : DEFAULT_COORDINATOR_SHARE_RATE;
    }

    private CoordinatorListItemResponse toListItem(Coordinator coordinator, Resident resident) {
        long sectorLeaderCount = sectorLeaderRepository.countByCoordinatorIdAndStatus(
                coordinator.getId(), "active");
        long individualLeaderCount = sectorLeaderRepository.findByCoordinatorId(coordinator.getId()).stream()
                .mapToLong(sectorLeader -> individualLeaderRepository.countBySectorLeaderIdAndStatus(
                        sectorLeader.getId(), "active"))
                .sum();
        BigDecimal totalEarnings = distributionRecordRepository.sumCoordinatorShareByCoordinatorId(coordinator.getId());

        return CoordinatorListItemResponse.builder()
                .id(coordinator.getId())
                .residentId(coordinator.getResidentId())
                .name(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .commissionRate(coordinator.getCommissionRate())
                .sectorLeaderCount(sectorLeaderCount)
                .individualLeaderCount(individualLeaderCount)
                .totalEarnings(totalEarnings)
                .status(coordinator.getStatus())
                .appointedAt(DateTimeUtil.toIsoUtc(coordinator.getCreatedAt()))
                .build();
    }

    private CoordinatorDetailResponse toDetail(Coordinator coordinator, Resident resident) {
        CoordinatorListItemResponse listItem = toListItem(coordinator, resident);
        return CoordinatorDetailResponse.builder()
                .id(listItem.getId())
                .residentId(listItem.getResidentId())
                .residentName(listItem.getName())
                .phone(listItem.getPhone())
                .propertyCompanyId(coordinator.getPropertyCompanyId())
                .commissionRate(listItem.getCommissionRate())
                .sectorLeaderCount(listItem.getSectorLeaderCount())
                .individualLeaderCount(listItem.getIndividualLeaderCount())
                .totalEarnings(listItem.getTotalEarnings())
                .status(listItem.getStatus())
                .appointedAt(listItem.getAppointedAt())
                .createdAt(DateTimeUtil.toIsoUtc(coordinator.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(coordinator.getUpdatedAt()))
                .build();
    }

    private Map<String, Resident> loadResidents(List<String> residentIds) {
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, resident -> resident));
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !LEADER_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }
    }
}
