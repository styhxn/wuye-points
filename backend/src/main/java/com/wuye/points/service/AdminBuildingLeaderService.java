package com.wuye.points.service;

import com.wuye.points.dto.admin.buildingleader.AppointBuildingLeaderRequest;
import com.wuye.points.dto.admin.buildingleader.BuildingLeaderDetailResponse;
import com.wuye.points.dto.admin.buildingleader.BuildingLeaderListItemResponse;
import com.wuye.points.dto.admin.buildingleader.UpdateBuildingLeaderRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.BuildingLeaderSpecifications;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理端 · 单元楼长管理（物业公司选定/添加/删除楼长角色）
 *
 * <p>楼长与活动组无关，是与「小区 + 单元」绑定的推广角色：该单元内住户消费时按比例分成。
 * 订单创建时快照绑定楼长，分成定向从订单产生那一刻生效，后续变更楼长不影响已产生订单。
 */
@Service
@RequiredArgsConstructor
public class AdminBuildingLeaderService {

    private static final Set<String> LEADER_STATUS = Set.of("active", "inactive");
    private static final BigDecimal DEFAULT_BUILDING_LEADER_RATE = new BigDecimal("0.1000");

    private final BuildingLeaderRepository buildingLeaderRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final CommunityRepository communityRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<BuildingLeaderListItemResponse> listBuildingLeaders(
            int page,
            int pageSize,
            String keyword,
            String status,
            String communityId,
            String unit,
            String propertyCompanyId) {
        permissionGuard.requirePermissionOrAdmin("buildingLeader.read");
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        return listBuildingLeadersForScope(scopedPropertyCompanyId, page, pageSize, keyword, status, communityId, unit);
    }

    /**
     * 楼长列表查询核心逻辑（供管理端与物业经理工作台复用，权限校验由调用方完成）。
     */
    @Transactional(readOnly = true)
    public PageResponse<BuildingLeaderListItemResponse> listBuildingLeadersForScope(
            String propertyCompanyId,
            int page,
            int pageSize,
            String keyword,
            String status,
            String communityId,
            String unit) {
        validateStatus(status);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<BuildingLeader> leaderPage = buildingLeaderRepository.findAll(
                BuildingLeaderSpecifications.withFilters(
                        propertyCompanyId, status, communityId, unit, keyword),
                pageable);

        List<BuildingLeader> leaders = leaderPage.getContent();
        Map<String, Resident> residentMap = loadResidents(leaders.stream()
                .map(BuildingLeader::getResidentId).toList());
        Map<String, PropertyCompany> companyMap = loadCompanies(leaders.stream()
                .map(BuildingLeader::getPropertyCompanyId).collect(Collectors.toSet()));
        Map<String, Community> communityMap = loadCommunities(leaders.stream()
                .map(BuildingLeader::getCommunityId).collect(Collectors.toSet()));
        Map<String, BigDecimal> earningMap = loadEarnings(leaders.stream()
                .map(BuildingLeader::getId).toList());

        List<BuildingLeaderListItemResponse> list = leaders.stream()
                .map(leader -> toListItem(leader, residentMap.get(leader.getResidentId()),
                        companyMap.get(leader.getPropertyCompanyId()),
                        communityMap.get(leader.getCommunityId()),
                        earningMap.get(leader.getId())))
                .toList();

        return PageResponse.of(list, page, pageSize, leaderPage.getTotalElements());
    }

    @Transactional
    public BuildingLeaderDetailResponse appointBuildingLeader(AppointBuildingLeaderRequest request) {
        permissionGuard.requirePermissionOrAdmin("buildingLeader.manage");
        return appointBuildingLeaderForScope(request, null);
    }

    /**
     * 指定单元楼长核心逻辑（供管理端与物业经理工作台复用，权限校验由调用方完成）。
     *
     * @param scopedPropertyCompanyId 非空时限定目标住户必须属于该物业公司（物业经理工作台传入）；
     *                                为空时按管理端登录态做同物业校验（assertSameProperty）
     */
    @Transactional
    public BuildingLeaderDetailResponse appointBuildingLeaderForScope(
            AppointBuildingLeaderRequest request, String scopedPropertyCompanyId) {
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (StringUtils.hasText(scopedPropertyCompanyId)) {
            if (!scopedPropertyCompanyId.equals(resident.getPropertyCompanyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "该住户不属于当前物业公司");
            }
        } else {
            adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        }
        String propertyCompanyId = resident.getPropertyCompanyId();

        String communityId = StringUtils.hasText(request.getCommunityId())
                ? request.getCommunityId().trim() : resident.getCommunityId();
        if (!StringUtils.hasText(communityId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "该住户未关联小区，请指定communityId");
        }
        String unit = StringUtils.hasText(request.getUnit())
                ? request.getUnit().trim() : resident.getUnit();
        if (!StringUtils.hasText(unit)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "该住户未关联单元，请指定unit");
        }

        if (buildingLeaderRepository.existsByCommunityIdAndUnitAndStatus(communityId, unit, "active")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该小区该单元已存在启用中的楼长，请先撤销后再指派");
        }
        if (buildingLeaderRepository.existsByResidentIdAndPropertyCompanyIdAndStatus(
                resident.getId(), propertyCompanyId, "active")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是启用中的楼长");
        }

        BuildingLeader leader = new BuildingLeader();
        leader.setId(idGenerator.generateId("bl"));
        leader.setResidentId(resident.getId());
        leader.setPropertyCompanyId(propertyCompanyId);
        leader.setCommunityId(communityId);
        leader.setUnit(unit);
        leader.setCommissionRate(resolveRate(request.getCommissionRate()));
        leader.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        validateStatus(leader.getStatus());
        if (StringUtils.hasText(request.getDescription())) {
            leader.setDescription(request.getDescription().trim());
        }
        buildingLeaderRepository.save(leader);

        // 楼长是「住户 + 楼长」的复合身份：不改写 residents.role，住户功能保持可用，
        // 楼长身份由 building_leaders 表的 active 记录判定。
        return toDetail(leader, resident);
    }

    @Transactional(readOnly = true)
    public BuildingLeaderDetailResponse getBuildingLeader(String id) {
        permissionGuard.requirePermissionOrAdmin("buildingLeader.read");
        adminSupport.validateIdPrefix(id, "bl_");

        BuildingLeader leader = getAccessibleLeader(id);
        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    @Transactional
    public BuildingLeaderDetailResponse updateBuildingLeader(String id, UpdateBuildingLeaderRequest request) {
        permissionGuard.requirePermissionOrAdmin("buildingLeader.manage");
        adminSupport.validateIdPrefix(id, "bl_");

        BuildingLeader leader = getAccessibleLeader(id);

        if (request.getCommissionRate() != null) {
            leader.setCommissionRate(resolveRate(request.getCommissionRate()));
        }
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            leader.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            leader.setDescription(request.getDescription().trim());
        }

        buildingLeaderRepository.save(leader);
        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    @Transactional
    public BuildingLeaderDetailResponse revokeBuildingLeader(String id, String reason) {
        permissionGuard.requirePermissionOrAdmin("buildingLeader.manage");
        adminSupport.validateIdPrefix(id, "bl_");
        // reason 仅为备注，前端可不传

        BuildingLeader leader = getAccessibleLeader(id);
        leader.setStatus("inactive");
        if (StringUtils.hasText(reason)) {
            leader.setDescription(reason.trim());
        }
        buildingLeaderRepository.save(leader);
        // 楼长为「住户 + 楼长」复合身份，住户角色不因撤销而变更（本就未改写 residents.role）。

        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    private BuildingLeader getAccessibleLeader(String id) {
        BuildingLeader leader = buildingLeaderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.BUILDING_LEADER_NOT_FOUND));
        adminSupport.assertSameProperty(leader.getPropertyCompanyId());
        return leader;
    }

    private BigDecimal resolveRate(BigDecimal value) {
        if (value == null) {
            return DEFAULT_BUILDING_LEADER_RATE;
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "commissionRate需在 0~1 之间");
        }
        return value;
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !LEADER_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
    }

    private Map<String, Resident> loadResidents(List<String> residentIds) {
        if (residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, r -> r, (a, b) -> a));
    }

    private Map<String, PropertyCompany> loadCompanies(Set<String> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return propertyCompanyRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PropertyCompany::getId, c -> c, (a, b) -> a));
    }

    private Map<String, Community> loadCommunities(Set<String> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return communityRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Community::getId, c -> c, (a, b) -> a));
    }

    private Map<String, BigDecimal> loadEarnings(List<String> leaderIds) {
        if (leaderIds.isEmpty()) {
            return Map.of();
        }
        Map<String, BigDecimal> result = new HashMap<>();
        for (Object[] row : distributionRecordRepository.sumBuildingLeaderShareGrouped(leaderIds)) {
            if (row[0] != null) {
                result.put((String) row[0], row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO);
            }
        }
        return result;
    }

    private BuildingLeaderListItemResponse toListItem(BuildingLeader leader,
                                                     Resident resident,
                                                     PropertyCompany company,
                                                     Community community,
                                                     BigDecimal totalEarning) {
        String createdAt = DateTimeUtil.toIsoUtc(leader.getCreatedAt());
        return BuildingLeaderListItemResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .residentPhone(resident != null ? resident.getPhone() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(leader.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .communityId(leader.getCommunityId())
                .communityName(community != null ? community.getName() : null)
                .unit(leader.getUnit())
                .commissionRate(leader.getCommissionRate())
                .description(leader.getDescription())
                .totalEarning(totalEarning != null ? totalEarning : BigDecimal.ZERO)
                .status(leader.getStatus())
                .createdAt(createdAt)
                .appointedAt(createdAt)
                .build();
    }

    private BuildingLeaderDetailResponse toDetail(BuildingLeader leader, Resident resident) {
        PropertyCompany company = propertyCompanyRepository.findById(leader.getPropertyCompanyId()).orElse(null);
        Community community = StringUtils.hasText(leader.getCommunityId())
                ? communityRepository.findById(leader.getCommunityId()).orElse(null) : null;
        BigDecimal totalEarning = distributionRecordRepository
                .sumBuildingLeaderShareByBuildingLeaderId(leader.getId());
        String createdAt = DateTimeUtil.toIsoUtc(leader.getCreatedAt());
        return BuildingLeaderDetailResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .residentPhone(resident != null ? resident.getPhone() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(leader.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .communityId(leader.getCommunityId())
                .communityName(community != null ? community.getName() : null)
                .unit(leader.getUnit())
                .commissionRate(leader.getCommissionRate())
                .description(leader.getDescription())
                .totalEarning(totalEarning != null ? totalEarning : BigDecimal.ZERO)
                .status(leader.getStatus())
                .createdAt(createdAt)
                .appointedAt(createdAt)
                .build();
    }
}