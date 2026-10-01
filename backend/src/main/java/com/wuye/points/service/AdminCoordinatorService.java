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
import com.wuye.points.repository.MerchantRepository;
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
    private final MerchantRepository merchantRepository;
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
        if (StringUtils.hasText(request.getDescription())) {
            coordinator.setDescription(request.getDescription().trim());
        }
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
        if (request.getDescription() != null) {
            coordinator.setDescription(request.getDescription().trim());
        }

        coordinatorRepository.save(coordinator);
        Resident resident = residentRepository.findById(coordinator.getResidentId()).orElse(null);
        return toDetail(coordinator, resident);
    }

    @Transactional
    public CoordinatorDetailResponse revokeCoordinator(String id, String reason) {
        permissionGuard.requirePermissionOrAdmin("coordinator.manage");
        adminSupport.validateIdPrefix(id, "coo_");
        // reason 仅为备注，前端可不传（2026-09-07 前端问题 A）

        Coordinator coordinator = getAccessibleCoordinator(id);

        // 层级约束：存在启用的下级板块负责人时不允许撤销，返回明确 400（避免前端拿到 500/级联错误）
        long activeSectorLeaders = sectorLeaderRepository.countByCoordinatorIdAndStatus(
                coordinator.getId(), "active");
        if (activeSectorLeaders > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "该统筹名下仍有 " + activeSectorLeaders + " 名启用中的板块负责人，请先撤销或转移板块负责人后再撤销统筹");
        }

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
        String propertyCompanyName = propertyCompanyRepository.findById(coordinator.getPropertyCompanyId())
                .map(PropertyCompany::getName)
                .orElse(null);
        // 商家数口径：该物业公司下挂接商家的数量（merchant_property_configs 已配置为口径前置条件，
        // 未挂接商家无法接单，不计入管辖范围）
        long merchantCount = merchantRepository.countByPropertyCompanyId(coordinator.getPropertyCompanyId());
        String phone = resident != null ? resident.getPhone() : null;
        String createdAtText = DateTimeUtil.toIsoUtc(coordinator.getCreatedAt());

        return CoordinatorListItemResponse.builder()
                .id(coordinator.getId())
                .residentId(coordinator.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .residentPhone(phone)
                .phone(phone)
                .propertyCompanyId(coordinator.getPropertyCompanyId())
                .propertyCompanyName(propertyCompanyName)
                .description(coordinator.getDescription())
                .commissionRate(coordinator.getCommissionRate())
                .sectorCount(sectorLeaderCount)
                .sectorLeaderCount(sectorLeaderCount)
                .individualLeaderCount(individualLeaderCount)
                .merchantCount(merchantCount)
                .totalEarnings(totalEarnings)
                .status(coordinator.getStatus())
                .createdAt(createdAtText)
                .appointedAt(createdAtText)
                .build();
    }

    private CoordinatorDetailResponse toDetail(Coordinator coordinator, Resident resident) {
        CoordinatorListItemResponse listItem = toListItem(coordinator, resident);
        return CoordinatorDetailResponse.builder()
                .id(listItem.getId())
                .residentId(listItem.getResidentId())
                .residentName(listItem.getResidentName())
                .residentPhone(listItem.getResidentPhone())
                .phone(listItem.getPhone())
                .propertyCompanyId(listItem.getPropertyCompanyId())
                .propertyCompanyName(listItem.getPropertyCompanyName())
                .description(listItem.getDescription())
                .commissionRate(listItem.getCommissionRate())
                .sectorCount(listItem.getSectorCount())
                .sectorLeaderCount(listItem.getSectorLeaderCount())
                .individualLeaderCount(listItem.getIndividualLeaderCount())
                .merchantCount(listItem.getMerchantCount())
                .totalEarnings(listItem.getTotalEarnings())
                .status(listItem.getStatus())
                .createdAt(listItem.getCreatedAt())
                .appointedAt(listItem.getAppointedAt())
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
