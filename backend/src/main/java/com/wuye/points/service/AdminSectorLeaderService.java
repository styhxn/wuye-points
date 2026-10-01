package com.wuye.points.service;

import com.wuye.points.dto.admin.sectorleader.AppointSectorLeaderRequest;
import com.wuye.points.dto.admin.sectorleader.SectorLeaderDetailResponse;
import com.wuye.points.dto.admin.sectorleader.SectorLeaderListItemResponse;
import com.wuye.points.dto.admin.sectorleader.UpdateSectorLeaderRequest;
import com.wuye.points.context.UserContext;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSectorLeaderService {

    private static final Set<String> LEADER_STATUS = Set.of("active", "inactive");
    private static final BigDecimal DEFAULT_SECTOR_LEADER_RATE = new BigDecimal("0.1000");

    private final SectorLeaderRepository sectorLeaderRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final DistributionConverter distributionConverter;
    private final PermissionGuard permissionGuard;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<SectorLeaderListItemResponse> listSectorLeaders(
            int page,
            int pageSize,
            String sector,
            String coordinatorId,
            String status,
            String propertyCompanyId,
            String keyword) {
        // 2026-09-07 前端问题 C：coordinator 持有 sector.read 预设权限，允许只读查看本物业板块负责人
        permissionGuard.requireReadPermissionOrAdmin("sector.read");
        validateStatus(status);
        validateSector(sector);
        if (StringUtils.hasText(coordinatorId)) {
            adminSupport.validateIdPrefix(coordinatorId, "coo_");
        }

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        // 2026-09-08：§39.1 keyword（姓名/手机号）过滤，先解析命中的住户ID再落到 IN 条件
        List<String> keywordResidentIds = null;
        if (StringUtils.hasText(keyword)) {
            keywordResidentIds = residentRepository.findIdsByNameContainingOrPhoneContaining(keyword.trim());
            if (keywordResidentIds.isEmpty()) {
                return PageResponse.of(List.of(), page, pageSize, 0);
            }
        }
        Page<SectorLeader> leaderPage = sectorLeaderRepository.findAll(
                SectorLeaderSpecifications.withFilters(
                        scopedPropertyCompanyId,
                        SectorCatalog.normalize(sector),
                        coordinatorId,
                        status != null ? status.trim().toLowerCase() : null,
                        keywordResidentIds),
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
        requireSectorManage();
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");
        // 2026-09-08：板块入参归一化（支持中文「维修」等），再校验五枚举
        String sector = SectorCatalog.normalizeOrThrow(request.getSector());

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        String propertyCompanyId = resident.getPropertyCompanyId();

        sectorLeaderRepository.findByResidentIdAndPropertyCompanyIdAndSector(
                        resident.getId(), propertyCompanyId, sector)
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是此板块负责人");
                });

        // 2026-09-08：统筹指派的板块负责人固定挂在本人名下，避免越权挂到其他统筹下
        Coordinator coordinator = roleGuard.isCoordinator()
                ? resolveCurrentCoordinator()
                : resolveCoordinator(request.getCoordinatorId(), propertyCompanyId);

        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        SectorLeader leader = new SectorLeader();
        leader.setId(idGenerator.generateId("sl"));
        leader.setResidentId(resident.getId());
        leader.setPropertyCompanyId(propertyCompanyId);
        leader.setSector(sector);
        leader.setDescription(normalizeDescription(request.getDescription()));
        leader.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company));
        leader.setCoordinatorId(coordinator != null ? coordinator.getId() : null);
        String status = StringUtils.hasText(request.getStatus())
                ? request.getStatus().trim().toLowerCase() : "active";
        validateStatus(status);
        leader.setStatus(status);
        sectorLeaderRepository.save(leader);

        return toDetail(leader, resident);
    }

    @Transactional(readOnly = true)
    public SectorLeaderDetailResponse getSectorLeader(String id) {
        // 2026-09-07 前端问题 C：coordinator 只读放行（详情仍由 assertSameProperty 限定本物业）
        permissionGuard.requireReadPermissionOrAdmin("sector.read");
        adminSupport.validateIdPrefix(id, "sl_");

        SectorLeader leader = getAccessibleSectorLeader(id);
        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    @Transactional
    public SectorLeaderDetailResponse updateSectorLeader(String id, UpdateSectorLeaderRequest request) {
        requireSectorManage();
        adminSupport.validateIdPrefix(id, "sl_");

        SectorLeader leader = getAccessibleSectorLeader(id);
        // 2026-09-08：统筹仅可更新本人名下的板块负责人
        assertOwnedByCurrentCoordinator(leader);
        PropertyCompany company = propertyCompanyRepository.findById(leader.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        if (StringUtils.hasText(request.getSector())) {
            // 2026-09-08：中文入参归一化 + 五枚举校验 + 同物业同板块冲突前置校验
            String newSector = SectorCatalog.normalizeOrThrow(request.getSector());
            assertSectorAvailable(leader, newSector, leader.getPropertyCompanyId());
            leader.setSector(newSector);
        }
        if (StringUtils.hasText(request.getDescription())) {
            leader.setDescription(normalizeDescription(request.getDescription()));
        }
        if (request.getCommissionRate() != null) {
            leader.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company));
        }
        if (request.getCoordinatorId() != null) {
            // 统筹不允许把板块负责人转挂到其他统筹名下
            Coordinator coordinator = roleGuard.isCoordinator()
                    ? resolveCurrentCoordinator()
                    : resolveCoordinator(request.getCoordinatorId(), leader.getPropertyCompanyId());
            leader.setCoordinatorId(coordinator != null ? coordinator.getId() : null);
        }
        if (StringUtils.hasText(request.getStatus())) {
            String status = request.getStatus().trim().toLowerCase();
            validateStatus(status);
            leader.setStatus(status);
        }

        sectorLeaderRepository.save(leader);
        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    @Transactional
    public SectorLeaderDetailResponse revokeSectorLeader(String id, String reason) {
        requireSectorManage();
        adminSupport.validateIdPrefix(id, "sl_");
        // reason 仅为备注，前端可不传（2026-09-07 前端问题 A）

        SectorLeader leader = getAccessibleSectorLeader(id);
        // 2026-09-08：统筹仅可撤销本人名下的板块负责人
        assertOwnedByCurrentCoordinator(leader);

        // 层级约束：存在启用的下级个体负责人时不允许撤销，返回明确 400（避免前端拿到 500/级联错误）
        long activeIndividualLeaders = individualLeaderRepository.countBySectorLeaderIdAndStatus(
                leader.getId(), "active");
        if (activeIndividualLeaders > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "该板块负责人名下仍有 " + activeIndividualLeaders + " 名启用中的个体负责人，请先撤销或转移个体负责人后再撤销板块负责人");
        }

        leader.setStatus("inactive");
        sectorLeaderRepository.save(leader);

        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        return toDetail(leader, resident);
    }

    /**
     * 2026-09-08 前端问题：统筹写操作 403。
     * 统筹（coordinator）按 §39 总述可列表/指派/更新/撤销本人名下板块负责人，
     * 校验方式由 requireAdmin 改为「持有 sector.manage 权限码即放行」，数据范围见
     * {@link #assertOwnedByCurrentCoordinator}。
     */
    private void requireSectorManage() {
        permissionGuard.requirePermissionOrAdminAllowingCoordinator("sector.manage");
    }

    /** 当前登录统筹对应的 coordinators 记录 */
    private Coordinator resolveCurrentCoordinator() {
        return coordinatorRepository.findByResidentIdAndPropertyCompanyId(
                        UserContext.currentUserId(), UserContext.currentPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.COORDINATOR_NOT_FOUND, "当前账号未绑定统筹记录"));
    }

    /** 统筹仅可操作本人名下的板块负责人；非统筹角色不做此限制 */
    private void assertOwnedByCurrentCoordinator(SectorLeader leader) {
        if (!roleGuard.isCoordinator()) {
            return;
        }
        String coordinatorId = resolveCurrentCoordinator().getId();
        if (leader.getCoordinatorId() == null || !coordinatorId.equals(leader.getCoordinatorId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "只能操作本人名下的板块负责人");
        }
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

        // 2026-09-07 前端问题 B：列表补充物业/统筹/时间字段，前端不再需要本地补全
        String propertyCompanyName = propertyCompanyRepository.findById(leader.getPropertyCompanyId())
                .map(PropertyCompany::getName)
                .orElse(null);
        String coordinatorName = leader.getCoordinatorId() != null
                ? coordinatorRepository.findById(leader.getCoordinatorId())
                        .map(coordinator -> residentRepository.findById(coordinator.getResidentId())
                                .map(Resident::getName)
                                .orElse(null))
                        .orElse(null)
                : null;
        String phone = resident != null ? resident.getPhone() : null;
        String createdAtText = DateTimeUtil.toIsoUtc(leader.getCreatedAt());

        return SectorLeaderListItemResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .phone(phone)
                .residentPhone(phone)
                .propertyCompanyId(leader.getPropertyCompanyId())
                .propertyCompanyName(propertyCompanyName)
                .sector(leader.getSector())
                .sectorName(distributionConverter.sectorLabel(leader.getSector()))
                .description(leader.getDescription())
                .commissionRate(leader.getCommissionRate())
                .coordinatorId(leader.getCoordinatorId())
                .coordinatorName(coordinatorName)
                .individualLeaderCount(individualLeaderCount)
                .totalEarnings(totalEarnings)
                .status(leader.getStatus())
                .statusCode(leader.getStatus())
                .statusLabel(statusLabel(leader.getStatus()))
                .appointedAt(createdAtText)
                .createdAt(createdAtText)
                .build();
    }

    /** 状态中文标签（§39.1：status 为展示用中文，statusCode 为英文码） */
    private String statusLabel(String status) {
        if (status == null) {
            return null;
        }
        return "active".equals(status) ? "启用" : "inactive".equals(status) ? "停用" : status;
    }

    private String normalizeDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
    }

    private SectorLeaderDetailResponse toDetail(SectorLeader leader, Resident resident) {
        SectorLeaderListItemResponse listItem = toListItem(leader, resident);
        return SectorLeaderDetailResponse.builder()
                .id(listItem.getId())
                .residentId(listItem.getResidentId())
                .residentName(listItem.getResidentName())
                .phone(listItem.getPhone())
                .residentPhone(listItem.getResidentPhone())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .propertyCompanyName(listItem.getPropertyCompanyName())
                .sector(listItem.getSector())
                .sectorName(listItem.getSectorName())
                .description(listItem.getDescription())
                .commissionRate(listItem.getCommissionRate())
                .coordinatorId(listItem.getCoordinatorId())
                .coordinatorName(listItem.getCoordinatorName())
                .individualLeaderCount(listItem.getIndividualLeaderCount())
                .totalEarnings(listItem.getTotalEarnings())
                .status(listItem.getStatus())
                .statusCode(listItem.getStatusCode())
                .statusLabel(listItem.getStatusLabel())
                .appointedAt(listItem.getAppointedAt())
                .createdAt(listItem.getCreatedAt())
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

    /**
     * 2026-09-08 前端问题「改板块 400」：改板块前做冲突前置校验。
     * 数据库唯一键是 (resident_id, property_company_id, sector)，并不限制「同物业同板块多人」，
     * 因此这里按产品口径校验：同一物业公司下同一板块已有其他启用中的负责人时，
     * 返回 400 + 可运营理解的冲突文案（含冲突对象 ID / 姓名），而不是笼统的「sector不合法」。
     */
    private void assertSectorAvailable(SectorLeader leader, String newSector, String propertyCompanyId) {
        if (!SectorCatalog.isValid(newSector)) {
            return;
        }
        if (newSector.equals(leader.getSector())) {
            return;
        }
        List<SectorLeader> sameSector = sectorLeaderRepository.findAll(
                SectorLeaderSpecifications.withFilters(propertyCompanyId, newSector, null, "active"));
        for (SectorLeader existing : sameSector) {
            if (existing.getId().equals(leader.getId())) {
                continue;
            }
            String holderName = residentRepository.findById(existing.getResidentId())
                    .map(Resident::getName)
                    .orElse("未知");
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "该物业下板块「" + SectorCatalog.label(newSector) + "」已有启用中的负责人 "
                            + existing.getId() + "（" + holderName + "），请先撤销或调整其板块后再改");
        }
    }
}
