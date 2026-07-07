package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.order.CancelOrderRequest;
import com.wuye.points.dto.order.CancelOrderResponse;
import com.wuye.points.dto.order.ConfirmOrderResponse;
import com.wuye.points.dto.order.CreateOrderRequest;
import com.wuye.points.dto.order.CreateOrderResponse;
import com.wuye.points.dto.order.OrderDetailResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.dto.order.PayOrderRequest;
import com.wuye.points.dto.order.PayOrderResponse;
import com.wuye.points.dto.order.UpdateOrderStatusRequest;
import com.wuye.points.dto.order.UpdateOrderStatusResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.CoinType;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderItemRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.OrderSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "totalAmount", "paidAt");
    private static final BigDecimal COMMISSION_REBATE_THRESHOLD = new BigDecimal("0.2000");
    private static final int LIST_ITEM_PREVIEW_LIMIT = 5;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final ProductRepository productRepository;
    private final ResidentRepository residentRepository;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;
    private final OrderConverter orderConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final DeliveryService deliveryService;
    private final DistributionService distributionService;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        roleGuard.requireAnyRole("resident");
        validateCreateRequest(request);

        Resident resident = getActiveResident(UserContext.currentUserId());
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        assertMerchantAvailable(merchant);
        loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());

        List<PreparedItem> preparedItems = prepareItems(request.getItems(), merchant.getId(), resident);
        BigDecimal subtotal = preparedItems.stream()
                .map(PreparedItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryFee = calculateDeliveryFee(merchant, subtotal);
        BigDecimal totalAmount = subtotal;

        if (StringUtils.hasText(request.getFamilyId())) {
            if (!request.getFamilyId().equals(resident.getFamilyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员无法使用家庭积分");
            }
        }

        Order order = new Order();
        order.setId(idGenerator.generateId("ord"));
        order.setOrderNo(generateOrderNo());
        order.setResidentId(resident.getId());
        order.setMerchantId(merchant.getId());
        order.setPropertyCompanyId(resident.getPropertyCompanyId());
        order.setFamilyId(StringUtils.hasText(request.getFamilyId()) ? request.getFamilyId() : resident.getFamilyId());
        order.setOrderStatus("pending");
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        order.setPointEarned(0);
        order.setCoinEarned(BigDecimal.ZERO);
        order.setAddress(StringUtils.hasText(request.getAddress()) ? request.getAddress() : buildDefaultAddress(resident));
        order.setContactPhone(StringUtils.hasText(request.getContactPhone()) ? request.getContactPhone() : resident.getPhone());
        order.setRemark(request.getRemark());
        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (PreparedItem prepared : preparedItems) {
            OrderItem item = new OrderItem();
            item.setId(idGenerator.generateId("oi"));
            item.setOrderId(order.getId());
            item.setProductId(prepared.product().getId());
            item.setProductName(prepared.product().getName());
            item.setProductCover(prepared.product().getCoverUrl());
            item.setQuantity(prepared.quantity());
            item.setUnitPrice(prepared.unitPrice());
            item.setTotalPrice(prepared.totalPrice());
            orderItems.add(item);
        }
        orderItemRepository.saveAll(orderItems);

        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .merchantId(merchant.getId())
                .merchantName(merchant.getName())
                .totalAmount(totalAmount)
                .deliveryFee(deliveryFee)
                .paymentMethod(null)
                .pointUsed(0)
                .coinUsed(BigDecimal.ZERO)
                .cashAmount(BigDecimal.ZERO)
                .orderStatus(order.getOrderStatus())
                .createdAt(DateTimeUtil.toIsoUtc(order.getCreatedAt()))
                .build();
    }

    @Transactional
    public PayOrderResponse payOrder(String id, PayOrderRequest request) {
        roleGuard.requireAnyRole("resident");
        if (!id.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }
        validatePaymentMethod(request.getPaymentMethod());

        Resident resident = getActiveResident(UserContext.currentUserId());
        Order order = getOrderForUpdate(id);
        assertOrderOwner(order, resident.getId());
        if (!"pending".equals(order.getOrderStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        Merchant merchant = merchantRepository.findById(order.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        MerchantPropertyConfig config = loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        deductStock(items);

        BigDecimal payable = order.getTotalAmount().add(defaultDecimal(order.getDeliveryFee()));
        boolean useFamilyPool = resolveUseFamilyPool(request.getFamilyId(), order, resident);

        PaymentBreakdown breakdown = resolvePaymentBreakdown(request, payable, config);
        executePayment(resident, order, breakdown, useFamilyPool);

        int pointEarned = calculatePointEarned(breakdown, config);
        BigDecimal coinEarned = calculateCoinEarned(breakdown, config);

        if (pointEarned > 0) {
            pointService.earnPoints(resident, pointEarned, "order", order.getId(),
                    "订单消费返积分", null);
        }
        if (coinEarned.compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.earnCoins(resident, coinEarned, CoinType.earn.name(), "order",
                    order.getId(), "订单物业币返现", null);
        }

        order.setPaymentMethod(request.getPaymentMethod());
        order.setPointUsed(breakdown.pointUsed());
        order.setCoinUsed(breakdown.coinUsed());
        order.setCashAmount(breakdown.cashAmount());
        order.setPointEarned(pointEarned);
        order.setCoinEarned(coinEarned);
        order.setOrderStatus("paid");
        order.setPaidAt(LocalDateTime.now());
        if (StringUtils.hasText(request.getFamilyId())) {
            order.setFamilyId(request.getFamilyId());
        }

        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        deliveryService.createDeliveryForPaidOrder(order, merchant);

        return PayOrderResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .paidAmount(payable)
                .pointUsed(order.getPointUsed())
                .coinUsed(order.getCoinUsed())
                .cashAmount(order.getCashAmount())
                .paidAt(DateTimeUtil.toIsoUtc(order.getPaidAt()))
                .pointEarned(pointEarned)
                .coinEarned(coinEarned)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderListItemResponse> listMyOrders(
            int page,
            int pageSize,
            String orderStatus,
            String merchantId,
            String startDate,
            String endDate,
            String keyword,
            String sort) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateOrderStatus(orderStatus);
        validateMerchantId(merchantId);

        Resident resident = getActiveResident(UserContext.currentUserId());
        Pageable pageable = buildPageable(page, pageSize, sort);
        LocalDateTime start = parseIsoDate(startDate);
        LocalDateTime end = parseIsoDate(endDate);

        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecifications.withFilters(
                        resident.getPropertyCompanyId(),
                        resident.getId(),
                        merchantId,
                        orderStatus,
                        start,
                        end,
                        normalizeKeyword(keyword)),
                pageable);

        return buildPageResponse(orderPage, page, pageSize, LIST_ITEM_PREVIEW_LIMIT);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderListItemResponse> listOrders(
            int page,
            int pageSize,
            String orderStatus,
            String merchantId,
            String residentId,
            String propertyCompanyId,
            String startDate,
            String endDate,
            String keyword,
            String sort) {
        roleGuard.requireAnyRole("merchant", "property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateOrderStatus(orderStatus);
        validateMerchantId(merchantId);

        String scopedPropertyCompanyId = resolveAdminPropertyCompanyId(propertyCompanyId);
        String scopedMerchantId = resolveAdminMerchantId(merchantId);

        Pageable pageable = buildPageable(page, pageSize, sort);
        LocalDateTime start = parseIsoDate(startDate);
        LocalDateTime end = parseIsoDate(endDate);

        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecifications.withFilters(
                        scopedPropertyCompanyId,
                        residentId,
                        scopedMerchantId,
                        orderStatus,
                        start,
                        end,
                        normalizeKeyword(keyword)),
                pageable);

        return buildPageResponse(orderPage, page, pageSize, LIST_ITEM_PREVIEW_LIMIT);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(String id) {
        if (!id.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND));
        assertCanViewOrder(order);

        Merchant merchant = merchantRepository.findById(order.getMerchantId()).orElse(null);
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        boolean maskPhone = !isOrderOwner(order) && !roleGuard.isPlatformAdmin() && !roleGuard.isPropertyAdmin();

        return orderConverter.toDetail(order, merchant, items, maskPhone);
    }

    @Transactional
    public CancelOrderResponse cancelOrder(String id, CancelOrderRequest request) {
        roleGuard.requireAnyRole("resident");
        if (!id.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }

        Resident resident = getActiveResident(UserContext.currentUserId());
        Order order = getOrderForUpdate(id);
        assertOrderOwner(order, resident.getId());

        if ("completed".equals(order.getOrderStatus())
                || "cancelled".equals(order.getOrderStatus())
                || "refunded".equals(order.getOrderStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        BigDecimal refundCoin = BigDecimal.ZERO;
        int refundPoint = 0;
        BigDecimal refundAmount = BigDecimal.ZERO;

        if ("paid".equals(order.getOrderStatus()) || "delivering".equals(order.getOrderStatus())) {
            refundCoin = defaultDecimal(order.getCoinUsed());
            refundPoint = defaultPoint(order.getPointUsed());
            refundAmount = refundCoin.add(defaultDecimal(order.getCashAmount()));
            refundPayment(resident, order, refundPoint, refundCoin);
            restoreStock(orderItemRepository.findByOrderId(order.getId()));
            reverseRewards(resident, order);
        }

        order.setOrderStatus("cancelled");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(request.getReason());

        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        deliveryService.failDeliveryForCancelledOrder(order.getId());
        distributionService.reverseForCancelledOrder(order.getId());

        return CancelOrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .cancelledAt(DateTimeUtil.toIsoUtc(order.getCancelledAt()))
                .refundAmount(refundAmount)
                .refundPoint(refundPoint)
                .refundCoin(refundCoin)
                .build();
    }

    @Transactional
    public ConfirmOrderResponse confirmOrder(String id) {
        roleGuard.requireAnyRole("resident");
        if (!id.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }

        Resident resident = getActiveResident(UserContext.currentUserId());
        Order order = getOrderForUpdate(id);
        assertOrderOwner(order, resident.getId());

        if (!"delivering".equals(order.getOrderStatus()) && !"paid".equals(order.getOrderStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        order.setOrderStatus("completed");
        order.setCompletedAt(LocalDateTime.now());

        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        // 确认收货时生成分成记录
        Merchant merchant = merchantRepository.findById(order.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        MerchantPropertyConfig config = loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());
        distributionService.calculateAndSave(order, resident, config);

        return ConfirmOrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .completedAt(DateTimeUtil.toIsoUtc(order.getCompletedAt()))
                .pointEarned(defaultPoint(order.getPointEarned()))
                .coinEarned(defaultDecimal(order.getCoinEarned()))
                .build();
    }

    @Transactional
    public UpdateOrderStatusResponse updateStatus(String id, UpdateOrderStatusRequest request) {
        roleGuard.requireAnyRole("merchant", "courier");
        if (!id.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }
        validateOrderStatus(request.getOrderStatus());

        Order order = getOrderForUpdate(id);
        assertCanManageOrder(order);

        if (request.getVersion() != null && !request.getVersion().equals(order.getVersion())) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        applyStatusTransition(order, request);

        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        return UpdateOrderStatusResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .courierId(order.getCourierId())
                .deliveredAt(DateTimeUtil.toIsoUtc(order.getDeliveredAt()))
                .version(order.getVersion())
                .updatedAt(DateTimeUtil.toIsoUtc(order.getUpdatedAt()))
                .build();
    }

    private void applyStatusTransition(Order order, UpdateOrderStatusRequest request) {
        String targetStatus = request.getOrderStatus();
        if (roleGuard.isCourier()) {
            if ("delivering".equals(targetStatus)) {
                if (!"paid".equals(order.getOrderStatus()) && !"delivering".equals(order.getOrderStatus())) {
                    throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
                }
                order.setOrderStatus("delivering");
                order.setCourierId(StringUtils.hasText(request.getCourierId())
                        ? request.getCourierId()
                        : UserContext.currentUserId());
                return;
            }
            if ("completed".equals(targetStatus)) {
                if (!"delivering".equals(order.getOrderStatus())) {
                    throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
                }
                order.setOrderStatus("completed");
                order.setDeliveredAt(LocalDateTime.now());
                order.setCompletedAt(LocalDateTime.now());
                return;
            }
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        if (roleGuard.isMerchant()) {
            if ("delivering".equals(targetStatus)) {
                if (!"paid".equals(order.getOrderStatus())) {
                    throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
                }
                order.setOrderStatus("delivering");
                return;
            }
            if ("completed".equals(targetStatus)) {
                if (!"paid".equals(order.getOrderStatus()) && !"delivering".equals(order.getOrderStatus())) {
                    throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
                }
                order.setOrderStatus("completed");
                if (order.getDeliveredAt() == null) {
                    order.setDeliveredAt(LocalDateTime.now());
                }
                order.setCompletedAt(LocalDateTime.now());
                return;
            }
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private PageResponse<OrderListItemResponse> buildPageResponse(Page<Order> orderPage,
                                                                  int page,
                                                                  int pageSize,
                                                                  int previewLimit) {
        Map<String, Merchant> merchantMap = loadMerchantMap(orderPage.getContent());
        Map<String, List<OrderItem>> itemMap = loadItemMap(orderPage.getContent());

        List<OrderListItemResponse> list = orderPage.getContent().stream()
                .map(order -> orderConverter.toListItem(
                        order,
                        merchantMap.get(order.getMerchantId()),
                        itemMap.getOrDefault(order.getId(), List.of()),
                        previewLimit))
                .toList();

        return PageResponse.of(list, page, pageSize, orderPage.getTotalElements());
    }

    private List<PreparedItem> prepareItems(List<CreateOrderRequest.OrderItemRequest> items,
                                            String merchantId,
                                            Resident resident) {
        List<PreparedItem> preparedItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest itemRequest : items) {
            if (!itemRequest.getProductId().startsWith("prd_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "商品ID格式不正确");
            }
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
            if (!merchantId.equals(product.getMerchantId())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "商品不属于该商家");
            }
            if (!"active".equals(product.getStatus())) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "商品已下架");
            }
            if (defaultStock(product) < itemRequest.getQuantity()) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "商品库存不足");
            }

            BigDecimal unitPrice = resolveUnitPrice(product, resident);
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            preparedItems.add(new PreparedItem(product, itemRequest.getQuantity(), unitPrice, totalPrice));
        }
        return preparedItems;
    }

    private BigDecimal resolveUnitPrice(Product product, Resident resident) {
        if (product.getMemberPrice() != null) {
            return product.getMemberPrice();
        }
        return product.getPrice();
    }

    private BigDecimal calculateDeliveryFee(Merchant merchant, BigDecimal subtotal) {
        BigDecimal fee = defaultDecimal(merchant.getDeliveryFee());
        BigDecimal threshold = merchant.getFreeDeliveryThreshold();
        if (threshold != null && subtotal.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return fee;
    }

    private void deductStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
            int stock = defaultStock(product);
            if (stock < item.getQuantity()) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "商品库存不足");
            }
            int remaining = stock - item.getQuantity();
            product.setStock(remaining);
            if (remaining <= 0) {
                product.setStatus("sold_out");
            }
            productRepository.save(product);
        }
    }

    private void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStock(defaultStock(product) + item.getQuantity());
                if ("sold_out".equals(product.getStatus())) {
                    product.setStatus("active");
                }
                productRepository.save(product);
            });
        }
    }

    private PaymentBreakdown resolvePaymentBreakdown(PayOrderRequest request,
                                                     BigDecimal payable,
                                                     MerchantPropertyConfig config) {
        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate()
                : new BigDecimal("100.0000");

        return switch (request.getPaymentMethod()) {
            case "property_coin" -> new PaymentBreakdown(0, payable, BigDecimal.ZERO);
            case "point" -> {
                int pointsNeeded = payable.multiply(exchangeRate)
                        .setScale(0, RoundingMode.UP)
                        .intValue();
                yield new PaymentBreakdown(pointsNeeded, BigDecimal.ZERO, BigDecimal.ZERO);
            }
            case "wechat" -> new PaymentBreakdown(0, BigDecimal.ZERO, payable);
            case "mixed" -> {
                int pointUsed = request.getPointUsed() != null ? request.getPointUsed() : 0;
                BigDecimal coinUsed = request.getCoinUsed() != null ? request.getCoinUsed() : BigDecimal.ZERO;
                BigDecimal pointValue = BigDecimal.valueOf(pointUsed)
                        .divide(exchangeRate, 2, RoundingMode.HALF_UP);
                BigDecimal cashAmount = payable.subtract(pointValue).subtract(coinUsed);
                if (cashAmount.compareTo(BigDecimal.ZERO) < 0) {
                    cashAmount = BigDecimal.ZERO;
                }
                BigDecimal paid = pointValue.add(coinUsed).add(cashAmount);
                if (paid.compareTo(payable) < 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "混合支付金额不足");
                }
                yield new PaymentBreakdown(pointUsed, coinUsed, cashAmount);
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        };
    }

    private void executePayment(Resident resident,
                                Order order,
                                PaymentBreakdown breakdown,
                                boolean useFamilyPool) {
        if (breakdown.pointUsed() > 0) {
            pointService.spendPoints(resident, breakdown.pointUsed(), "order", order.getId(),
                    "订单积分支付", null, useFamilyPool);
        }
        if (breakdown.coinUsed().compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(resident, breakdown.coinUsed(), "order", order.getId(),
                    "订单物业币支付", null, useFamilyPool);
        }
    }

    private void refundPayment(Resident resident, Order order, int refundPoint, BigDecimal refundCoin) {
        boolean useFamilyPool = StringUtils.hasText(order.getFamilyId());
        if (refundPoint > 0) {
            pointService.earnPoints(resident, refundPoint, "order", order.getId(),
                    "订单取消退回积分", null);
        }
        if (refundCoin.compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.earnCoins(resident, refundCoin, CoinType.earn.name(), "order",
                    order.getId(), "订单取消退回物业币", null);
        }
    }

    private void reverseRewards(Resident resident, Order order) {
        if (defaultPoint(order.getPointEarned()) > 0) {
            pointService.spendPoints(resident, order.getPointEarned(), "order", order.getId(),
                    "订单取消扣回返积分", null, StringUtils.hasText(order.getFamilyId()));
        }
        if (defaultDecimal(order.getCoinEarned()).compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(resident, order.getCoinEarned(), "order", order.getId(),
                    "订单取消扣回物业币返现", null, StringUtils.hasText(order.getFamilyId()));
        }
    }

    private int calculatePointEarned(PaymentBreakdown breakdown, MerchantPropertyConfig config) {
        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate()
                : new BigDecimal("100.0000");
        BigDecimal rewardBase = breakdown.coinUsed().add(breakdown.cashAmount());
        if (rewardBase.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return rewardBase.multiply(exchangeRate).setScale(0, RoundingMode.DOWN).intValue();
    }

    private BigDecimal calculateCoinEarned(PaymentBreakdown breakdown, MerchantPropertyConfig config) {
        if (!Boolean.TRUE.equals(config.getCoinRebateEnabled())) {
            return BigDecimal.ZERO;
        }
        if (config.getCommissionRate() == null
                || config.getCommissionRate().compareTo(COMMISSION_REBATE_THRESHOLD) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rebateRate = config.getCoinRebateRate() != null
                ? config.getCoinRebateRate()
                : BigDecimal.ZERO;
        BigDecimal rewardBase = breakdown.coinUsed().add(breakdown.cashAmount());
        if (rewardBase.compareTo(BigDecimal.ZERO) <= 0 || rebateRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return rewardBase.multiply(rebateRate).setScale(2, RoundingMode.HALF_UP);
    }

    private MerchantPropertyConfig loadMerchantConfig(String merchantId, String propertyCompanyId) {
        return merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchantId, propertyCompanyId)
                .filter(config -> "active".equals(config.getStatus()))
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_CONFIG_NOT_FOUND));
    }

    private void assertMerchantAvailable(Merchant merchant) {
        if (!"approved".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_AUDITING);
        }
        if (!"active".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
    }

    private void assertOrderOwner(Order order, String residentId) {
        if (!order.getResidentId().equals(residentId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void assertCanViewOrder(Order order) {
        if (isOrderOwner(order)) {
            return;
        }
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (roleGuard.isPropertyAdmin()) {
            roleGuard.assertSamePropertyOrPlatform(order.getPropertyCompanyId());
            return;
        }
        if (roleGuard.isMerchant() && isOwnMerchantOrder(order)) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertCanManageOrder(Order order) {
        if (roleGuard.isMerchant()) {
            if (!isOwnMerchantOrder(order)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            return;
        }
        if (roleGuard.isCourier()) {
            roleGuard.assertSamePropertyOrPlatform(order.getPropertyCompanyId());
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private boolean isOrderOwner(Order order) {
        return UserContext.currentUserId() != null
                && UserContext.currentUserId().equals(order.getResidentId());
    }

    private boolean isOwnMerchantOrder(Order order) {
        if (!roleGuard.isMerchant()) {
            return false;
        }
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId()).orElse(null);
        if (merchantUser == null || !StringUtils.hasText(merchantUser.getPhone())) {
            return false;
        }
        return merchantRepository.findById(order.getMerchantId())
                .map(merchant -> merchantUser.getPhone().equals(merchant.getContactPhone())
                        && merchantUser.getPropertyCompanyId().equals(merchant.getPropertyCompanyId()))
                .orElse(false);
    }

    private String resolveAdminPropertyCompanyId(String propertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(propertyCompanyId)) {
                if (!propertyCompanyId.startsWith("pc_")) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
                }
            }
            return propertyCompanyId;
        }
        return UserContext.currentPropertyCompanyId();
    }

    private String resolveAdminMerchantId(String merchantId) {
        if (roleGuard.isMerchant()) {
            Merchant merchant = resolveOwnedMerchant();
            if (StringUtils.hasText(merchantId) && !merchant.getId().equals(merchantId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            return merchant.getId();
        }
        return merchantId;
    }

    private Merchant resolveOwnedMerchant() {
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    private Order getOrderForUpdate(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Resident getActiveResident(String residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!ResidentStatus.active.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }

    private boolean resolveUseFamilyPool(String familyId, Order order, Resident resident) {
        if (StringUtils.hasText(familyId)) {
            if (!familyId.equals(resident.getFamilyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员无法使用家庭积分/物业币");
            }
            return true;
        }
        return StringUtils.hasText(order.getFamilyId());
    }

    private Map<String, Merchant> loadMerchantMap(List<Order> orders) {
        List<String> merchantIds = orders.stream().map(Order::getMerchantId).distinct().toList();
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity()));
    }

    private Map<String, List<OrderItem>> loadItemMap(List<Order> orders) {
        Map<String, List<OrderItem>> itemMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, order -> orderItemRepository.findByOrderId(order.getId())));
        return itemMap;
    }

    private void validateCreateRequest(CreateOrderRequest request) {
        if (!request.getMerchantId().startsWith("mch_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "merchantId格式不正确");
        }
        if (StringUtils.hasText(request.getFamilyId()) && !request.getFamilyId().startsWith("fam_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "familyId格式不正确");
        }
    }

    private void validatePaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod) || !RoleGuard.PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        }
    }

    private void validateOrderStatus(String orderStatus) {
        if (StringUtils.hasText(orderStatus) && !RoleGuard.ORDER_STATUS.contains(orderStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "orderStatus值无效");
        }
    }

    private void validateMerchantId(String merchantId) {
        if (StringUtils.hasText(merchantId) && !merchantId.startsWith("mch_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "merchantId格式不正确");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String trimmed = keyword.trim();
        if (trimmed.length() > 100) {
            throw new BizException(ErrorCode.PARAM_INVALID, "keyword长度不能超过100");
        }
        return trimmed;
    }

    private String buildDefaultAddress(Resident resident) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(resident.getBuilding())) {
            builder.append(resident.getBuilding());
        }
        if (StringUtils.hasText(resident.getUnit())) {
            builder.append(resident.getUnit());
        }
        if (StringUtils.hasText(resident.getRoom())) {
            builder.append(resident.getRoom());
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private String generateOrderNo() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long suffix = Math.abs(idGenerator.nextId() % 100000);
        return timePart + String.format("%05d", suffix);
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        return PageRequest.of(page - 1, pageSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }
        String[] parts = sort.split(",");
        Sort.Order[] orders = new Sort.Order[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            boolean desc = part.startsWith("-");
            String field = desc ? part.substring(1) : (part.startsWith("+") ? part.substring(1) : part);
            if (!SORTABLE_FIELDS.contains(field)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "sort字段不支持: " + field);
            }
            orders[i] = desc ? Sort.Order.desc(field) : Sort.Order.asc(field);
        }
        return Sort.by(orders);
    }

    private LocalDateTime parseIsoDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Instant.parse(value).atZone(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式不正确");
        }
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private int defaultPoint(Integer value) {
        return value != null ? value : 0;
    }

    private int defaultStock(Product product) {
        return product.getStock() != null ? product.getStock() : 0;
    }

    private record PreparedItem(Product product, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
    }

    private record PaymentBreakdown(int pointUsed, BigDecimal coinUsed, BigDecimal cashAmount) {
    }
}
