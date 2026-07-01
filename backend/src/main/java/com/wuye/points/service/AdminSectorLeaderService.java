package com.wuye.points.service;

import com.wuye.points.dto.admin.sectorleader.AppointSectorLeaderRequest;
import com.wuye.points.dto.admin.sectorleader.SectorLeaderDetailResponse;
import com.wuye.points.dto.admin.sectorleader.SectorLeaderListItemResponse;
import com.wuye.points.dto.admin.sectorleader.UpdateSectorLeaderRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.repository.spec.SectorLeaderSpecifications;
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
public class AdminSectorLeaderService {

    private static final Set<String> LEADER_STATUS = Set.of("active", "inactive");
    private static final Set<String> SECTORS = Set.of(
            "elderly", "medical", "education", "cleaning", "canteen", "pet", "life_service");
    private static final BigDecimal DEFAULT_SECTOR_LEADER_RATE = new BigDecimal("0.1000");

    private final SectorLeaderRepository sectorLeaderRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final DistributionConverter distributionConverter;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<SectorLeaderListItemResponse> listSectorLeaders(
            int page,
            int pageSize,
            String sector,
            String coordinatorId,
            String status,
            String propertyCompanyId) {
        permissionGuard.requirePermissionOrAdmin("sector.read");
        validateStatus(status);
        validateSector(sector);
        if (StringUtils.hasText(coordinatorId)) {
            adminSupport.validateIdPrefix(coordinatorId, "coo_");
        }

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<SectorLeader> leaderPage = sectorLeaderRepository.findAll(
                SectorLeaderSpecifications.withFilters(
                        scopedPropertyCompanyId, sector, coordinatorId, status),
                pageable);

        Map<String, Resident> residentMap = loadResidents(leaderPage.getContent().stream()
                .map(SectorLeader::getResidentId)
                .toList());

        List<SectorLeaderListItemResponse> list = leaderPage.getContent().stream()
                .map(leader -> toListItem(leader, residentMap.get(leader.getResidentId())))
                .toList();

        return PageResponse.of(list, page, pageSize, leaderPage.getTotalElements());
    }

    @Transactional
    public SectorLeaderDetailResponse appointSectorLeader(AppointSectorLeaderRequest request) {
        permissionGuard.requirePermissionOrAdmin("sector.manage");
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");
        validateSector(request.getSector());

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        String propertyCompanyId = resident.getPropertyCompanyId();

        sectorLeaderRepository.findByResidentIdAndPropertyCompanyIdAndSector(
                        resident.getId(), propertyCompanyId, request.getSector())
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是此板块负责人");
                });

        Coordinator coordinator = resolveCoordinator(request.getCoordinatorId(), propertyCompanyId);

        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        SectorLeader leader = new SectorLeader();
        leader.setId(idGenerator.generateId("sl"));
        leader.setResidentId(resident.getId());
        leader.setPropertyCompanyId(propertyCompanyId);
        leader.setSector(request.getSector());
        leader.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company));
        leader.setCoordinatorId(coordinator != null ? coordinator.getId() : null);
        leader.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        validateStatus(leader.getStatus());
        sectorLeaderRepository.save(leader);

        return toDetail(leader, resident);
    }

    @Transactional(readOnly = true)
    public SectorLeaderDetailResponse getSectorLeader(String id) {
        permissionGuard.requirePermissionOrAdmin("sector.read");
        adminSupport.validateIdPrefix(id, "sl_");

        SectorLeader leader = getAccessibleSectorLeader(id);
        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    @Transactional
    public SectorLeaderDetailResponse updateSectorLeader(String id, UpdateSectorLeaderRequest request) {
        permissionGuard.requirePermissionOrAdmin("sector.manage");
        adminSupport.validateIdPrefix(id, "sl_");

        SectorLeader leader = getAccessibleSectorLeader(id);
        PropertyCompany company = propertyCompanyRepository.findById(leader.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        if (request.getSector() != null) {
            validateSector(request.getSector());
            leader.setSector(request.getSector());
        }
        if (request.getCommissionRate() != null) {
            leader.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company));
        }
        if (request.getCoordinatorId() != null) {
            Coordinator coordinator = resolveCoordinator(request.getCoordinatorId(), leader.getPropertyCompanyId());
            leader.setCoordinatorId(coordinator != null ? coordinator.getId() : null);
        }
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            leader.setStatus(request.getStatus());
        }

        sectorLeaderRepository.save(leader);
        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    @Transactional
    public SectorLeaderDetailResponse revokeSectorLeader(String id, String reason) {
        permissionGuard.requirePermissionOrAdmin("sector.manage");
        adminSupport.validateIdPrefix(id, "sl_");
        if (!StringUtils.hasText(reason)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "reason不能为空");
        }

        SectorLeader leader = getAccessibleSectorLeader(id);
        leader.setStatus("inactive");
        sectorLeaderRepository.save(leader);

        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    private SectorLeader getAccessibleSectorLeader(String id) {
        SectorLeader leader = sectorLeaderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.SECTOR_LEADER_NOT_FOUND));
        adminSupport.assertSameProperty(leader.getPropertyCompanyId());
        return leader;
    }

    private Coordinator resolveCoordinator(String coordinatorId, String propertyCompanyId) {
        if (!StringUtils.hasText(coordinatorId)) {
            return coordinatorRepository
                    .findFirstByPropertyCompanyIdAndStatusOrderByCommissionRateDesc(propertyCompanyId, "active")
                    .orElse(null);
        }
        adminSupport.validateIdPrefix(coordinatorId, "coo_");
        Coordinator coordinator = coordinatorRepository.findByIdAndPropertyCompanyId(coordinatorId, propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.COORDINATOR_NOT_FOUND));
        if (!"active".equals(coordinator.getStatus())) {
            throw new BizException(ErrorCode.COORDINATOR_NOT_FOUND, "统筹未激活");
        }
        return coordinator;
    }

    private BigDecimal resolveCommissionRate(BigDecimal requestedRate, PropertyCompany company) {
        if (requestedRate != null) {
            return requestedRate;
        }
        return company.getSectorLeaderRate() != null
                ? company.getSectorLeaderRate()
                : DEFAULT_SECTOR_LEADER_RATE;
    }

    private SectorLeaderListItemResponse toListItem(SectorLeader leader, Resident resident) {
        long individualLeaderCount = individualLeaderRepository.countBySectorLeaderIdAndStatus(
                leader.getId(), "active");
        BigDecimal totalEarnings = distributionRecordRepository.sumSectorLeaderShareBySectorLeaderId(leader.getId());

        return SectorLeaderListItemResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .sector(leader.getSector())
                .sectorName(distributionConverter.sectorLabel(leader.getSector()))
                .commissionRate(leader.getCommissionRate())
                .coordinatorId(leader.getCoordinatorId())
                .individualLeaderCount(individualLeaderCount)
                .totalEarnings(totalEarnings)
                .status(leader.getStatus())
                .appointedAt(DateTimeUtil.toIsoUtc(leader.getCreatedAt()))
                .build();
    }

    private SectorLeaderDetailResponse toDetail(SectorLeader leader, Resident resident) {
        SectorLeaderListItemResponse listItem = toListItem(leader, resident);
        return SectorLeaderDetailResponse.builder()
                .id(listItem.getId())
                .residentId(listItem.getResidentId())
                .residentName(listItem.getResidentName())
                .phone(listItem.getPhone())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .sector(listItem.getSector())
                .sectorName(listItem.getSectorName())
                .commissionRate(listItem.getCommissionRate())
                .coordinatorId(listItem.getCoordinatorId())
                .individualLeaderCount(listItem.getIndividualLeaderCount())
                .totalEarnings(listItem.getTotalEarnings())
                .status(listItem.getStatus())
                .appointedAt(listItem.getAppointedAt())
                .createdAt(DateTimeUtil.toIsoUtc(leader.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(leader.getUpdatedAt()))
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

    private void validateSector(String sector) {
        if (StringUtils.hasText(sector) && !SECTORS.contains(sector)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "sector不合法");
        }
    }
}
