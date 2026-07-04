package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.delivery.AssignDeliveryRequest;
import com.wuye.points.dto.delivery.AssignDeliveryResponse;
import com.wuye.points.dto.delivery.CompleteDeliveryRequest;
import com.wuye.points.dto.delivery.CompleteDeliveryResponse;
import com.wuye.points.dto.delivery.GrabDeliveryResponse;
import com.wuye.points.dto.delivery.MyDeliveryItemResponse;
import com.wuye.points.dto.delivery.PendingDeliveryItemResponse;
import com.wuye.points.dto.delivery.UpdateDeliveryStatusRequest;
import com.wuye.points.dto.delivery.UpdateDeliveryStatusResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DeliveryRule;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DeliveryRuleRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.DeliverySpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "acceptedAt", "deliveredAt");
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;
    private static final BigDecimal DEFAULT_COURIER_EARNING = new BigDecimal("0.35");

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRuleRepository deliveryRuleRepository;
    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final DeliveryConverter deliveryConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public void createDeliveryForPaidOrder(Order order, Merchant merchant) {
        if (deliveryRepository.findByOrderId(order.getId()).isPresent()) {
            return;
        }

        DeliveryRule rule = loadDeliveryRule(order.getPropertyCompanyId());
        BigDecimal courierEarning = resolveCourierEarning(rule);

        Delivery delivery = new Delivery();
        delivery.setId(idGenerator.generateId("dlv"));
        delivery.setOrderId(order.getId());
        delivery.setPropertyCompanyId(order.getPropertyCompanyId());
        delivery.setStatus("pending");
        delivery.setFee(resolveDeliveryFee(order, merchant, rule));
        delivery.setCourierEarning(courierEarning);
        delivery.setPickupAddress(merchant != null ? merchant.getAddress() : null);
        delivery.setDeliveryAddress(order.getAddress());
        delivery.setContactPhone(order.getContactPhone());
        deliveryRepository.save(delivery);
    }

    private BigDecimal resolveDeliveryFee(Order order, Merchant merchant, DeliveryRule rule) {
        if (merchant != null && merchant.getFreeDeliveryThreshold() != null
                && order.getTotalAmount() != null
                && order.getTotalAmount().compareTo(merchant.getFreeDeliveryThreshold()) >= 0) {
            return BigDecimal.ZERO;
        }
        if (merchant != null && merchant.getDeliveryFee() != null) {
            return merchant.getDeliveryFee();
        }
        return defaultDecimal(order.getDeliveryFee());
    }

    @Transactional
    public void failDeliveryForCancelledOrder(String orderId) {
        deliveryRepository.findByOrderId(orderId).ifPresent(delivery -> {
            if ("completed".equals(delivery.getStatus())) {
                return;
            }
            delivery.setStatus("failed");
            deliveryRepository.save(delivery);
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<PendingDeliveryItemResponse> listPending(
            int page,
            int pageSize,
            String communityId,
            String sort) {
        roleGuard.requireAnyRole("courier");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateCommunityId(communityId);

        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<Delivery> deliveryPage = deliveryRepository.findAll(
                DeliverySpecifications.pendingPaidOrders(propertyCompanyId, communityId),
                pageable);

        int timeoutMinutes = resolveTimeoutMinutes(propertyCompanyId);
        return buildPendingPageResponse(deliveryPage, page, pageSize, timeoutMinutes);
    }

    @Transactional
    public GrabDeliveryResponse grabDelivery(String id) {
        roleGuard.requireAnyRole("courier");
        validateDeliveryId(id);

        String courierId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(courierId) || !StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }

        Delivery delivery = deliveryRepository.findByIdAndPropertyCompanyId(id, propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "配送不存在"));
        if (!"pending".equals(delivery.getStatus()) && !"property_fallback".equals(delivery.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "配送状态不允许抢单");
        }

        DeliveryRule rule = loadDeliveryRule(propertyCompanyId);
        BigDecimal courierEarning = resolveCourierEarning(rule);
        LocalDateTime acceptedAt = LocalDateTime.now();

        int updated;
        if ("pending".equals(delivery.getStatus())) {
            updated = deliveryRepository.grabIfPending(id, courierId, acceptedAt, courierEarning);
        } else {
            delivery.setCourierId(courierId);
            delivery.setStatus("accepted");
            delivery.setAcceptedAt(acceptedAt);
            delivery.setCourierEarning(courierEarning);
            deliveryRepository.save(delivery);
            updated = 1;
        }

        if (updated == 0) {
            throw new BizException(ErrorCode.DELIVERY_GRAB_FAILED);
        }

        Delivery saved = deliveryRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "配送不存在"));
        syncOrderCourier(saved.getOrderId(), courierId);

        int timeoutMinutes = resolveTimeoutMinutes(propertyCompanyId);
        return GrabDeliveryResponse.builder()
                .id(saved.getId())
                .orderId(saved.getOrderId())
                .courierId(saved.getCourierId())
                .status(saved.getStatus())
                .courierEarning(defaultDecimal(saved.getCourierEarning()))
                .acceptedAt(DateTimeUtil.toIsoUtc(saved.getAcceptedAt()))
                .timeoutAt(DateTimeUtil.toIsoUtc(saved.getAcceptedAt().plusMinutes(timeoutMinutes)))
                .build();
    }

    @Transactional
    public CompleteDeliveryResponse completeDelivery(String id, CompleteDeliveryRequest request) {
        roleGuard.requireAnyRole("courier");
        validateDeliveryId(id);
        if (request != null && request.getProofImageUrls() != null && request.getProofImageUrls().size() > 5) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "配送凭证最多5张");
        }

        String courierId = UserContext.currentUserId();
        Delivery delivery = getDeliveryInScope(id);
        assertAssignedCourier(delivery, courierId);

        if (!"accepted".equals(delivery.getStatus()) && !"delivering".equals(delivery.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "配送状态不允许完成");
        }

        LocalDateTime now = LocalDateTime.now();
        delivery.setStatus("completed");
        delivery.setDeliveredAt(now);
        deliveryRepository.save(delivery);

        syncOrderAfterDeliveryComplete(delivery.getOrderId(), now);

        return CompleteDeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .status(delivery.getStatus())
                .courierEarning(defaultDecimal(delivery.getCourierEarning()))
                .deliveredAt(DateTimeUtil.toIsoUtc(delivery.getDeliveredAt()))
                .build();
    }

    @Transactional
    public UpdateDeliveryStatusResponse updateStatus(String id, UpdateDeliveryStatusRequest request) {
        roleGuard.requireAnyRole("courier", "property_admin");
        validateDeliveryId(id);
        validateDeliveryStatus(request.getStatus());

        Delivery delivery = getDeliveryInScope(id);
        if (roleGuard.isCourier()) {
            assertAssignedCourier(delivery, UserContext.currentUserId());
        }

        String targetStatus = request.getStatus();
        if ("delivering".equals(targetStatus)) {
            if (!"accepted".equals(delivery.getStatus()) && !"property_fallback".equals(delivery.getStatus())) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "配送状态不允许更新");
            }
            delivery.setStatus("delivering");
            delivery.setPickedAt(LocalDateTime.now());
            syncOrderDelivering(delivery.getOrderId(), delivery.getCourierId());
        } else if ("failed".equals(targetStatus)) {
            if (roleGuard.isCourier()) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            delivery.setStatus("failed");
        } else {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "不支持的状态变更");
        }

        deliveryRepository.save(delivery);
        return UpdateDeliveryStatusResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .status(delivery.getStatus())
                .pickedAt(DateTimeUtil.toIsoUtc(delivery.getPickedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(delivery.getUpdatedAt()))
                .build();
    }

    @Transactional
    public AssignDeliveryResponse assignDelivery(String id, AssignDeliveryRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateDeliveryId(id);
        validateResidentId(request.getCourierId());

        Delivery delivery = getDeliveryInScope(id);
        if (!"pending".equals(delivery.getStatus()) && !"property_fallback".equals(delivery.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "配送状态不允许指派");
        }

        Resident courier = residentRepository.findById(request.getCourierId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!"courier".equals(courier.getRole())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "指派对象不是快递员");
        }
        roleGuard.assertSamePropertyOrPlatform(courier.getPropertyCompanyId());
        if (!delivery.getPropertyCompanyId().equals(courier.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "快递员不在同一物业");
        }

        DeliveryRule rule = loadDeliveryRule(delivery.getPropertyCompanyId());
        LocalDateTime acceptedAt = LocalDateTime.now();
        delivery.setCourierId(courier.getId());
        delivery.setStatus("accepted");
        delivery.setAcceptedAt(acceptedAt);
        delivery.setCourierEarning(resolveCourierEarning(rule));
        deliveryRepository.save(delivery);

        syncOrderCourier(delivery.getOrderId(), courier.getId());

        return AssignDeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .courierId(delivery.getCourierId())
                .status(delivery.getStatus())
                .courierEarning(defaultDecimal(delivery.getCourierEarning()))
                .acceptedAt(DateTimeUtil.toIsoUtc(delivery.getAcceptedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<MyDeliveryItemResponse> listMyDeliveries(
            int page,
            int pageSize,
            String status,
            String sort) {
        roleGuard.requireAnyRole("courier");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateDeliveryStatus(status);

        String courierId = UserContext.currentUserId();
        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<Delivery> deliveryPage;

        if (StringUtils.hasText(status)) {
            deliveryPage = deliveryRepository.findAll(
                    DeliverySpecifications.withFilters(null, courierId, status, null, null),
                    pageable);
        } else {
            deliveryPage = deliveryRepository.findAll(
                    DeliverySpecifications.withFilters(
                            null, courierId, null, null,
                            List.of("accepted", "delivering", "completed")),
                    pageable);
        }

        return buildMyPageResponse(deliveryPage, page, pageSize, false);
    }

    @Transactional
    public int processPropertyFallback() {
        List<Delivery> pendingDeliveries = deliveryRepository.findPendingBefore(
                LocalDateTime.now().minusMinutes(DEFAULT_TIMEOUT_MINUTES));

        int count = 0;
        for (Delivery delivery : pendingDeliveries) {
            DeliveryRule rule = loadDeliveryRule(delivery.getPropertyCompanyId());
            int timeoutMinutes = rule != null && rule.getTimeoutMinutes() != null
                    ? rule.getTimeoutMinutes()
                    : DEFAULT_TIMEOUT_MINUTES;
            if (delivery.getCreatedAt().plusMinutes(timeoutMinutes).isAfter(LocalDateTime.now())) {
                continue;
            }
            delivery.setStatus("property_fallback");
            delivery.setCourierId(null);
            deliveryRepository.save(delivery);
            count++;
        }
        return count;
    }

    private PageResponse<PendingDeliveryItemResponse> buildPendingPageResponse(
            Page<Delivery> deliveryPage,
            int page,
            int pageSize,
            int timeoutMinutes) {
        List<String> orderIds = deliveryPage.getContent().stream()
                .map(Delivery::getOrderId)
                .distinct()
                .toList();
        Map<String, Order> orderMap = loadOrderMap(orderIds);
        Map<String, Merchant> merchantMap = loadMerchantMap(orderMap);

        List<PendingDeliveryItemResponse> list = deliveryPage.getContent().stream()
                .map(delivery -> {
                    Order order = orderMap.get(delivery.getOrderId());
                    Merchant merchant = order != null ? merchantMap.get(order.getMerchantId()) : null;
                    return deliveryConverter.toPendingItem(delivery, order, merchant, timeoutMinutes);
                })
                .toList();

        return PageResponse.of(list, page, pageSize, deliveryPage.getTotalElements());
    }

    private PageResponse<MyDeliveryItemResponse> buildMyPageResponse(
            Page<Delivery> deliveryPage,
            int page,
            int pageSize,
            boolean maskPhone) {
        List<String> orderIds = deliveryPage.getContent().stream()
                .map(Delivery::getOrderId)
                .distinct()
                .toList();
        Map<String, Order> orderMap = loadOrderMap(orderIds);
        Map<String, Merchant> merchantMap = loadMerchantMap(orderMap);

        List<MyDeliveryItemResponse> list = deliveryPage.getContent().stream()
                .map(delivery -> {
                    Order order = orderMap.get(delivery.getOrderId());
                    Merchant merchant = order != null ? merchantMap.get(order.getMerchantId()) : null;
                    return deliveryConverter.toMyItem(delivery, order, merchant, maskPhone);
                })
                .toList();

        return PageResponse.of(list, page, pageSize, deliveryPage.getTotalElements());
    }

    private Map<String, Order> loadOrderMap(List<String> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return orderRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));
    }

    private Map<String, Merchant> loadMerchantMap(Map<String, Order> orderMap) {
        List<String> merchantIds = orderMap.values().stream()
                .map(Order::getMerchantId)
                .distinct()
                .toList();
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity()));
    }

    private Delivery getDeliveryInScope(String id) {
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (roleGuard.isPlatformAdmin()) {
            return deliveryRepository.findById(id)
                    .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "配送不存在"));
        }
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return deliveryRepository.findByIdAndPropertyCompanyId(id, propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "配送不存在"));
    }

    private void assertAssignedCourier(Delivery delivery, String courierId) {
        if (courierId == null || !courierId.equals(delivery.getCourierId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非接单人无法操作");
        }
    }

    private void syncOrderCourier(String orderId, String courierId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setCourierId(courierId);
            orderRepository.save(order);
        });
    }

    private void syncOrderDelivering(String orderId, String courierId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if ("paid".equals(order.getOrderStatus()) || "delivering".equals(order.getOrderStatus())) {
                order.setOrderStatus("delivering");
                if (StringUtils.hasText(courierId)) {
                    order.setCourierId(courierId);
                }
                orderRepository.save(order);
            }
        });
    }

    private void syncOrderAfterDeliveryComplete(String orderId, LocalDateTime deliveredAt) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if ("paid".equals(order.getOrderStatus()) || "delivering".equals(order.getOrderStatus())) {
                order.setOrderStatus("delivering");
                order.setDeliveredAt(deliveredAt);
                orderRepository.save(order);
            }
        });
    }

    private DeliveryRule loadDeliveryRule(String propertyCompanyId) {
        return deliveryRuleRepository.findByPropertyCompanyId(propertyCompanyId).orElse(null);
    }

    private BigDecimal resolveCourierEarning(DeliveryRule rule) {
        if (rule != null && rule.getCourierPerOrder() != null) {
            return rule.getCourierPerOrder();
        }
        return DEFAULT_COURIER_EARNING;
    }

    private int resolveTimeoutMinutes(String propertyCompanyId) {
        DeliveryRule rule = loadDeliveryRule(propertyCompanyId);
        if (rule != null && rule.getTimeoutMinutes() != null) {
            return rule.getTimeoutMinutes();
        }
        return DEFAULT_TIMEOUT_MINUTES;
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        int safePage = Math.max(page, 1);
        Sort sortOrder = parseSort(sort);
        return PageRequest.of(safePage - 1, pageSize, sortOrder);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        boolean desc = sort.startsWith("-");
        String field = desc ? sort.substring(1) : sort;
        if (!SORTABLE_FIELDS.contains(field)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "不支持的排序字段");
        }
        return desc ? Sort.by(Sort.Direction.DESC, field) : Sort.by(Sort.Direction.ASC, field);
    }

    private void validateDeliveryId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("dlv_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "配送ID格式不正确");
        }
    }

    private void validateResidentId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "courierId格式不正确");
        }
    }

    private void validateCommunityId(String communityId) {
        if (StringUtils.hasText(communityId) && !communityId.startsWith("com_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "communityId格式不正确");
        }
    }

    private void validateDeliveryStatus(String status) {
        if (StringUtils.hasText(status) && !RoleGuard.DELIVERY_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "配送状态不合法");
        }
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
