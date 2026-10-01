package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.projectleader.CreateProjectLeaderRequest;
import com.wuye.points.dto.projectleader.ProjectLeaderDetailResponse;
import com.wuye.points.dto.projectleader.ProjectLeaderListItemResponse;
import com.wuye.points.dto.projectleader.UpdateProjectLeaderRequest;
import com.wuye.points.entity.ProjectLeader;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ProjectLeaderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
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
 * 项目负责人服务（B1）
 */
@Service
@RequiredArgsConstructor
public class ProjectLeaderService {

    private static final Set<String> LEADER_STATUS = Set.of("active", "inactive");
    private static final BigDecimal DEFAULT_PROJECT_LEADER_RATE = new BigDecimal("0.1000");

    private final ProjectLeaderRepository projectLeaderRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public List<ProjectLeaderListItemResponse> listProjectLeaders(String propertyCompanyId,
                                                                  String regionalLeaderId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator",
                "sector_leader", "resident");
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);

        List<ProjectLeader> leaders;
        if (StringUtils.hasText(regionalLeaderId)) {
            validateIdPrefix(regionalLeaderId, "rl_");
            leaders = projectLeaderRepository.findByRegionalLeaderId(regionalLeaderId);
        } else if (StringUtils.hasText(scopedPropertyCompanyId)) {
            leaders = projectLeaderRepository.findByPropertyCompanyId(scopedPropertyCompanyId);
        } else {
            leaders = projectLeaderRepository.findAll();
        }

        return leaders.stream()
                .map(this::toListItem)
                .sorted(Comparator.comparing(ProjectLeaderListItemResponse::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectLeaderDetailResponse getProjectLeader(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator", "sector_leader");
        validateIdPrefix(id, "pl_");
        ProjectLeader leader = getAccessibleProjectLeader(id);
        return toDetail(leader);
    }

    @Transactional(readOnly = true)
    public ProjectLeaderDetailResponse getMyProjectLeader() {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator",
                "sector_leader", "resident");
        String userId = UserContext.currentUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        ProjectLeader leader = projectLeaderRepository.findByResidentId(userId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "当前用户不是项目负责人"));
        return toDetail(leader);
    }

    @Transactional
    public ProjectLeaderDetailResponse createProjectLeader(CreateProjectLeaderRequest request) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "sector_leader");
        validateIdPrefix(request.getResidentId(), "res_");
        if (StringUtils.hasText(request.getRegionalLeaderId())) {
            validateIdPrefix(request.getRegionalLeaderId(), "rl_");
        }
        validateStatus(request.getStatus());

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        String propertyCompanyId = resident.getPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "住户未绑定物业公司");
        }
        roleGuard.assertSamePropertyOrPlatform(propertyCompanyId);

        projectLeaderRepository.findByResidentId(resident.getId())
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是项目负责人");
                });

        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        ProjectLeader leader = new ProjectLeader();
        leader.setId(idGenerator.generateId("pl"));
        leader.setResidentId(resident.getId());
        leader.setRegionalLeaderId(request.getRegionalLeaderId());
        leader.setPropertyCompanyId(propertyCompanyId);
        leader.setName(request.getName());
        leader.setPhone(request.getPhone());
        leader.setProjectName(request.getProjectName());
        leader.setShareRate(resolveShareRate(request.getShareRate(), company));
        leader.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        projectLeaderRepository.save(leader);

        return toDetail(leader);
    }

    @Transactional
    public ProjectLeaderDetailResponse updateProjectLeader(String id, UpdateProjectLeaderRequest request) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "sector_leader");
        validateIdPrefix(id, "pl_");
        if (StringUtils.hasText(request.getRegionalLeaderId())) {
            validateIdPrefix(request.getRegionalLeaderId(), "rl_");
        }
        validateStatus(request.getStatus());

        ProjectLeader leader = getAccessibleProjectLeader(id);

        if (request.getRegionalLeaderId() != null) {
            leader.setRegionalLeaderId(request.getRegionalLeaderId());
        }
        if (request.getName() != null) {
            leader.setName(request.getName());
        }
        if (request.getPhone() != null) {
            leader.setPhone(request.getPhone());
        }
        if (request.getProjectName() != null) {
            leader.setProjectName(request.getProjectName());
        }
        if (request.getShareRate() != null) {
            leader.setShareRate(request.getShareRate());
        }
        if (request.getStatus() != null) {
            leader.setStatus(request.getStatus());
        }
        projectLeaderRepository.save(leader);
        return toDetail(leader);
    }

    @Transactional
    public void deleteProjectLeader(String id) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "sector_leader");
        validateIdPrefix(id, "pl_");
        ProjectLeader leader = getAccessibleProjectLeader(id);
        // 软删除：置为 inactive
        leader.setStatus("inactive");
        projectLeaderRepository.save(leader);
    }

    private ProjectLeader getAccessibleProjectLeader(String id) {
        ProjectLeader leader = projectLeaderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "项目负责人不存在"));
        roleGuard.assertSamePropertyOrPlatform(leader.getPropertyCompanyId());
        return leader;
    }

    private ProjectLeaderListItemResponse toListItem(ProjectLeader leader) {
        return ProjectLeaderListItemResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .regionalLeaderId(leader.getRegionalLeaderId())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .name(leader.getName())
                .phone(leader.getPhone())
                .projectName(leader.getProjectName())
                .shareRate(leader.getShareRate())
                .status(leader.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(leader.getCreatedAt()))
                .build();
    }

    private ProjectLeaderDetailResponse toDetail(ProjectLeader leader) {
        return ProjectLeaderDetailResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .regionalLeaderId(leader.getRegionalLeaderId())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .name(leader.getName())
                .phone(leader.getPhone())
                .projectName(leader.getProjectName())
                .shareRate(leader.getShareRate())
                .status(leader.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(leader.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(leader.getUpdatedAt()))
                .build();
    }

    private BigDecimal resolveShareRate(BigDecimal requestedRate, PropertyCompany company) {
        if (requestedRate != null) {
            return requestedRate;
        }
        return company.getProjectLeaderRate() != null
                ? company.getProjectLeaderRate()
                : DEFAULT_PROJECT_LEADER_RATE;
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
