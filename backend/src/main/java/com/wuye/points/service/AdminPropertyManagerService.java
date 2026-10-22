package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.propertymanager.CreatePropertyManagerRequest;
import com.wuye.points.dto.admin.propertymanager.PropertyManagerDetailResponse;
import com.wuye.points.dto.admin.propertymanager.PropertyManagerListItemResponse;
import com.wuye.points.dto.admin.propertymanager.UpdatePropertyManagerRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyManager;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyManagerRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PropertyManagerSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
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

/**
 * 管理端 · 物业公司经理管理
 *
 * <p>物业经理是「住户 + 经理」复合身份（不占用 residents.role），从该物业公司订单产生的
 * 单元楼长分成中按 commissionRate 抽成。同一物业公司同一时刻仅允许一名 active 经理（应用层校验）。
 */
@Service
@RequiredArgsConstructor
public class AdminPropertyManagerService {

    private static final Set<String> MANAGER_STATUS = Set.of("active", "inactive");

    private final PropertyManagerRepository propertyManagerRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<PropertyManagerListItemResponse> listPropertyManagers(
            int page,
            int pageSize,
            String keyword,
            String status,
            String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateStatus(status);

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<PropertyManager> managerPage = propertyManagerRepository.findAll(
                PropertyManagerSpecifications.withFilters(scopedPropertyCompanyId, status, keyword),
                pageable);

        List<PropertyManager> managers = managerPage.getContent();
        Map<String, Resident> residentMap = loadResidents(managers.stream()
                .map(PropertyManager::getResidentId).toList());
        Map<String, PropertyCompany> companyMap = loadCompanies(managers.stream()
                .map(PropertyManager::getPropertyCompanyId).collect(Collectors.toSet()));

        List<PropertyManagerListItemResponse> list = managers.stream()
                .map(manager -> toListItem(manager,
                        residentMap.get(manager.getResidentId()),
                        companyMap.get(manager.getPropertyCompanyId())))
                .toList();
        return PageResponse.of(list, page, pageSize, managerPage.getTotalElements());
    }

    @Transactional
    public PropertyManagerDetailResponse createPropertyManager(CreatePropertyManagerRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        String propertyCompanyId = resident.getPropertyCompanyId();

        // 同一住户已有经理记录（含停用）时禁止重复指派，请走更新接口调整原记录
        if (!propertyManagerRepository.findByResidentId(resident.getId()).isEmpty()) {
            throw new BizException(ErrorCode.PROPERTY_MANAGER_ALREADY_EXISTS, "该住户已有物业经理记录，请直接更新原记录");
        }

        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active";
        validateStatus(status);
        assertUniqueActiveManager(propertyCompanyId, status, null);

        PropertyManager manager = new PropertyManager();
        manager.setId(idGenerator.generateId("pm"));
        manager.setResidentId(resident.getId());
        manager.setPropertyCompanyId(propertyCompanyId);
        manager.setCommissionRate(resolveRate(request.getCommissionRate()));
        manager.setStatus(status);
        propertyManagerRepository.save(manager);

        return toDetail(manager, resident);
    }

    @Transactional(readOnly = true)
    public PropertyManagerDetailResponse getPropertyManager(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        PropertyManager manager = getAccessibleManager(id);
        Resident resident = residentRepository.findById(manager.getResidentId()).orElse(null);
        return toDetail(manager, resident);
    }

    @Transactional
    public PropertyManagerDetailResponse updatePropertyManager(String id, UpdatePropertyManagerRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        PropertyManager manager = getAccessibleManager(id);

        // 经理不能改自己的比例（防自肥）：当前登录人即该经理本人时禁止调整抽成比例
        if (request.getCommissionRate() != null
                && manager.getResidentId().equals(UserContext.currentUserId())) {
            throw new BizException(ErrorCode.PROPERTY_MANAGER_SELF_RATE_FORBIDDEN);
        }
        if (request.getCommissionRate() != null) {
            manager.setCommissionRate(resolveRate(request.getCommissionRate()));
        }
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            if ("active".equals(request.getStatus()) && !"active".equals(manager.getStatus())) {
                assertUniqueActiveManager(manager.getPropertyCompanyId(), "active", manager.getId());
            }
            manager.setStatus(request.getStatus());
        }
        propertyManagerRepository.save(manager);

        Resident resident = residentRepository.findById(manager.getResidentId()).orElse(null);
        return toDetail(manager, resident);
    }

    @Transactional
    public PropertyManagerDetailResponse revokePropertyManager(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        PropertyManager manager = getAccessibleManager(id);
        manager.setStatus("inactive");
        propertyManagerRepository.save(manager);

        Resident resident = residentRepository.findById(manager.getResidentId()).orElse(null);
        return toDetail(manager, resident);
    }

    private PropertyManager getAccessibleManager(String id) {
        adminSupport.validateIdPrefix(id, "pm_");
        PropertyManager manager = propertyManagerRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_MANAGER_NOT_FOUND));
        adminSupport.assertSameProperty(manager.getPropertyCompanyId());
        return manager;
    }

    /** 同物业仅一名 active 经理（应用层校验）；excludeId 用于更新场景排除自身 */
    private void assertUniqueActiveManager(String propertyCompanyId, String targetStatus, String excludeId) {
        if (!"active".equals(targetStatus)) {
            return;
        }
        propertyManagerRepository
                .findFirstByPropertyCompanyIdAndStatusOrderByIdAsc(propertyCompanyId, "active")
                .ifPresent(existing -> {
                    if (!existing.getId().equals(excludeId)) {
                        throw new BizException(ErrorCode.PROPERTY_MANAGER_ALREADY_EXISTS,
                                "该物业公司已存在在职物业经理，请先停用后再指派");
                    }
                });
    }

    /** 经理抽成比例：默认 0，取值 [0,1)（=1 时楼长净额恒为 0，无业务意义） */
    private BigDecimal resolveRate(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) >= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "commissionRate需在 0~1 之间（不含1）");
        }
        return value;
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !MANAGER_STATUS.contains(status)) {
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

    private PropertyManagerListItemResponse toListItem(PropertyManager manager,
                                                       Resident resident,
                                                       PropertyCompany company) {
        return PropertyManagerListItemResponse.builder()
                .id(manager.getId())
                .residentId(manager.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .residentPhone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(manager.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .commissionRate(manager.getCommissionRate())
                .status(manager.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(manager.getCreatedAt()))
                .build();
    }

    private PropertyManagerDetailResponse toDetail(PropertyManager manager, Resident resident) {
        PropertyCompany company = propertyCompanyRepository.findById(manager.getPropertyCompanyId()).orElse(null);
        return PropertyManagerDetailResponse.builder()
                .id(manager.getId())
                .residentId(manager.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .residentPhone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(manager.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .commissionRate(manager.getCommissionRate())
                .status(manager.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(manager.getCreatedAt()))
                .build();
    }
}
