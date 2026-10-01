package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.regionallistleader.CreateRegionalLeaderRequest;
import com.wuye.points.dto.regionallistleader.RegionalLeaderDetailResponse;
import com.wuye.points.dto.regionallistleader.RegionalLeaderListItemResponse;
import com.wuye.points.dto.regionallistleader.UpdateRegionalLeaderRequest;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.RegionalLeader;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ProjectLeaderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.RegionalLeaderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 区域负责人服务（B1）
 */
@Service
@RequiredArgsConstructor
public class RegionalLeaderService {

    private static final Set<String> LEADER_STATUS = Set.of("active", "inactive");
    private static final BigDecimal DEFAULT_REGIONAL_LEADER_RATE = new BigDecimal("0.2000");

    private final RegionalLeaderRepository regionalLeaderRepository;
    private final ProjectLeaderRepository projectLeaderRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public List<RegionalLeaderListItemResponse> listRegionalLeaders(String propertyCompanyId,
                                                                    String sectorLeaderId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator", "sector_leader");
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        String scopedSectorLeaderId = resolveScopedSectorLeaderId(sectorLeaderId);

        List<RegionalLeader> leaders;
        if (StringUtils.hasText(scopedSectorLeaderId)) {
            leaders = regionalLeaderRepository.findBySectorLeaderId(scopedSectorLeaderId);
        } else if (StringUtils.hasText(scopedPropertyCompanyId)) {
            leaders = regionalLeaderRepository.findByPropertyCompanyId(scopedPropertyCompanyId);
        } else {
            leaders = regionalLeaderRepository.findAll();
        }

        return leaders.stream()
                .map(this::toListItem)
                .sorted(Comparator.comparing(RegionalLeaderListItemResponse::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RegionalLeaderDetailResponse getRegionalLeader(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator", "sector_leader");
        validateIdPrefix(id, "rl_");
        RegionalLeader leader = getAccessibleRegionalLeader(id);
        return toDetail(leader);
    }

    @Transactional(readOnly = true)
    public RegionalLeaderDetailResponse getMyRegionalLeader() {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator",
                "sector_leader", "resident");
        String userId = UserContext.currentUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        RegionalLeader leader = regionalLeaderRepository.findByResidentId(userId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "当前用户不是区域负责人"));
        return toDetail(leader);
    }

    @Transactional
    public RegionalLeaderDetailResponse createRegionalLeader(CreateRegionalLeaderRequest request) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "sector_leader");
        validateIdPrefix(request.getResidentId(), "res_");
        if (StringUtils.hasText(request.getSectorLeaderId())) {
            validateIdPrefix(request.getSectorLeaderId(), "sl_");
        }
        if (StringUtils.hasText(request.getCoordinatorId())) {
            validateIdPrefix(request.getCoordinatorId(), "coo_");
        }
        validateStatus(request.getStatus());

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        String propertyCompanyId = resident.getPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "住户未绑定物业公司");
        }
        assertSamePropertyOrPlatform(propertyCompanyId);

        // 唯一性校验：同一住户只能是一个区域负责人
        regionalLeaderRepository.findByResidentId(resident.getId())
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是区域负责人");
                });

        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        RegionalLeader leader = new RegionalLeader();
        leader.setId(idGenerator.generateId("rl"));
        leader.setResidentId(resident.getId());
        leader.setSectorLeaderId(request.getSectorLeaderId());
        leader.setCoordinatorId(request.getCoordinatorId());
        leader.setPropertyCompanyId(propertyCompanyId);
        leader.setName(request.getName());
        leader.setPhone(request.getPhone());
        leader.setRegionName(request.getRegionName());
        leader.setShareRate(resolveShareRate(request.getShareRate(), company));
        leader.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        regionalLeaderRepository.save(leader);

        return toDetail(leader);
    }

    @Transactional
    public RegionalLeaderDetailResponse updateRegionalLeader(String id, UpdateRegionalLeaderRequest request) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "sector_leader");
        validateIdPrefix(id, "rl_");
        if (StringUtils.hasText(request.getSectorLeaderId())) {
            validateIdPrefix(request.getSectorLeaderId(), "sl_");
        }
        if (StringUtils.hasText(request.getCoordinatorId())) {
            validateIdPrefix(request.getCoordinatorId(), "coo_");
        }
        validateStatus(request.getStatus());

        RegionalLeader leader = getAccessibleRegionalLeader(id);

        if (request.getSectorLeaderId() != null) {
            leader.setSectorLeaderId(request.getSectorLeaderId());
        }
        if (request.getCoordinatorId() != null) {
            leader.setCoordinatorId(request.getCoordinatorId());
        }
        if (request.getName() != null) {
            leader.setName(request.getName());
        }
        if (request.getPhone() != null) {
            leader.setPhone(request.getPhone());
        }
        if (request.getRegionName() != null) {
            leader.setRegionName(request.getRegionName());
        }
        if (request.getShareRate() != null) {
            leader.setShareRate(request.getShareRate());
        }
        if (request.getStatus() != null) {
            leader.setStatus(request.getStatus());
        }
        regionalLeaderRepository.save(leader);
        return toDetail(leader);
    }

    @Transactional
    public void deleteRegionalLeader(String id) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "sector_leader");
        validateIdPrefix(id, "rl_");
        RegionalLeader leader = getAccessibleRegionalLeader(id);
        // 软删除：置为 inactive
        leader.setStatus("inactive");
        regionalLeaderRepository.save(leader);
    }

    private RegionalLeader getAccessibleRegionalLeader(String id) {
        RegionalLeader leader = regionalLeaderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "区域负责人不存在"));
        assertSamePropertyOrPlatform(leader.getPropertyCompanyId());
        return leader;
    }

    private void assertSamePropertyOrPlatform(String targetPropertyCompanyId) {
        roleGuard.assertSamePropertyOrPlatform(targetPropertyCompanyId);
    }

    private RegionalLeaderListItemResponse toListItem(RegionalLeader leader) {
        long projectLeaderCount = projectLeaderRepository.countByRegionalLeaderIdAndStatus(
                leader.getId(), "active");
        return RegionalLeaderListItemResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .sectorLeaderId(leader.getSectorLeaderId())
                .coordinatorId(leader.getCoordinatorId())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .name(leader.getName())
                .phone(leader.getPhone())
                .regionName(leader.getRegionName())
                .shareRate(leader.getShareRate())
                .status(leader.getStatus())
                .projectLeaderCount(projectLeaderCount)
                .createdAt(DateTimeUtil.toIsoUtc(leader.getCreatedAt()))
                .build();
    }

    private RegionalLeaderDetailResponse toDetail(RegionalLeader leader) {
        long projectLeaderCount = projectLeaderRepository.countByRegionalLeaderIdAndStatus(
                leader.getId(), "active");
        return RegionalLeaderDetailResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .sectorLeaderId(leader.getSectorLeaderId())
                .coordinatorId(leader.getCoordinatorId())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .name(leader.getName())
                .phone(leader.getPhone())
                .regionName(leader.getRegionName())
                .shareRate(leader.getShareRate())
                .status(leader.getStatus())
                .projectLeaderCount(projectLeaderCount)
                .createdAt(DateTimeUtil.toIsoUtc(leader.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(leader.getUpdatedAt()))
                .build();
    }

    private BigDecimal resolveShareRate(BigDecimal requestedRate, PropertyCompany company) {
        if (requestedRate != null) {
            return requestedRate;
        }
        return company.getRegionalLeaderRate() != null
                ? company.getRegionalLeaderRate()
                : DEFAULT_REGIONAL_LEADER_RATE;
    }

    private String resolveScopedPropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(requestedPropertyCompanyId)) {
                if (!requestedPropertyCompanyId.startsWith("pc_")) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
                }
                return requestedPropertyCompanyId;
            }
            return null;
        }
        return UserContext.currentPropertyCompanyId();
    }

    /**
     * sector_leader 范围解析：通过 propertyCompanyId 限定范围已经足够，
     * sectorLeaderId 由调用方按需传入过滤即可。
     */
    private String resolveScopedSectorLeaderId(String requestedSectorLeaderId) {
        return requestedSectorLeaderId;
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !LEADER_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }
    }

    private void validateIdPrefix(String id, String prefix) {
        if (!StringUtils.hasText(id) || !id.startsWith(prefix)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ID格式不正确");
        }
    }
}
