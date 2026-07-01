package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.service.ServiceOrderDetailResponse;
import com.wuye.points.dto.service.ServiceOrderListItemResponse;
import com.wuye.points.dto.service.UpdateServiceOrderStatusRequest;
import com.wuye.points.dto.service.UpdateServiceOrderStatusResponse;
import com.wuye.points.entity.CommunityService;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ServiceOrder;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityServiceRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.ServiceOrderRepository;
import com.wuye.points.repository.spec.ServiceOrderSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "serviceTime", "amount");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
            "pending", Set.of("confirmed", "cancelled"),
            "confirmed", Set.of("in_progress", "cancelled"),
            "in_progress", Set.of("completed", "cancelled"),
            "completed", Set.of(),
            "cancelled", Set.of());

    private final ServiceOrderRepository serviceOrderRepository;
    private final CommunityServiceRepository communityServiceRepository;
    private final ResidentRepository residentRepository;
    private final CommunityServiceService communityServiceService;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public PageResponse<ServiceOrderListItemResponse> listServiceOrders(
            int page,
            int pageSize,
            String orderStatus,
            Boolean asProvider,
            String sort) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateOrderStatus(orderStatus);

        String propertyCompanyId = null;
        String residentId = null;
        String providerId = null;

        if (roleGuard.isPropertyAdmin()) {
            propertyCompanyId = UserContext.currentPropertyCompanyId();
        } else if (roleGuard.isPlatformAdmin()) {
            propertyCompanyId = null;
        } else {
            Resident viewer = getActiveResident(UserContext.currentUserId());
            propertyCompanyId = viewer.getPropertyCompanyId();
            if (Boolean.TRUE.equals(asProvider)) {
                providerId = viewer.getId();
            } else {
                residentId = viewer.getId();
            }
        }

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<ServiceOrder> orderPage = serviceOrderRepository.findAll(
                ServiceOrderSpecifications.withFilters(propertyCompanyId, residentId, providerId, orderStatus),
                pageable);

        return buildOrderListPage(orderPage, page, pageSize);
    }

    @Transactional(readOnly = true)
    public ServiceOrderDetailResponse getServiceOrder(String id) {
        validateOrderId(id);
        ServiceOrder order = getReadableOrder(id);
        CommunityService service = communityServiceRepository.findById(order.getServiceId()).orElse(null);
        Resident resident = residentRepository.findById(order.getResidentId()).orElse(null);
        Resident provider = StringUtils.hasText(order.getProviderId())
                ? residentRepository.findById(order.getProviderId()).orElse(null)
                : null;
        return communityServiceService.toOrderDetail(order, service, resident, provider);
    }

    @Transactional
    public UpdateServiceOrderStatusResponse updateStatus(String id, UpdateServiceOrderStatusRequest request) {
        validateOrderId(id);
        validateOrderStatus(request.getOrderStatus());

        ServiceOrder order = getManageableOrder(id);
        assertStatusTransition(order.getOrderStatus(), request.getOrderStatus());
        assertRoleCanSetStatus(order, request.getOrderStatus());
        order.setOrderStatus(request.getOrderStatus());
        serviceOrderRepository.save(order);

        return UpdateServiceOrderStatusResponse.builder()
                .id(order.getId())
                .orderStatus(order.getOrderStatus())
                .updatedAt(DateTimeUtil.toIsoUtc(order.getUpdatedAt()))
                .build();
    }

    private PageResponse<ServiceOrderListItemResponse> buildOrderListPage(
            Page<ServiceOrder> orderPage,
            int page,
            int pageSize) {
        List<ServiceOrder> orders = orderPage.getContent();
        Map<String, CommunityService> serviceMap = communityServiceRepository.findAllById(
                orders.stream().map(ServiceOrder::getServiceId).distinct().toList()).stream()
                .collect(Collectors.toMap(CommunityService::getId, Function.identity(), (a, b) -> a));
        Map<String, Resident> residentMap = residentRepository.findAllById(
                orders.stream().map(ServiceOrder::getResidentId).distinct().toList()).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));

        List<ServiceOrderListItemResponse> list = orders.stream()
                .map(order -> {
                    CommunityService service = serviceMap.get(order.getServiceId());
                    Resident resident = residentMap.get(order.getResidentId());
                    return ServiceOrderListItemResponse.builder()
                            .id(order.getId())
                            .serviceId(order.getServiceId())
                            .serviceName(service != null ? service.getName() : null)
                            .residentId(order.getResidentId())
                            .residentName(resident != null ? resident.getName() : null)
                            .providerId(order.getProviderId())
                            .orderStatus(order.getOrderStatus())
                            .amount(order.getAmount())
                            .paymentMethod(order.getPaymentMethod())
                            .serviceTime(DateTimeUtil.toIsoUtc(order.getServiceTime()))
                            .address(order.getAddress())
                            .contactPhone(order.getContactPhone())
                            .createdAt(DateTimeUtil.toIsoUtc(order.getCreatedAt()))
                            .build();
                })
                .toList();

        return PageResponse.of(list, page, pageSize, orderPage.getTotalElements());
    }

    private ServiceOrder getReadableOrder(String id) {
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.SERVICE_ORDER_NOT_FOUND));

        if (roleGuard.isPlatformAdmin()) {
            return order;
        }
        if (roleGuard.isPropertyAdmin()) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (propertyCompanyId != null && propertyCompanyId.equals(order.getPropertyCompanyId())) {
                return order;
            }
            throw new BizException(ErrorCode.SERVICE_ORDER_NOT_FOUND);
        }

        String currentUserId = UserContext.currentUserId();
        if (currentUserId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        if (currentUserId.equals(order.getResidentId()) || currentUserId.equals(order.getProviderId())) {
            return order;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private ServiceOrder getManageableOrder(String id) {
        ServiceOrder order = getReadableOrder(id);
        String currentUserId = UserContext.currentUserId();
        if (roleGuard.isPropertyAdmin() || roleGuard.isPlatformAdmin()) {
            return order;
        }
        if (currentUserId != null
                && (currentUserId.equals(order.getProviderId()) || currentUserId.equals(order.getResidentId()))) {
            return order;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertRoleCanSetStatus(ServiceOrder order, String targetStatus) {
        String currentUserId = UserContext.currentUserId();
        if (roleGuard.isPropertyAdmin() || roleGuard.isPlatformAdmin()) {
            return;
        }
        if (currentUserId != null && currentUserId.equals(order.getProviderId())) {
            return;
        }
        if (currentUserId != null
                && currentUserId.equals(order.getResidentId())
                && "cancelled".equals(targetStatus)) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertStatusTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null || currentStatus.equals(targetStatus)) {
            return;
        }
        Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "订单状态不允许操作");
        }
    }

    private void validateOrderId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("so_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "服务订单ID格式不正确");
        }
    }

    private void validateOrderStatus(String orderStatus) {
        if (StringUtils.hasText(orderStatus) && !RoleGuard.SERVICE_ORDER_STATUS.contains(orderStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "orderStatus值无效");
        }
    }

    private Resident getActiveResident(String userId) {
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = Sort.Direction.DESC;
        String field = sort;
        if (sort.startsWith("-")) {
            field = sort.substring(1);
        } else if (sort.startsWith("+")) {
            field = sort.substring(1);
            direction = Sort.Direction.ASC;
        }
        if (!SORTABLE_FIELDS.contains(field)) {
            field = "createdAt";
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }
}
