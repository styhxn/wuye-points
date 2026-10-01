package com.wuye.points.service;

import com.wuye.points.constant.FulfillmentConstants;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.cart.CartDeliveryFeeRequest;
import com.wuye.points.dto.cart.CartDeliveryFeeResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.order.CancelOrderRequest;
import com.wuye.points.dto.order.CancelOrderResponse;
import com.wuye.points.dto.order.ChooseFulfillmentRequest;
import com.wuye.points.dto.order.ConfirmOrderResponse;
import com.wuye.points.dto.order.CreateListingOrderRequest;
import com.wuye.points.dto.order.CreateOrderRequest;
import com.wuye.points.dto.order.CreateOrderResponse;
import com.wuye.points.dto.order.OrderDetailResponse;
import com.wuye.points.dto.order.OrderListItemResponse;
import com.wuye.points.dto.order.OverrideFulfillmentRequest;
import com.wuye.points.dto.order.PayOrderRequest;
import com.wuye.points.dto.order.PayOrderResponse;
import com.wuye.points.dto.order.PreviewListingOrderRequest;
import com.wuye.points.dto.order.PreviewOrderRequest;
import com.wuye.points.dto.order.PreviewOrderResponse;
import com.wuye.points.dto.order.RefundAuditRequest;
import com.wuye.points.dto.order.RefundRequestDTO;
import com.wuye.points.dto.order.RefundResponse;
import com.wuye.points.dto.order.SendDeliveryResponse;
import com.wuye.points.dto.order.UpdateOrderStatusRequest;
import com.wuye.points.dto.order.UpdateOrderStatusResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributorProduct;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantListing;
import com.wuye.points.enums.CoinType;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.payment.EcommerceSplitProperties;
import com.wuye.points.payment.WechatPayService;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.entity.MerchantWechatAccount;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.OrderItemRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.ResidentMerchantListingRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.OrderSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.DisplayLabelUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "totalAmount", "paidAt");
    private static final BigDecimal COMMISSION_REBATE_THRESHOLD = new BigDecimal("0.2000");
    private static final int LIST_ITEM_PREVIEW_LIMIT = 5;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;
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
    private final FulfillmentAuditService fulfillmentAuditService;
    private final DistributorProductRepository distributorProductRepository;
    private final ResidentMerchantListingRepository listingRepository;
    private final ResidentMerchantSettlementService residentMerchantSettlementService;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyCoinMallRuleService propertyCoinMallRuleService;
    private final CanteenService canteenService;
    private final PlatformSettingService platformSettingService;
    private final FamilyRepository familyRepository;
    private final WechatPayService wechatPayService;
    private final WechatSplitService wechatSplitService;
    private final PaymentRecordRepository paymentRecordRepository;
    private final EcommerceSplitProperties ecommerceSplitProperties;
    private final MerchantWechatAccountRepository merchantWechatAccountRepository;

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
        applyFulfillmentRoutingAtCreation(order, merchant, preparedItems);
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

    /**
     * 向业主商户下单（购买其从一级经销商拿货后上架的商品）。
     * 订单商家为货源方（一级经销商），订单记录 listing_id，完成时结算差价并自动下架。
     */
    @Transactional
    public CreateOrderResponse createListingOrder(CreateListingOrderRequest request) {
        roleGuard.requireAnyRole("resident");
        if (request == null || !StringUtils.hasText(request.getListingId())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "上架ID不能为空");
        }
        Resident resident = getActiveResident(UserContext.currentUserId());

        ResidentMerchantListing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "上架商品不存在"));
        if (!Boolean.TRUE.equals(listing.getEnabled())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该商品已下架");
        }
        DistributorProduct product = distributorProductRepository.findById(listing.getDistributorProductId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "货源商品不存在"));
        if (!"active".equals(product.getStatus())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "货源商品已停售");
        }
        Merchant merchant = merchantRepository.findById(product.getDistributorMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        assertMerchantAvailable(merchant);
        loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());

        int quantity = request.getQuantity() == null || request.getQuantity() < 1 ? 1 : request.getQuantity();
        BigDecimal unitPrice = defaultDecimal(listing.getRetailPrice());
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deliveryFee = calculateDeliveryFee(merchant, totalAmount);

        Order order = new Order();
        order.setId(idGenerator.generateId("ord"));
        order.setOrderNo(generateOrderNo());
        order.setResidentId(resident.getId());
        order.setMerchantId(merchant.getId());
        order.setPropertyCompanyId(resident.getPropertyCompanyId());
        order.setFamilyId(StringUtils.hasText(request.getFamilyId())
                ? request.getFamilyId() : resident.getFamilyId());
        order.setOrderStatus("pending");
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        order.setPointEarned(0);
        order.setCoinEarned(BigDecimal.ZERO);
        order.setAddress(StringUtils.hasText(request.getAddress())
                ? request.getAddress() : buildDefaultAddress(resident));
        order.setContactPhone(StringUtils.hasText(request.getContactPhone())
                ? request.getContactPhone() : resident.getPhone());
        order.setRemark(request.getRemark());
        order.setListingId(listing.getId());
        // 业主商户上架商品：走常规配送履约链路（支付后进入 pending_choice）
        order.setRequiresDelivery(Boolean.TRUE);
        order.setFulfillmentMode(null);
        orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setId(idGenerator.generateId("oi"));
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductCover(product.getCoverUrl());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setTotalPrice(totalAmount);
        orderItemRepository.save(item);

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

        BigDecimal payable = order.getTotalAmount().add(defaultDecimal(order.getDeliveryFee()));
        boolean useFamilyPool = resolveUseFamilyPool(request.getFamilyId(), order, resident);

        PaymentBreakdown breakdown = resolvePaymentBreakdown(request, payable, config);
        if (breakdown.cashAmount().compareTo(BigDecimal.ZERO) > 0) {
            // 含现金部分：微信预下单，订单保持 pending，支付成功后由回调 completeChannelPayment 落账
            return initiateChannelPayment(order, resident, merchant, items, request, payable, breakdown);
        }

        // 纯积分/物业币即时支付：直接落账
        deductStock(items);
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

        applyPostPaymentRouting(order, merchant, items, resident, config);

        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

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

    /**
     * 含现金部分的支付：创建支付流水并微信预下单，订单保持 pending，由微信回调落账。
     * <p>预支付即锁定库存（超时未支付由 {@link PendingPaymentScheduler} 取消订单并恢复库存）；
     * 本方法任一步失败抛出异常时，事务回滚，库存/流水均不落库。</p>
     */
    private PayOrderResponse initiateChannelPayment(Order order, Resident resident, Merchant merchant,
                                                    List<OrderItem> items, PayOrderRequest request,
                                                    BigDecimal payable, PaymentBreakdown breakdown) {
        if (!wechatPayService.isEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "在线支付未开通，请联系物业或使用积分/物业币支付");
        }
        // 防重复提交：订单已有待支付流水时拒绝，避免重复扣款
        if (!paymentRecordRepository.findByBizTypeAndBizIdAndStatus(
                PaymentBizType.ORDER, order.getId(), "pending").isEmpty()) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "订单支付处理中，请勿重复提交");
        }
        deductStock(items);
        if (StringUtils.hasText(request.getFamilyId())) {
            order.setFamilyId(request.getFamilyId());
        }

        String openId = resolveOrderOpenId(request.getOpenid(), resident);
        PaymentRecord record = wechatPayService.createIntent(
                PaymentBizType.ORDER, order.getId(), resident.getId(),
                order.getPropertyCompanyId(), request.getPaymentMethod(), payable,
                breakdown.pointUsed(), breakdown.coinUsed(), breakdown.cashAmount());
        // 电商收付通模式：按订单商家二级商户号路由，用户付款直进商家账户（资金不经平台）
        if (ecommerceSplitProperties.isEnabled()) {
            String subMchid = resolveMerchantSubMchid(merchant);
            WechatJsPayParams payParams = wechatPayService.miniPay(record, openId, "物业积分商城订单", subMchid);
            return buildChannelPayResponse(order, request, payable, breakdown, record, payParams);
        }
        WechatJsPayParams payParams = wechatPayService.miniPay(record, openId, "物业积分商城订单");
        return buildChannelPayResponse(order, request, payable, breakdown, record, payParams);
    }

    /**
     * 解析订单商家的微信二级商户号（电商收付通）。未进件/未激活 → 拒绝在线支付（fail-closed），
     * 仅积分/物业币可付。留空配置回退服务商号（灰度过渡）。
     */
    private String resolveMerchantSubMchid(Merchant merchant) {
        if (merchant != null) {
            MerchantWechatAccount account = merchantWechatAccountRepository
                    .findByMerchantIdAndStatus(merchant.getId(), "active").orElse(null);
            if (account != null && StringUtils.hasText(account.getSubMchid())) {
                return account.getSubMchid();
            }
            throw new BizException(ErrorCode.ORDER_PAY_UNAVAILABLE);
        }
        return StringUtils.hasText(ecommerceSplitProperties.getPlatformSubMchId())
                ? ecommerceSplitProperties.getPlatformSubMchId() : null;
    }

    private PayOrderResponse buildChannelPayResponse(Order order, PayOrderRequest request, BigDecimal payable,
                                                     PaymentBreakdown breakdown, PaymentRecord record,
                                                     WechatJsPayParams payParams) {
        return PayOrderResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus("pending")
                .paymentMethod(request.getPaymentMethod())
                .paidAmount(payable)
                .pointUsed(breakdown.pointUsed())
                .coinUsed(breakdown.coinUsed())
                .cashAmount(breakdown.cashAmount())
                .paymentRecordId(record.getId())
                .payParams(payParams)
                .build();
    }

    /** 解析微信小程序 openid：请求显式传入优先，否则取用户绑定 openid，两者皆无则报错。 */
    private String resolveOrderOpenId(String requestOpenId, Resident resident) {
        if (StringUtils.hasText(requestOpenId)) {
            return requestOpenId;
        }
        if (StringUtils.hasText(resident.getOpenid())) {
            return resident.getOpenid();
        }
        throw new BizException(ErrorCode.PARAM_MISSING, "openid不能为空（微信小程序支付需要）");
    }

    /**
     * 微信支付回调确认后订单置为已支付（幂等）。由 PaymentBizDispatcher 调用，无登录上下文。
     * 积分/物业币抵扣以支付流水快照为准，与 payOrder 保持一致的落账口径。
     */
    @Transactional
    public void completeChannelPayment(PaymentRecord record) {
        Order order = getOrderForUpdate(record.getBizId());
        if (!"pending".equals(order.getOrderStatus())) {
            return; // 幂等：已支付/已取消/已关闭
        }

        Resident resident = residentRepository.findById(order.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        Merchant merchant = merchantRepository.findById(order.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        int pointUsed = record.getPointUsed() != null ? record.getPointUsed() : 0;
        BigDecimal coinUsed = record.getCoinUsed() != null ? record.getCoinUsed() : BigDecimal.ZERO;
        if (pointUsed > 0) {
            pointService.spendPoints(resident, pointUsed, "order", order.getId(),
                    "订单消费抵扣积分", null, false);
        }
        if (coinUsed.compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(resident, coinUsed, "order", order.getId(),
                    "订单消费抵扣物业币", null, false);
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        deductStock(items);

        List<OrderItem> channelItems = orderItemRepository.findByOrderId(order.getId());
        MerchantPropertyConfig channelConfig = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), order.getPropertyCompanyId())
                .filter(c -> "active".equals(c.getStatus()))
                .orElse(null);

        order.setPaymentMethod(record.getPaymentMethod());
        order.setPointUsed(pointUsed);
        order.setCoinUsed(coinUsed);
        order.setCashAmount(record.getCashAmount());
        order.setOrderStatus("paid");
        order.setPaidAt(LocalDateTime.now());

        applyPostPaymentRouting(order, merchant, channelItems, resident, channelConfig);

        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }
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

        if ("paid".equals(order.getOrderStatus())
                || "delivering".equals(order.getOrderStatus())
                || "pending_verification".equals(order.getOrderStatus())
                || "verified".equals(order.getOrderStatus())) {
            refundCoin = defaultDecimal(order.getCoinUsed());
            refundPoint = defaultPoint(order.getPointUsed());
            refundAmount = refundCoin.add(defaultDecimal(order.getCashAmount()));
            refundPayment(resident, order, refundPoint, refundCoin);
            restoreStock(orderItemRepository.findByOrderId(order.getId()));
            reverseRewards(resident, order);
            refundCashIfAny(order, "用户取消订单");
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

    /**
     * 履约方式超时兜底：将仍处于 pending_choice 的订单默认改派为平台配送（courier_hall），
     * 并写入审计日志（留痕）。由 FulfillmentChoiceTimeoutJob 调用，非幂等由查询条件保证。
     */
    @Transactional
    public void autoResolvePendingChoice(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND));
        if (!FulfillmentConstants.FULFILLMENT_MODE_PENDING_CHOICE.equals(order.getFulfillmentMode())) {
            return;
        }
        String fallbackMode = FulfillmentConstants.FULFILLMENT_MODE_COURIER_HALL;
        Merchant merchant = merchantRepository.findById(order.getMerchantId()).orElse(null);
        DeliveryService.FulfillmentDeliveryResult result = deliveryService.ensureDelivery(
                order, merchant, FulfillmentConstants.CARRIER_TYPE_COURIER);
        order.setFulfillmentMode(fallbackMode);
        order.setFulfillmentChosenAt(LocalDateTime.now());
        order.setFulfillmentDeadline(null);
        if ("paid".equals(order.getOrderStatus())) {
            order.setOrderStatus("delivering");
        }
        orderRepository.save(order);
        fulfillmentAuditService.recordTimeout(order.getId(), order.getMerchantId(),
                fallbackMode, result.delivery().getId());
    }

    /**
     * 订单支付试算（§8.0 POST /orders/preview）：按物业币商城规则试算可抵扣金额与现金应付。
     * 仅查询，不落库，供前端支付页展示；支付仍以 POST /orders/{id}/pay 后端校验为准。
     */
    @Transactional(readOnly = true)
    public PreviewOrderResponse previewOrder(PreviewOrderRequest request) {
        roleGuard.requireAnyRole("resident");
        if (!request.getMerchantId().startsWith("mch_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "merchantId格式不正确");
        }
        if (StringUtils.hasText(request.getFamilyId()) && !request.getFamilyId().startsWith("fam_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "familyId格式不正确");
        }

        Resident resident = getActiveResident(UserContext.currentUserId());
        if (StringUtils.hasText(request.getFamilyId())
                && !request.getFamilyId().equals(resident.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员无法使用家庭物业币");
        }
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        assertMerchantAvailable(merchant);
        loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());

        List<PreparedItem> preparedItems = prepareItems(request.getItems(), merchant.getId(), resident);
        BigDecimal productAmount = preparedItems.stream()
                .map(PreparedItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 团购单特殊口径：整单商品分类均为"团购"时免配送费
        boolean groupBuy = !preparedItems.isEmpty() && preparedItems.stream()
                .allMatch(item -> "团购".equals(item.product().getCategory()));

        BigDecimal originalDeliveryFee = groupBuy ? BigDecimal.ZERO : defaultDecimal(merchant.getDeliveryFee());
        BigDecimal deliveryFee = groupBuy ? BigDecimal.ZERO : calculateDeliveryFee(merchant, productAmount);
        BigDecimal deliveryWaiverAmount = originalDeliveryFee.subtract(deliveryFee).max(BigDecimal.ZERO);
        boolean freeDeliveryEligible = !groupBuy
                && deliveryFee.compareTo(BigDecimal.ZERO) == 0
                && merchant.getFreeDeliveryThreshold() != null
                && productAmount.compareTo(merchant.getFreeDeliveryThreshold()) >= 0;

        BigDecimal totalAmount = productAmount.add(deliveryFee);

        // 可抵扣物业币余额（家庭池优先，与支付链路口径一致）
        BigDecimal availableCoin = defaultDecimal(resident.getCoinBalance());
        if (StringUtils.hasText(resident.getFamilyId())) {
            availableCoin = availableCoin.add(resolveFamilyBalance(resident.getFamilyId()));
        }

        PropertyCompany company = StringUtils.hasText(resident.getPropertyCompanyId())
                ? propertyCompanyRepository.findById(resident.getPropertyCompanyId()).orElse(null)
                : null;
        PropertyCoinMallRuleService.CoinAllowance allowance = company != null
                ? propertyCoinMallRuleService.previewCoinAllowance(
                        company, totalAmount, request.getCoinUsed(), availableCoin)
                : new PropertyCoinMallRuleService.CoinAllowance(
                        totalAmount, BigDecimal.ZERO, BigDecimal.ZERO, totalAmount, null);

        BigDecimal coinUsed = allowance.coinUsed();
        int pointUsed = request.getPointUsed() != null ? request.getPointUsed() : 0;
        MerchantPropertyConfig config = loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());
        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate()
                : new BigDecimal("100.0000");
        BigDecimal pointValue = BigDecimal.valueOf(pointUsed)
                .divide(exchangeRate, 2, RoundingMode.HALF_UP);
        BigDecimal cashAmount = totalAmount.subtract(coinUsed).subtract(pointValue).max(BigDecimal.ZERO);

        return PreviewOrderResponse.builder()
                .totalAmount(totalAmount)
                .subtotal(productAmount)
                .productAmount(productAmount)
                .deliveryFee(deliveryFee)
                .originalDeliveryFee(originalDeliveryFee)
                .deliveryWaiverAmount(deliveryWaiverAmount)
                .deliverySubsidySponsor(groupBuy
                        ? null
                        : (deliveryWaiverAmount.compareTo(BigDecimal.ZERO) > 0 ? "merchant" : null))
                .deliverySubsidyAmount(deliveryWaiverAmount)
                .deliverySettlementBase(deliveryFee.add(deliveryWaiverAmount))
                .freeDeliveryEligible(freeDeliveryEligible)
                .waiverReason(groupBuy ? null
                        : (deliveryWaiverAmount.compareTo(BigDecimal.ZERO) > 0 ? "threshold" : "none"))
                .pointUsed(pointUsed)
                .maxCoinAllowed(allowance.maxCoinAllowed())
                .coinUsed(coinUsed)
                .cashAmount(cashAmount)
                .reasonIfCapped(allowance.reasonIfCapped())
                .directedBalances(canteenService.listUsableBalances(merchant.getId(), resident.getId()))
                .build();
    }

    /**
     * 业主上架商品支付试算（POST /orders/from-listing/preview）。
     * 上架商品无商城 productId（货源商品 dpr_ 不在 products 表），故不走普通 previewOrder；
     * 试算口径与 createListingOrder 保持一致：商品金额=零售价×数量，运费按一级经销商规则，
     * 物业币/积分抵扣与 family 池口径同商城试算。仅查询，不落库。
     */
    @Transactional(readOnly = true)
    public PreviewOrderResponse previewListingOrder(PreviewListingOrderRequest request) {
        roleGuard.requireAnyRole("resident");
        if (request == null || !StringUtils.hasText(request.getListingId())
                || !request.getListingId().startsWith("rml_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "上架ID格式不正确");
        }
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (StringUtils.hasText(request.getFamilyId())
                && !request.getFamilyId().equals(resident.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员无法使用家庭物业币");
        }

        ResidentMerchantListing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "上架商品不存在"));
        if (!Boolean.TRUE.equals(listing.getEnabled())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该商品已下架");
        }
        DistributorProduct product = distributorProductRepository.findById(listing.getDistributorProductId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "货源商品不存在"));
        if (!"active".equals(product.getStatus())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "货源商品已停售");
        }
        Merchant merchant = merchantRepository.findById(product.getDistributorMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        assertMerchantAvailable(merchant);
        loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());

        int quantity = request.getQuantity() == null || request.getQuantity() < 1 ? 1 : request.getQuantity();
        BigDecimal retailPrice = defaultDecimal(listing.getRetailPrice());
        BigDecimal productAmount = retailPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal originalDeliveryFee = defaultDecimal(merchant.getDeliveryFee());
        BigDecimal deliveryFee = calculateDeliveryFee(merchant, productAmount);
        BigDecimal deliveryWaiverAmount = originalDeliveryFee.subtract(deliveryFee).max(BigDecimal.ZERO);
        boolean freeDeliveryEligible = deliveryFee.compareTo(BigDecimal.ZERO) == 0
                && merchant.getFreeDeliveryThreshold() != null
                && productAmount.compareTo(merchant.getFreeDeliveryThreshold()) >= 0;

        BigDecimal totalAmount = productAmount.add(deliveryFee);

        // 可抵扣物业币余额（家庭池优先，与支付链路口径一致）
        BigDecimal availableCoin = defaultDecimal(resident.getCoinBalance());
        if (StringUtils.hasText(resident.getFamilyId())) {
            availableCoin = availableCoin.add(resolveFamilyBalance(resident.getFamilyId()));
        }

        PropertyCompany company = StringUtils.hasText(resident.getPropertyCompanyId())
                ? propertyCompanyRepository.findById(resident.getPropertyCompanyId()).orElse(null)
                : null;
        PropertyCoinMallRuleService.CoinAllowance allowance = company != null
                ? propertyCoinMallRuleService.previewCoinAllowance(
                        company, totalAmount, request.getCoinUsed(), availableCoin)
                : new PropertyCoinMallRuleService.CoinAllowance(
                        totalAmount, BigDecimal.ZERO, BigDecimal.ZERO, totalAmount, null);

        BigDecimal coinUsed = allowance.coinUsed();
        int pointUsed = request.getPointUsed() != null ? request.getPointUsed() : 0;
        MerchantPropertyConfig config = loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());
        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate()
                : new BigDecimal("100.0000");
        BigDecimal pointValue = BigDecimal.valueOf(pointUsed)
                .divide(exchangeRate, 2, RoundingMode.HALF_UP);
        BigDecimal cashAmount = totalAmount.subtract(coinUsed).subtract(pointValue).max(BigDecimal.ZERO);

        return PreviewOrderResponse.builder()
                .totalAmount(totalAmount)
                .subtotal(productAmount)
                .productAmount(productAmount)
                .deliveryFee(deliveryFee)
                .originalDeliveryFee(originalDeliveryFee)
                .deliveryWaiverAmount(deliveryWaiverAmount)
                .deliverySubsidySponsor(deliveryWaiverAmount.compareTo(BigDecimal.ZERO) > 0 ? "merchant" : null)
                .deliverySubsidyAmount(deliveryWaiverAmount)
                .deliverySettlementBase(deliveryFee.add(deliveryWaiverAmount))
                .freeDeliveryEligible(freeDeliveryEligible)
                .waiverReason(deliveryWaiverAmount.compareTo(BigDecimal.ZERO) > 0 ? "threshold" : "none")
                .pointUsed(pointUsed)
                .maxCoinAllowed(allowance.maxCoinAllowed())
                .coinUsed(coinUsed)
                .cashAmount(cashAmount)
                .reasonIfCapped(allowance.reasonIfCapped())
                .directedBalances(canteenService.listUsableBalances(merchant.getId(), resident.getId()))
                .build();
    }

    /**
     * 购物车运费预估：按商家配送费与满额免配送规则计算整单运费、减免与补贴分摊。
     * 仅查询，不落库，供前端下单前展示配送费明细。
     */
    @Transactional(readOnly = true)
    public CartDeliveryFeeResponse previewCartDeliveryFee(CartDeliveryFeeRequest request) {
        roleGuard.requireAnyRole("resident");
        if (!request.getMerchantId().startsWith("mch_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商家ID格式不正确");
        }
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        assertMerchantAvailable(merchant);

        List<String> productIds = request.getItems().stream()
                .map(CartDeliveryFeeRequest.Item::getProductId)
                .distinct()
                .toList();
        Map<String, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        BigDecimal productAmount = BigDecimal.ZERO;
        for (CartDeliveryFeeRequest.Item item : request.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (!merchant.getId().equals(product.getMerchantId())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "商品不属于该商家");
            }
            productAmount = productAmount.add(defaultDecimal(product.getPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal originalDeliveryFee = defaultDecimal(merchant.getDeliveryFee());
        BigDecimal threshold = merchant.getFreeDeliveryThreshold();
        boolean eligible = threshold != null && productAmount.compareTo(threshold) >= 0;
        BigDecimal deliveryFee = calculateDeliveryFee(merchant, productAmount);
        BigDecimal waiverAmount = originalDeliveryFee.subtract(deliveryFee).max(BigDecimal.ZERO);
        BigDecimal shortfall = eligible || threshold == null
                ? BigDecimal.ZERO
                : threshold.subtract(productAmount).max(BigDecimal.ZERO);

        return CartDeliveryFeeResponse.builder()
                .productAmount(productAmount)
                .deliveryFee(deliveryFee)
                .freeDeliveryThreshold(threshold)
                .freeDeliveryEligible(eligible)
                .freeDeliveryShortfall(shortfall)
                .originalDeliveryFee(originalDeliveryFee)
                .deliveryWaiverAmount(waiverAmount)
                .deliverySubsidySponsor("merchant")
                .deliverySubsidyAmount(waiverAmount)
                .deliverySettlementBase(deliveryFee.add(waiverAmount))
                .waiverReason(waiverAmount.compareTo(BigDecimal.ZERO) > 0 ? "threshold" : "none")
                .calculationVersion("distribution_b_v1")
                .build();
    }

    /**
     * 退款审核：物业/平台管理员对处于 refunding 的订单执行通过（退回积分/物业币并恢复库存、扣回返现）
     * 或驳回（置 refund_rejected 并留痕）。幂等由订单状态机保证。
     */
    @Transactional
    public RefundResponse auditRefund(String orderId, RefundAuditRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (!orderId.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }
        String action = request.getAction();
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "action值无效，应为 approve/reject");
        }

        Order order = getOrderForUpdate(orderId);
        roleGuard.assertSamePropertyOrPlatform(order.getPropertyCompanyId());
        if (!"refunding".equals(order.getOrderStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "订单不在退款审核中状态");
        }

        if ("reject".equals(action)) {
            if (!StringUtils.hasText(request.getRejectReason())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "驳回时必须填写原因");
            }
            order.setOrderStatus("refund_rejected");
            order.setRemark(request.getRejectReason());
            try {
                orderRepository.save(order);
            } catch (OptimisticLockingFailureException ex) {
                throw new BizException(ErrorCode.ORDER_CONFLICT);
            }
            return RefundResponse.builder()
                    .orderId(order.getId())
                    .orderStatus(order.getOrderStatus())
                    .requestedAt(DateTimeUtil.toIsoUtc(LocalDateTime.now()))
                    .message("退款申请已驳回")
                    .build();
        }

        Resident resident = residentRepository.findById(order.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        int refundPoint = defaultPoint(order.getPointUsed());
        BigDecimal refundCoin = defaultDecimal(order.getCoinUsed());
        refundPayment(resident, order, refundPoint, refundCoin);
        restoreStock(orderItemRepository.findByOrderId(order.getId()));
        reverseRewards(resident, order);
        refundCashIfAny(order, "退货审核通过");

        order.setOrderStatus("refunded");
        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        return RefundResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .requestedAt(DateTimeUtil.toIsoUtc(LocalDateTime.now()))
                .message("退款审核通过，已退回积分/物业币并恢复库存")
                .build();
    }

    /**
     * 住户申请退货：订单置为 {@code refunding}，等待物业/平台管理员审核。
     *
     * <p>此前仅存在管理端审核（refunding→refunded）而无住户侧申请入口，
     * 导致订单永远无法进入 refunding，审核流空转。本方法补齐该断链。</p>
     */
    @Transactional
    public RefundResponse requestRefund(String orderId, RefundRequestDTO request) {
        roleGuard.requireAnyRole("resident");
        if (!orderId.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }
        Resident resident = getActiveResident(UserContext.currentUserId());
        Order order = getOrderForUpdate(orderId);
        assertOrderOwner(order, resident.getId());

        String status = order.getOrderStatus();
        if (!"paid".equals(status) && !"delivering".equals(status) && !"completed".equals(status)
                && !"pending_verification".equals(status) && !"verified".equals(status)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "当前订单状态不可申请退货");
        }

        String reason = request.getReason();
        String remark = StringUtils.hasText(request.getRemark())
                ? reason + "（" + request.getRemark() + "）"
                : reason;
        order.setOrderStatus("refunding");
        order.setRemark(remark);
        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        return RefundResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .requestedAt(DateTimeUtil.toIsoUtc(LocalDateTime.now()))
                .message("退货申请已提交，等待物业审核")
                .build();
    }

    /**
     * 商家选择履约方式：{@code merchant_self} 商家自配 / {@code courier_hall} 平台配送（发布到抢单大厅）。
     *
     * <p>仅「已支付且 {@code fulfillment_mode = pending_choice}」的订单可选；
     * 重复提交相同方式视为幂等成功；已选其它方式后再次提交返回 {@code 70025}。
     * 选择结果写入 {@code fulfillment_choice_logs} 留痕，并同步创建/改写配送单。</p>
     */
    @Transactional
    public SendDeliveryResponse chooseFulfillment(String orderId, ChooseFulfillmentRequest request) {
        roleGuard.requireAnyRole("merchant");
        if (!orderId.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }
        String mode = request.getMode();
        assertFulfillmentModeValid(mode);

        Order order = getOrderForUpdate(orderId);
        if (!isOwnMerchantOrder(order)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其它商家的订单");
        }

        String currentMode = normalizeCurrentFulfillmentMode(order);
        if (FulfillmentConstants.FULFILLMENT_MODE_NONE.equals(currentMode)) {
            throw new BizException(ErrorCode.FULFILLMENT_NOT_REQUIRED, "本单无需配送（团购/社区食堂单），不可选择履约方式");
        }
        if (!FulfillmentConstants.FULFILLMENT_MODE_PENDING_CHOICE.equals(currentMode)) {
            if (mode.equals(currentMode)) {
                // 幂等：重复提交同一履约方式直接返回当前配送单
                return buildFulfillmentResponse(order, mode, false);
            }
            throw new BizException(ErrorCode.FULFILLMENT_LOCKED, "履约方式已锁定，不可变更");
        }
        if (order.getFulfillmentDeadline() != null
                && order.getFulfillmentDeadline().isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.FULFILLMENT_DEADLINE_PASSED);
        }

        return applyFulfillmentChoice(order, mode, FulfillmentConstants.AuditAction.CHOOSE, null);
    }

    /**
     * 商家将已支付订单发布到配送抢单大厅（等价于选择 {@code courier_hall}）。
     *
     * <p>§8.6.2：支付成功时后端已自动创建配送单，本接口幂等返回（{@code alreadyExists=true}）。</p>
     */
    @Transactional
    public SendDeliveryResponse sendDelivery(String orderId) {
        roleGuard.requireAnyRole("merchant");
        if (!orderId.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }

        Order order = getOrderForUpdate(orderId);
        if (!isOwnMerchantOrder(order)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其它商家的订单");
        }
        String currentMode = normalizeCurrentFulfillmentMode(order);
        if (FulfillmentConstants.FULFILLMENT_MODE_NONE.equals(currentMode)) {
            throw new BizException(ErrorCode.FULFILLMENT_NOT_REQUIRED, "本单无需配送（团购/社区食堂单），不可发布配送");
        }
        if (!"paid".equals(order.getOrderStatus())
                && !"delivering".equals(order.getOrderStatus())
                && !FulfillmentConstants.FULFILLMENT_MODE_PENDING_CHOICE.equals(currentMode)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "订单状态不允许发布配送");
        }

        String action = FulfillmentConstants.FULFILLMENT_MODE_PENDING_CHOICE.equals(currentMode)
                ? FulfillmentConstants.AuditAction.CHOOSE
                : FulfillmentConstants.AuditAction.AUTO_CHOOSE;
        return applyFulfillmentChoice(order, FulfillmentConstants.FULFILLMENT_MODE_COURIER_HALL, action, null);
    }

    /**
     * 管理员强制改派履约方式（留痕），用于商家超时未选或异常场景的人工兜底。
     * 已结束（completed/cancelled/refunded）订单不可改派；改派后同步重建配送单。
     */
    @Transactional
    public SendDeliveryResponse overrideFulfillment(String orderId, OverrideFulfillmentRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (!orderId.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }
        String mode = request.getMode();
        assertFulfillmentModeValid(mode);

        Order order = getOrderForUpdate(orderId);
        roleGuard.assertSamePropertyOrPlatform(order.getPropertyCompanyId());
        if (Set.of("completed", "cancelled", "refunded").contains(order.getOrderStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "订单已结束，不可改派履约方式");
        }
        String currentMode = normalizeCurrentFulfillmentMode(order);
        if (FulfillmentConstants.FULFILLMENT_MODE_NONE.equals(currentMode)) {
            throw new BizException(ErrorCode.FULFILLMENT_NOT_REQUIRED, "本单无需配送，不可改派履约方式");
        }
        if (mode.equals(currentMode)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "目标履约方式与当前一致，无需改派");
        }

        return applyFulfillmentChoice(order, mode, FulfillmentConstants.AuditAction.OVERRIDE, request.getRemark());
    }

    /**
     * 落地履约方式：更新订单 + 同步配送单 + 审计留痕。
     *
     * @param mode   merchant_self / courier_hall
     * @param action 审计动作（choose / auto_choose / override）
     */
    private SendDeliveryResponse applyFulfillmentChoice(Order order, String mode, String action, String remark) {
        String beforeMode = order.getFulfillmentMode();
        boolean merchantSelf = FulfillmentConstants.FULFILLMENT_MODE_MERCHANT_SELF.equals(mode);
        Merchant merchant = merchantRepository.findById(order.getMerchantId()).orElse(null);

        DeliveryService.FulfillmentDeliveryResult result = deliveryService.ensureDelivery(
                order, merchant, merchantSelf
                        ? FulfillmentConstants.CARRIER_TYPE_MERCHANT
                        : FulfillmentConstants.CARRIER_TYPE_COURIER);

        order.setFulfillmentMode(mode);
        order.setFulfillmentChosenAt(LocalDateTime.now());
        order.setFulfillmentDeadline(null);
        if ("paid".equals(order.getOrderStatus())) {
            order.setOrderStatus("delivering");
        }

        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        if (FulfillmentConstants.AuditAction.OVERRIDE.equals(action)) {
            fulfillmentAuditService.recordOverride(order.getId(), order.getMerchantId(),
                    UserContext.currentUserId(), beforeMode, mode, result.delivery().getId(), remark);
        } else {
            fulfillmentAuditService.record(order.getId(), order.getMerchantId(), "merchant",
                    order.getMerchantId(), action, beforeMode, mode, remark, result.delivery().getId());
        }

        return buildFulfillmentResponse(order, mode, result.created(), result.delivery().getId());
    }

    private SendDeliveryResponse buildFulfillmentResponse(Order order, String mode, boolean created) {
        return buildFulfillmentResponse(order, mode, created,
                deliveryRepository.findByOrderId(order.getId()).map(Delivery::getId).orElse(null));
    }

    private SendDeliveryResponse buildFulfillmentResponse(Order order, String mode, boolean created, String deliveryId) {
        String label = DisplayLabelUtil.fulfillmentModeLabel(mode);
        return SendDeliveryResponse.builder()
                .orderId(order.getId())
                .deliveryId(deliveryId)
                .statusCode(mode)
                .status(label)
                .statusLabel(label)
                .alreadyExists(!created)
                .sentAt(DateTimeUtil.toIsoUtc(LocalDateTime.now()))
                .build();
    }

    /**
     * 历史订单兼容：{@code fulfillment_mode} 为空但需配送且已支付的订单，按 {@code pending_choice} 处理，
     * 避免历史数据导致商家无法选择履约方式（409）。
     */
    private String normalizeCurrentFulfillmentMode(Order order) {
        String mode = order.getFulfillmentMode();
        if (StringUtils.hasText(mode)) {
            return mode;
        }
        if (Boolean.FALSE.equals(order.getRequiresDelivery())) {
            return FulfillmentConstants.FULFILLMENT_MODE_NONE;
        }
        return FulfillmentConstants.FULFILLMENT_MODE_PENDING_CHOICE;
    }

    private void assertFulfillmentModeValid(String mode) {
        if (!FulfillmentConstants.FULFILLMENT_MODE_MERCHANT_SELF.equals(mode)
                && !FulfillmentConstants.FULFILLMENT_MODE_COURIER_HALL.equals(mode)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "履约方式无效，应为 merchant_self 或 courier_hall");
        }
    }

    /** 下单时落库：团购单 / 社区食堂单无需配送（{@code requires_delivery=false, fulfillment_mode=none}） */
    private void applyFulfillmentRoutingAtCreation(Order order, Merchant merchant, List<PreparedItem> preparedItems) {
        boolean groupBuy = !preparedItems.isEmpty() && preparedItems.stream()
                .allMatch(item -> "团购".equals(item.product().getCategory()));
        boolean canteen = isCanteenMerchant(merchant);
        if (groupBuy || canteen) {
            order.setRequiresDelivery(Boolean.FALSE);
            order.setFulfillmentMode(FulfillmentConstants.FULFILLMENT_MODE_NONE);
        } else {
            order.setRequiresDelivery(Boolean.TRUE);
            order.setFulfillmentMode(null);
        }
    }

    /**
     * 支付成功后的分流（§68.1）：
     * <ul>
     *   <li>社区食堂单 → {@code completed}（购买即结束，不配送、不核实、不生成配送单）；</li>
     *   <li>团购单 → {@code pending_verification}（商家核实，见 §8.6.1a）；</li>
     *   <li>其它 → {@code paid} + {@code pending_choice} + 30 分钟选择时限 + 生成待抢配送单。</li>
     * </ul>
     */
    private void applyPostPaymentRouting(Order order,
                                         Merchant merchant,
                                         List<OrderItem> items,
                                         Resident resident,
                                         MerchantPropertyConfig config) {
        boolean canteen = isCanteenMerchant(merchant);
        boolean groupBuy = isGroupBuyOrder(items);

        if (canteen || groupBuy) {
            order.setRequiresDelivery(Boolean.FALSE);
            order.setFulfillmentMode(FulfillmentConstants.FULFILLMENT_MODE_NONE);
            order.setFulfillmentDeadline(null);
            if (canteen) {
                LocalDateTime now = LocalDateTime.now();
                order.setOrderStatus("completed");
                order.setCompletedAt(now);
                order.setDeliveredAt(now);
                if (config != null && resident != null) {
                    distributionService.calculateAndSave(order, resident, config);
                }
                residentMerchantSettlementService.settleOnOrderCompleted(order);
            } else {
                int hours = platformSettingService.getMerchantVerificationHours();
                order.setOrderStatus("pending_verification");
                order.setVerificationDeadline(LocalDateTime.now().plusHours(hours <= 0 ? 24 : hours));
            }
            return;
        }

        order.setRequiresDelivery(Boolean.TRUE);
        order.setFulfillmentMode(FulfillmentConstants.FULFILLMENT_MODE_PENDING_CHOICE);
        order.setFulfillmentDeadline(LocalDateTime.now()
                .plusMinutes(FulfillmentConstants.FULFILLMENT_CHOICE_TIMEOUT_MINUTES));
        // 保持既有「支付后自动发布到待抢大厅」行为：生成 courier 承运的 pending 配送单
        deliveryService.createDeliveryForPaidOrder(order, merchant);
    }

    private boolean isCanteenMerchant(Merchant merchant) {
        return merchant != null && "canteen".equals(merchant.getMerchantType());
    }

    private boolean isGroupBuyOrder(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        List<String> productIds = items.stream().map(OrderItem::getProductId).distinct().toList();
        Map<String, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        return items.stream().allMatch(item -> {
            Product product = products.get(item.getProductId());
            return product != null && "团购".equals(product.getCategory());
        });
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
        // 业主商户卖货：生成卖货明细并自动下架
        residentMerchantSettlementService.settleOnOrderCompleted(order);

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

        // 商家/快递员置为已完成时，若来自业主商户上架则结算并自动下架
        if ("completed".equals(order.getOrderStatus())) {
            residentMerchantSettlementService.settleOnOrderCompleted(order);
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

    @Transactional(readOnly = true)
    public PageResponse<OrderListItemResponse> listMyMerchantSales(
            int page, int pageSize, String orderStatus) {
        roleGuard.requireAnyRole("merchant");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(orderStatus) && !RoleGuard.ORDER_STATUS.contains(orderStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "orderStatus值无效");
        }
        String residentId = UserContext.currentUserId();
        List<Merchant> merchants = merchantRepository.findByResidentId(residentId);
        Merchant merchant = merchants.stream()
                .filter(m -> "active".equals(m.getStatus()))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1, Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = StringUtils.hasText(orderStatus)
                ? orderRepository.findByMerchantIdAndOrderStatus(merchant.getId(), orderStatus, pageable)
                : orderRepository.findByMerchantId(merchant.getId(), pageable);

        Map<String, Merchant> merchantMap = loadMerchantMap(orderPage.getContent());
        Map<String, List<OrderItem>> itemMap = loadItemMap(orderPage.getContent());
        List<OrderListItemResponse> list = orderPage.getContent().stream()
                .map(order -> orderConverter.toListItem(
                        order,
                        merchantMap.get(order.getMerchantId()),
                        itemMap.getOrDefault(order.getId(), List.of()),
                        3))
                .toList();
        return PageResponse.of(list, page, pageSize, orderPage.getTotalElements());
    }

    private List<PreparedItem> prepareItems(List<CreateOrderRequest.OrderItemRequest> items,
                                            String merchantId,
                                            Resident resident) {
        List<PreparedItem> preparedItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest itemRequest : items) {
            if (!itemRequest.getProductId().startsWith("prd_")) {
                if (itemRequest.getProductId().startsWith("rml_")) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "业主上架商品请调用 /orders/from-listing 下单");
                }
                if (itemRequest.getProductId().startsWith("dpr_")) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "货源商品请经由业主上架商品下单，请勿直接调用本接口");
                }
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
            // 业主上架货源商品（dpr_）不占用商城库存：拿货名额在上架/取消上架时已处理，
            // 下单后仅卖一次并自动下架，故跳过商城库存扣减。
            if (isListingSourcedItem(item)) {
                continue;
            }
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
            // 业主上架货源商品（dpr_）无商城库存结转，退货不恢复商城库存。
            if (isListingSourcedItem(item)) {
                continue;
            }
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStock(defaultStock(product) + item.getQuantity());
                if ("sold_out".equals(product.getStatus())) {
                    product.setStatus("active");
                }
                productRepository.save(product);
            });
        }
    }

    /** 是否业主上架货源商品（OrderItem 记录的一级经销商商品 id，前缀 dpr_）。 */
    private boolean isListingSourcedItem(OrderItem item) {
        return item != null && item.getProductId() != null && item.getProductId().startsWith("dpr_");
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

    /**
     * 已支付订单取消 / 退货审核通过时，对现金部分发起微信原路退款（fail-closed）。
     *
     * <p>流程：① 先退回该订单已冻结（或已完结）的分账，资金回笼后退款才有余额（幂等）；
     * ② 调微信退款 API（直连 / 电商收付通按流水自动路由）；③ 流水置 refunding，
     * 由 {@code RefundQueryScheduler} 轮询落 refunded / refund_failed。</p>
     *
     * <p>纯积分 / 物业币支付（cashAmount=0）或无 paid 流水直接返回。退款申请失败抛错回滚
     * 整个取消 / 审核事务（订单保持原状，用户可重试），绝不静默吞掉现金。</p>
     */
    private void refundCashIfAny(Order order, String reason) {
        if (defaultDecimal(order.getCashAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        PaymentRecord record = paymentRecordRepository
                .findByBizTypeAndBizIdAndStatus(PaymentBizType.ORDER, order.getId(), "paid").stream()
                .findFirst().orElse(null);
        if (record == null) {
            log.warn("订单需退现金但无 paid 支付流水，跳过微信退款 orderId={}", order.getId());
            return;
        }
        BigDecimal cash = defaultDecimal(record.getCashAmount());
        if (cash.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        try {
            wechatSplitService.reverseForOrder(order.getId());
        } catch (Exception e) {
            // 分账回退失败不阻断退款申请；回退资金未到账时微信退款会报余额不足，届时整个事务回滚由用户重试
            log.warn("订单退款前分账退回异常，继续发起退款 orderId={}", order.getId(), e);
        }
        String outRefundNo = wechatPayService.refund(record, cash, reason);
        record.setOutRefundNo(outRefundNo);
        record.setStatus("refunding");
        paymentRecordRepository.save(record);
        log.info("订单现金退款已发起 orderId={} cash={} outRefundNo={}", order.getId(), cash, outRefundNo);
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

    private BigDecimal resolveFamilyBalance(String familyId) {
        if (!StringUtils.hasText(familyId)) {
            return BigDecimal.ZERO;
        }
        return familyRepository.findById(familyId)
                .map(Family::getCoinBalance)
                .map(value -> value != null ? value : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
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
