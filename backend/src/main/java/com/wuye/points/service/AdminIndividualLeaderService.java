package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.individualleader.AppointIndividualLeaderRequest;
import com.wuye.points.dto.admin.individualleader.AuditIndividualLeaderApplicationRequest;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderApplicationAuditResponse;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderApplicationListItemResponse;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderDetailResponse;
import com.wuye.points.dto.admin.individualleader.IndividualLeaderListItemResponse;
import com.wuye.points.dto.admin.individualleader.UpdateIndividualLeaderRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.IndividualLeaderApplication;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderApplicationRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.repository.spec.IndividualLeaderApplicationSpecifications;
import com.wuye.points.repository.spec.IndividualLeaderSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SectorCatalog;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminIndividualLeaderService {

    private static final Set<String> LEADER_STATUS = Set.of("active", "inactive");
    /** 个体负责人申请的审核状态（2026-09-08 §37 申请列表 / 审核） */
    private static final Set<String> APPLICATION_AUDIT_STATUS = Set.of("pending", "approved", "rejected");
    private static final BigDecimal DEFAULT_INDIVIDUAL_LEADER_RATE = BigDecimal.ZERO;

    private final IndividualLeaderRepository individualLeaderRepository;
    private final IndividualLeaderApplicationRepository applicationRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final DistributionConverter distributionConverter;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<IndividualLeaderListItemResponse> listIndividualLeaders(
            int page,
            int pageSize,
            String sectorLeaderId,
            String keyword,
            String status,
            String sector,
            String propertyCompanyId) {
        requireIndividualRead();
        validateStatus(status);
        validateSector(sector);

        String scopedSectorLeaderId = resolveScopedSectorLeaderId(sectorLeaderId);
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<IndividualLeader> leaderPage = individualLeaderRepository.findAll(
                IndividualLeaderSpecifications.withFilters(
                        scopedPropertyCompanyId, scopedSectorLeaderId, sector, status, keyword),
                pageable);

        Map<String, Resident> residentMap = loadResidents(leaderPage.getContent().stream()
                .map(IndividualLeader::getResidentId)
                .toList());

        List<IndividualLeaderListItemResponse> list = leaderPage.getContent().stream()
                .map(leader -> toListItem(leader, residentMap.get(leader.getResidentId())))
                .toList();

        return PageResponse.of(list, page, pageSize, leaderPage.getTotalElements());
    }

    @Transactional
    public IndividualLeaderDetailResponse appointIndividualLeader(AppointIndividualLeaderRequest request) {
        requireIndividualManage();
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");
        adminSupport.validateIdPrefix(request.getSectorLeaderId(), "sl_");
        // 2026-09-08：板块入参归一化（支持中文），统一五枚举
        String sector = SectorCatalog.normalizeOrThrow(request.getSector());

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());

        SectorLeader sectorLeader = getAccessibleSectorLeaderForWrite(request.getSectorLeaderId());
        assertSectorLeaderScope(sectorLeader);
        if (!sector.equals(sectorLeader.getSector())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "sector与板块负责人不匹配：该板块负责人负责「" + SectorCatalog.label(sectorLeader.getSector())
                            + "」，不能指派到「" + SectorCatalog.label(sector) + "」");
        }

        individualLeaderRepository.findByResidentIdAndPropertyCompanyIdAndSectorLeaderId(
                        resident.getId(), resident.getPropertyCompanyId(), sectorLeader.getId())
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是个体负责人");
                });

        PropertyCompany company = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        IndividualLeader leader = new IndividualLeader();
        leader.setId(idGenerator.generateId("il"));
        leader.setResidentId(resident.getId());
        leader.setPropertyCompanyId(resident.getPropertyCompanyId());
        leader.setSectorLeaderId(sectorLeader.getId());
        leader.setSector(sector);
        leader.setName(StringUtils.hasText(request.getName()) ? request.getName().trim() : resident.getName());
        leader.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company));
        leader.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        validateStatus(leader.getStatus());
        individualLeaderRepository.save(leader);

        return toDetail(leader, resident);
    }

    @Transactional(readOnly = true)
    public IndividualLeaderDetailResponse getIndividualLeader(String id) {
        requireIndividualRead();
        adminSupport.validateIdPrefix(id, "il_");

        IndividualLeader leader = getAccessibleIndividualLeader(id);
        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    @Transactional
    public IndividualLeaderDetailResponse updateIndividualLeader(String id, UpdateIndividualLeaderRequest request) {
        requireIndividualManage();
        adminSupport.validateIdPrefix(id, "il_");

        IndividualLeader leader = getAccessibleIndividualLeader(id);
        PropertyCompany company = propertyCompanyRepository.findById(leader.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        if (request.getSectorLeaderId() != null) {
            adminSupport.validateIdPrefix(request.getSectorLeaderId(), "sl_");
            SectorLeader sectorLeader = getAccessibleSectorLeaderForWrite(request.getSectorLeaderId());
            assertSectorLeaderScope(sectorLeader);
            leader.setSectorLeaderId(sectorLeader.getId());
        }
        if (StringUtils.hasText(request.getSector())) {
            leader.setSector(SectorCatalog.normalizeOrThrow(request.getSector()));
        }
        if (request.getName() != null) {
            leader.setName(request.getName().trim());
        }
        if (request.getCommissionRate() != null) {
            leader.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company));
        }
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            leader.setStatus(request.getStatus());
        }

        individualLeaderRepository.save(leader);
        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    @Transactional
    public IndividualLeaderDetailResponse revokeIndividualLeader(String id, String reason) {
        requireIndividualManage();
        adminSupport.validateIdPrefix(id, "il_");
        if (!StringUtils.hasText(reason)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "reason不能为空");
        }

        IndividualLeader leader = getAccessibleIndividualLeader(id);
        assertSectorLeaderScopeOnLeader(leader);
        leader.setStatus("inactive");
        individualLeaderRepository.save(leader);

        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    /**
     * 个体负责人申请列表（2026-09-08 新增，§37 GET /admin/individual-leaders/applications）。
     * 按 createdAt 倒序，支持审核状态 / 板块 / 关键词（姓名或手机号）过滤。
     */
    @Transactional(readOnly = true)
    public PageResponse<IndividualLeaderApplicationListItemResponse> listApplications(
            int page,
            int pageSize,
            String auditStatus,
            String sector,
            String keyword,
            String propertyCompanyId) {
        requireIndividualRead();
        validateApplicationAuditStatus(auditStatus);

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<IndividualLeaderApplication> applicationPage = applicationRepository.findAll(
                IndividualLeaderApplicationSpecifications.withFilters(
                        scopedPropertyCompanyId, sector, auditStatus, keyword),
                pageable);

        Map<String, Resident> residentMap = loadResidents(applicationPage.getContent().stream()
                .map(IndividualLeaderApplication::getResidentId)
                .toList());

        List<IndividualLeaderApplicationListItemResponse> list = applicationPage.getContent().stream()
                .map(application -> toApplicationListItem(application, residentMap.get(application.getResidentId())))
                .toList();

        return PageResponse.of(list, page, pageSize, applicationPage.getTotalElements());
    }

    /**
     * 审核个体负责人申请（2026-09-08 新增，§37.6）。
     * approved：创建 IndividualLeader 记录（有同板块启用中的板块负责人则自动挂靠，否则留空待分配）
     * 并把住户角色变更为 individual_leader；rejected：记录拒绝原因。
     */
    @Transactional
    public IndividualLeaderApplicationAuditResponse auditApplication(
            String applicationId, AuditIndividualLeaderApplicationRequest request) {
        requireIndividualManage();
        adminSupport.validateIdPrefix(applicationId, "ila_");

        IndividualLeaderApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BizException(ErrorCode.INDIVIDUAL_LEADER_APPLICATION_NOT_FOUND));
        adminSupport.assertSameProperty(application.getPropertyCompanyId());

        if (!"pending".equals(application.getAuditStatus())) {
            throw new BizException(ErrorCode.INDIVIDUAL_LEADER_APPLICATION_ALREADY_AUDITED,
                    "该申请已审核（当前状态：" + application.getAuditStatus() + "）");
        }

        LocalDateTime auditedAt = LocalDateTime.now();
        application.setAuditedAt(auditedAt);

        if ("rejected".equals(request.getAuditStatus())) {
            if (!StringUtils.hasText(request.getRejectReason())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "rejectReason不能为空");
            }
            application.setAuditStatus("rejected");
            application.setRejectReason(request.getRejectReason().trim());
            applicationRepository.save(application);
            return toAuditResponse(application, null, null);
        }

        Resident resident = residentRepository.findById(application.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        if (!"active".equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED, "该住户账号已停用，无法通过申请");
        }
        individualLeaderRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        resident.getId(), resident.getPropertyCompanyId(), "active")
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是个体负责人，无需重复审批");
                });

        PropertyCompany company = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        IndividualLeader leader = new IndividualLeader();
        leader.setId(idGenerator.generateId("il"));
        leader.setResidentId(resident.getId());
        leader.setPropertyCompanyId(resident.getPropertyCompanyId());
        leader.setSectorLeaderId(sectorLeaderRepository.findFirstByPropertyCompanyIdAndSectorAndStatus(
                        resident.getPropertyCompanyId(), application.getSector(), "active")
                .map(SectorLeader::getId)
                .orElse(null));
        leader.setSector(application.getSector());
        leader.setName(resident.getName());
        leader.setCommissionRate(resolveCommissionRate(null, company));
        leader.setStatus("active");
        individualLeaderRepository.save(leader);

        resident.setRole("individual_leader");
        residentRepository.save(resident);

        application.setAuditStatus("approved");
        application.setIndividualLeaderId(leader.getId());
        applicationRepository.save(application);

        return toAuditResponse(application, leader, resident);
    }

    private IndividualLeaderApplicationAuditResponse toAuditResponse(
            IndividualLeaderApplication application,
            IndividualLeader leader,
            Resident resident) {
        Resident applicant = resident != null
                ? resident
                : residentRepository.findById(application.getResidentId()).orElse(null);
        return IndividualLeaderApplicationAuditResponse.builder()
                .applicationId(application.getId())
                .auditStatus(application.getAuditStatus())
                .residentId(application.getResidentId())
                .residentName(applicant != null ? applicant.getName() : null)
                .sector(application.getSector())
                .rejectReason(application.getRejectReason())
                .auditedAt(DateTimeUtil.toIsoUtc(application.getAuditedAt()))
                .individualLeaderId(application.getIndividualLeaderId())
                .individualLeader(leader != null ? toDetail(leader, applicant) : null)
                .build();
    }

    private IndividualLeaderApplicationListItemResponse toApplicationListItem(
            IndividualLeaderApplication application, Resident resident) {
        return IndividualLeaderApplicationListItemResponse.builder()
                .id(application.getId())
                .residentId(application.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(application.getPropertyCompanyId())
                .sector(application.getSector())
                .remark(application.getRemark())
                .auditStatus(application.getAuditStatus())
                .rejectReason(application.getRejectReason())
                .createdAt(DateTimeUtil.toIsoUtc(application.getCreatedAt()))
                .auditedAt(DateTimeUtil.toIsoUtc(application.getAuditedAt()))
                .build();
    }

    private void validateApplicationAuditStatus(String auditStatus) {
        if (StringUtils.hasText(auditStatus) && !APPLICATION_AUDIT_STATUS.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus不合法，应为 pending/approved/rejected");
        }
    }

    private void requireIndividualRead() {
        if ("sector_leader".equals(UserContext.currentRole())) {
            sectorLeaderRepository.findByResidentIdAndPropertyCompanyIdAndStatus(
                            UserContext.currentUserId(), UserContext.currentPropertyCompanyId(), "active")
                    .orElseThrow(() -> new BizException(ErrorCode.PERMISSION_DENIED));
            return;
        }
        // 2026-09-07 前端问题 C：统筹为只读下级角色，可查看个体负责人列表
        if ("coordinator".equals(UserContext.currentRole())) {
            return;
        }
        permissionGuard.requirePermissionOrAdmin("individual.read");
    }

    private void requireIndividualManage() {
        if ("sector_leader".equals(UserContext.currentRole())) {
            sectorLeaderRepository.findByResidentIdAndPropertyCompanyIdAndStatus(
                            UserContext.currentUserId(), UserContext.currentPropertyCompanyId(), "active")
                    .orElseThrow(() -> new BizException(ErrorCode.PERMISSION_DENIED));
            return;
        }
        permissionGuard.requirePermissionOrAdmin("individual.manage");
    }

    private String resolveScopedSectorLeaderId(String requestedSectorLeaderId) {
        if ("sector_leader".equals(UserContext.currentRole())) {
            SectorLeader current = sectorLeaderRepository.findByResidentIdAndPropertyCompanyIdAndStatus(
                            UserContext.currentUserId(), UserContext.currentPropertyCompanyId(), "active")
                    .orElseThrow(() -> new BizException(ErrorCode.PERMISSION_DENIED));
            if (StringUtils.hasText(requestedSectorLeaderId)
                    && !requestedSectorLeaderId.equals(current.getId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            return current.getId();
        }
        if (StringUtils.hasText(requestedSectorLeaderId)) {
            adminSupport.validateIdPrefix(requestedSectorLeaderId, "sl_");
        }
        return requestedSectorLeaderId;
    }

    private IndividualLeader getAccessibleIndividualLeader(String id) {
        IndividualLeader leader = individualLeaderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.INDIVIDUAL_LEADER_NOT_FOUND));
        adminSupport.assertSameProperty(leader.getPropertyCompanyId());
        assertSectorLeaderScopeOnLeader(leader);
        return leader;
    }

    private SectorLeader getAccessibleSectorLeaderForWrite(String sectorLeaderId) {
        SectorLeader sectorLeader = sectorLeaderRepository.findById(sectorLeaderId)
                .orElseThrow(() -> new BizException(ErrorCode.SECTOR_LEADER_NOT_FOUND));
        adminSupport.assertSameProperty(sectorLeader.getPropertyCompanyId());
        return sectorLeader;
    }

    private void assertSectorLeaderScope(SectorLeader sectorLeader) {
        if (!"sector_leader".equals(UserContext.currentRole())) {
            return;
        }
        SectorLeader current = sectorLeaderRepository.findByResidentIdAndPropertyCompanyIdAndStatus(
                        UserContext.currentUserId(), UserContext.currentPropertyCompanyId(), "active")
                .orElseThrow(() -> new BizException(ErrorCode.PERMISSION_DENIED));
        if (!current.getId().equals(sectorLeader.getId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void assertSectorLeaderScopeOnLeader(IndividualLeader leader) {
        if (!"sector_leader".equals(UserContext.currentRole())) {
            return;
        }
        SectorLeader current = sectorLeaderRepository.findByResidentIdAndPropertyCompanyIdAndStatus(
                        UserContext.currentUserId(), UserContext.currentPropertyCompanyId(), "active")
                .orElseThrow(() -> new BizException(ErrorCode.PERMISSION_DENIED));
        if (!current.getId().equals(leader.getSectorLeaderId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private BigDecimal resolveCommissionRate(BigDecimal requestedRate, PropertyCompany company) {
        if (requestedRate != null) {
            return requestedRate;
        }
        return company.getIndividualLeaderRate() != null
                ? company.getIndividualLeaderRate()
                : DEFAULT_INDIVIDUAL_LEADER_RATE;
    }

    private IndividualLeaderListItemResponse toListItem(IndividualLeader leader, Resident resident) {
        BigDecimal totalEarnings = distributionRecordRepository
                .sumIndividualLeaderShareByIndividualLeaderId(leader.getId());

        return IndividualLeaderListItemResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .name(leader.getName())
                .sectorLeaderId(leader.getSectorLeaderId())
                .sector(leader.getSector())
                .sectorName(distributionConverter.sectorLabel(leader.getSector()))
                .commissionRate(leader.getCommissionRate())
                .totalEarnings(totalEarnings)
                .status(leader.getStatus())
                .appointedAt(DateTimeUtil.toIsoUtc(leader.getCreatedAt()))
                .build();
    }

    private IndividualLeaderDetailResponse toDetail(IndividualLeader leader, Resident resident) {
        IndividualLeaderListItemResponse listItem = toListItem(leader, resident);
        return IndividualLeaderDetailResponse.builder()
                .id(listItem.getId())
                .residentId(listItem.getResidentId())
                .residentName(listItem.getResidentName())
                .phone(listItem.getPhone())
                .name(listItem.getName())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .sectorLeaderId(listItem.getSectorLeaderId())
                .sector(listItem.getSector())
                .sectorName(listItem.getSectorName())
                .commissionRate(listItem.getCommissionRate())
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
        if (StringUtils.hasText(status) && !LEADER_STATUS.contains(status.toLowerCase())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "status不合法：" + status + "；可选值 active/inactive");
        }
    }

    /** 2026-09-08：板块字典统一走 {@link SectorCatalog}，并支持中文入参 */
    private void validateSector(String sector) {
        SectorCatalog.validate(sector);
    }
}
