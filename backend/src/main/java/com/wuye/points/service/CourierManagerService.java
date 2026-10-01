package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.couriermanager.AddCourierRequest;
import com.wuye.points.dto.couriermanager.CourierItemResponse;
import com.wuye.points.dto.couriermanager.CourierListResponse;
import com.wuye.points.dto.couriermanager.CourierManagerDetailResponse;
import com.wuye.points.dto.couriermanager.CourierManagerListItemResponse;
import com.wuye.points.dto.couriermanager.CourierOperationResponse;
import com.wuye.points.dto.couriermanager.CreateCourierManagerRequest;
import com.wuye.points.dto.couriermanager.MyCourierManagerResponse;
import com.wuye.points.dto.couriermanager.UpdateCourierManagerRequest;
import com.wuye.points.entity.CourierManager;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CourierManagerRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.CourierManagerSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.DisplayLabelUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 快递负责人服务：CRUD、快递员管理、工作台。
 */
@Service
@RequiredArgsConstructor
public class CourierManagerService {

    private static final Set<String> MANAGER_STATUS = Set.of("active", "inactive");
    private static final String DEFAULT_STATUS = "active";

    private final CourierManagerRepository courierManagerRepository;
    private final ResidentRepository residentRepository;
    private final AdminSupport adminSupport;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final WithdrawableService withdrawableService;

    // ============== 快递负责人 CRUD ==============

    @Transactional(readOnly = true)
    public PageResponse<CourierManagerListItemResponse> listCourierManagers(
            int page,
            int pageSize,
            String communityId,
            String status,
            String keyword,
            String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateStatus(status);
        if (StringUtils.hasText(communityId)) {
            adminSupport.validateIdPrefix(communityId, "com_");
        }

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<CourierManager> managerPage = courierManagerRepository.findAll(
                CourierManagerSpecifications.withFilters(
                        scopedPropertyCompanyId, communityId, status, keyword),
                pageable);

        Map<String, Resident> residentMap = loadResidents(managerPage.getContent().stream()
                .map(CourierManager::getResidentId)
                .toList());

        List<CourierManagerListItemResponse> list = managerPage.getContent().stream()
                .map(manager -> toListItem(manager, residentMap.get(manager.getResidentId())))
                .toList();

        return PageResponse.of(list, page, pageSize, managerPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public CourierManagerDetailResponse getCourierManager(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(id, "cm_");
        CourierManager manager = getAccessibleManager(id);
        Resident resident = residentRepository.findById(manager.getResidentId()).orElse(null);
        return toDetail(manager, resident);
    }

    @Transactional
    public CourierManagerDetailResponse createCourierManager(CreateCourierManagerRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        String propertyCompanyId = resident.getPropertyCompanyId();

        courierManagerRepository
                .findByResidentIdAndPropertyCompanyIdAndStatus(
                        resident.getId(), propertyCompanyId, DEFAULT_STATUS)
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.COURIER_MANAGER_DUPLICATE);
                });

        CourierManager manager = new CourierManager();
        manager.setId(idGenerator.generateId("cm"));
        manager.setResidentId(resident.getId());
        manager.setPropertyCompanyId(propertyCompanyId);
        manager.setCommunityId(StringUtils.hasText(request.getCommunityId())
                ? request.getCommunityId() : resident.getCommunityId());
        manager.setName(StringUtils.hasText(request.getName()) ? request.getName().trim() : resident.getName());
        manager.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : resident.getPhone());
        manager.setResponsibleArea(request.getResponsibleArea());
        manager.setDeliveryTimeConfig(request.getDeliveryTimeConfig());
        manager.setStatus(DEFAULT_STATUS);
        courierManagerRepository.save(manager);

        return toDetail(manager, resident);
    }

    @Transactional
    public CourierManagerDetailResponse updateCourierManager(String id, UpdateCourierManagerRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(id, "cm_");

        CourierManager manager = getAccessibleManager(id);
        if (StringUtils.hasText(request.getName())) {
            manager.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getPhone())) {
            manager.setPhone(request.getPhone().trim());
        }
        if (request.getCommunityId() != null) {
            if (StringUtils.hasText(request.getCommunityId())) {
                adminSupport.validateIdPrefix(request.getCommunityId(), "com_");
            }
            manager.setCommunityId(request.getCommunityId());
        }
        if (request.getResponsibleArea() != null) {
            manager.setResponsibleArea(request.getResponsibleArea());
        }
        if (request.getDeliveryTimeConfig() != null) {
            manager.setDeliveryTimeConfig(request.getDeliveryTimeConfig());
        }
        if (StringUtils.hasText(request.getStatus())) {
            validateStatus(request.getStatus());
            manager.setStatus(request.getStatus());
        }
        if (request.getShareRate() != null) {
            if (request.getShareRate().compareTo(java.math.BigDecimal.ZERO) < 0
                    || request.getShareRate().compareTo(java.math.BigDecimal.ONE) > 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "shareRate 须在 0~1 之间");
            }
            manager.setShareRate(request.getShareRate());
        }

        courierManagerRepository.save(manager);
        Resident resident = residentRepository.findById(manager.getResidentId()).orElse(null);
        return toDetail(manager, resident);
    }

    @Transactional
    public CourierManagerDetailResponse deleteCourierManager(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(id, "cm_");

        CourierManager manager = getAccessibleManager(id);
        manager.setStatus("inactive");
        courierManagerRepository.save(manager);

        // 解绑所有快递员
        List<Resident> couriers = residentRepository.findByCourierManagerId(manager.getId());
        for (Resident courier : couriers) {
            courier.setCourierManagerId(null);
        }
        residentRepository.saveAll(couriers);

        Resident resident = residentRepository.findById(manager.getResidentId()).orElse(null);
        return toDetail(manager, resident);
    }

    // ============== 快递员管理 ==============

    @Transactional
    public CourierOperationResponse addCourier(String managerId, AddCourierRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(managerId, "cm_");
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");

        CourierManager manager = getAccessibleManager(managerId);
        Resident courier = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(courier.getPropertyCompanyId());

        if (courier.getCourierManagerId() != null
                && !courier.getCourierManagerId().equals(manager.getId())) {
            throw new BizException(ErrorCode.COURIER_ALREADY_BOUND);
        }
        courier.setCourierManagerId(manager.getId());
        courier.setRole("courier");
        residentRepository.save(courier);

        return CourierOperationResponse.builder()
                .courierManagerId(manager.getId())
                .residentId(courier.getId())
                .operation("added")
                .build();
    }

    @Transactional
    public CourierOperationResponse removeCourier(String managerId, String courierResidentId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(managerId, "cm_");
        adminSupport.validateIdPrefix(courierResidentId, "res_");

        CourierManager manager = getAccessibleManager(managerId);
        Resident courier = residentRepository.findByIdAndCourierManagerId(courierResidentId, manager.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "该快递员不在此负责人名下"));

        courier.setCourierManagerId(null);
        residentRepository.save(courier);

        return CourierOperationResponse.builder()
                .courierManagerId(manager.getId())
                .residentId(courier.getId())
                .operation("removed")
                .build();
    }

    @Transactional(readOnly = true)
    public CourierListResponse listCouriers(String managerId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(managerId, "cm_");

        CourierManager manager = getAccessibleManager(managerId);
        List<Resident> couriers = residentRepository.findByCourierManagerId(manager.getId());
        List<CourierItemResponse> items = couriers.stream()
                .map(this::toCourierItem)
                .toList();

        return CourierListResponse.builder()
                .courierManagerId(manager.getId())
                .couriers(items)
                .total((long) items.size())
                .build();
    }

    // ============== 快递负责人工作台 ==============

    @Transactional(readOnly = true)
    public MyCourierManagerResponse getMyCourierManager() {
        roleGuard.requireAnyRole("courier");
        String residentId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(residentId) || !StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.COURIER_MANAGER_NOT_FOUND);
        }

        // 快递负责人：汇总名下配送员 + 本人已完成配送报酬
        CourierManager manager = courierManagerRepository
                .findByResidentIdAndPropertyCompanyIdAndStatus(residentId, propertyCompanyId, DEFAULT_STATUS)
                .orElse(null);
        if (manager != null) {
            long courierCount = residentRepository.countByCourierManagerId(manager.getId());
            WithdrawableService.CourierWithdrawable w = withdrawableService.calcCourierManagerWithdrawable(manager);
            return MyCourierManagerResponse.builder()
                    .id(manager.getId())
                    .residentId(manager.getResidentId())
                    .propertyCompanyId(manager.getPropertyCompanyId())
                    .communityId(manager.getCommunityId())
                    .name(manager.getName())
                    .phone(manager.getPhone())
                    .responsibleArea(manager.getResponsibleArea())
                    .deliveryTimeConfig(manager.getDeliveryTimeConfig())
                    .courierCount(courierCount)
                    .withdrawableAmount(w.withdrawableAmount())
                    .totalWithdrawn(w.totalWithdrawn())
                    .withdrawalBlocked(w.withdrawalBlocked())
                    .status(manager.getStatus())
                    .createdAt(DateTimeUtil.toIsoUtc(manager.getCreatedAt()))
                    .build();
        }

        // 普通配送员：本人已完成配送报酬即可提现
        Resident courier = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!"courier".equals(courier.getRole())) {
            throw new BizException(ErrorCode.COURIER_MANAGER_NOT_FOUND, "当前用户非配送员");
        }
        WithdrawableService.CourierWithdrawable w = withdrawableService.calcCourierResidentWithdrawable(residentId);
        return MyCourierManagerResponse.builder()
                .id(courier.getId())
                .residentId(courier.getId())
                .propertyCompanyId(courier.getPropertyCompanyId())
                .communityId(courier.getCommunityId())
                .name(courier.getName())
                .phone(courier.getPhone())
                .responsibleArea(null)
                .deliveryTimeConfig(null)
                .courierCount(0L)
                .withdrawableAmount(w.withdrawableAmount())
                .totalWithdrawn(w.totalWithdrawn())
                .withdrawalBlocked(w.withdrawalBlocked())
                .status(StringUtils.hasText(courier.getStatus()) ? courier.getStatus() : "active")
                .createdAt(DateTimeUtil.toIsoUtc(courier.getCreatedAt()))
                .build();
    }

    // ============== 私有工具 ==============

    private CourierManager getAccessibleManager(String id) {
        CourierManager manager = courierManagerRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COURIER_MANAGER_NOT_FOUND));
        adminSupport.assertSameProperty(manager.getPropertyCompanyId());
        return manager;
    }

    private Map<String, Resident> loadResidents(List<String> residentIds) {
        if (residentIds == null || residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, resident -> resident));
    }

    private long countCouriers(String managerId) {
        return residentRepository.countByCourierManagerId(managerId);
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !MANAGER_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }
    }

    private CourierManagerListItemResponse toListItem(CourierManager manager, Resident resident) {
        String statusCode = manager.getStatus();
        String statusLabel = DisplayLabelUtil.leaderStatusLabel(statusCode);
        long courierCount = countCouriers(manager.getId());

        return CourierManagerListItemResponse.builder()
                .id(manager.getId())
                .residentId(manager.getResidentId())
                .propertyCompanyId(manager.getPropertyCompanyId())
                .communityId(manager.getCommunityId())
                .name(StringUtils.hasText(manager.getName()) ? manager.getName()
                        : (resident != null ? resident.getName() : null))
                .phone(StringUtils.hasText(manager.getPhone()) ? manager.getPhone()
                        : (resident != null ? resident.getPhone() : null))
                .responsibleArea(manager.getResponsibleArea())
                .courierCount(courierCount)
                .statusCode(statusCode)
                .statusLabel(statusLabel)
                .status(DisplayLabelUtil.displayOrCode(statusCode, statusLabel))
                .createdAt(DateTimeUtil.toIsoUtc(manager.getCreatedAt()))
                .build();
    }

    private CourierManagerDetailResponse toDetail(CourierManager manager, Resident resident) {
        String statusCode = manager.getStatus();
        String statusLabel = DisplayLabelUtil.leaderStatusLabel(statusCode);
        long courierCount = countCouriers(manager.getId());

        return CourierManagerDetailResponse.builder()
                .id(manager.getId())
                .residentId(manager.getResidentId())
                .propertyCompanyId(manager.getPropertyCompanyId())
                .communityId(manager.getCommunityId())
                .name(StringUtils.hasText(manager.getName()) ? manager.getName()
                        : (resident != null ? resident.getName() : null))
                .phone(StringUtils.hasText(manager.getPhone()) ? manager.getPhone()
                        : (resident != null ? resident.getPhone() : null))
                .responsibleArea(manager.getResponsibleArea())
                .deliveryTimeConfig(manager.getDeliveryTimeConfig())
                .courierCount(courierCount)
                .statusCode(statusCode)
                .statusLabel(statusLabel)
                .status(DisplayLabelUtil.displayOrCode(statusCode, statusLabel))
                .shareRate(manager.getShareRate())
                .createdAt(DateTimeUtil.toIsoUtc(manager.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(manager.getUpdatedAt()))
                .build();
    }

    private CourierItemResponse toCourierItem(Resident resident) {
        return CourierItemResponse.builder()
                .residentId(resident.getId())
                .name(resident.getName())
                .phone(resident.getPhone())
                .communityId(resident.getCommunityId())
                .building(resident.getBuilding())
                .unit(resident.getUnit())
                .room(resident.getRoom())
                .status(resident.getStatus())
                .boundAt(DateTimeUtil.toIsoUtc(resident.getUpdatedAt()))
                .build();
    }
}
