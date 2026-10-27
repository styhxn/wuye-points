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
import com.wuye.points.entity.ResidentMerchantApplication;
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
import com.wuye.points.payment.WechatSplitProperties;
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
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.PropertyManagerRepository;
import com.wuye.points.repository.ResidentMerchantApplicationRepository;
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
    private final ProductStockService productStockService;
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
    private final OrderCompletionService orderCompletionService;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyCoinMallRuleService propertyCoinMallRuleService;
    private final CanteenService canteenService;
    private final PlatformSettingService platformSettingService;
    private final FamilyRepository familyRepository;
    private final WechatPayService wechatPayService;
    private final WechatSplitService wechatSplitService;
    private final WechatSplitProperties wechatSplitProperties;
    private final PaymentRecordRepository paymentRecordRepository;
    private final EcommerceSplitProperties ecommerceSplitProperties;
    private final MerchantWechatAccountRepository merchantWechatAccountRepository;
    private final CouponService couponService;
    private final BuildingLeaderRepository buildingLeaderRepository;
    private final PropertyManagerRepository propertyManagerRepository;
    private final ResidentMerchantApplicationRepository residentMerchantApplicationRepository;
    private final ListingStockService listingStockService;

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
        // 配送满额减免快照（规范§7.6）：减免额与承担方在下单时固定，结算不得按最新配置重算
        BigDecimal originalDeliveryFee = defaultDecimal(merchant.getDeliveryFee());
        BigDecimal deliveryWaiverAmount = originalDeliveryFee.subtract(deliveryFee).max(BigDecimal.ZERO);

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
        order.setOriginalDeliveryFee(originalDeliveryFee);
        order.setDeliverySubsidySponsor(deliveryWaiverAmount.compareTo(BigDecimal.ZERO) > 0 ? "merchant" : "none");
        order.setDeliverySubsidyAmount(deliveryWaiverAmount);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        order.setPointEarned(0);
        order.setCoinEarned(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setAddress(StringUtils.hasText(request.getAddress()) ? request.getAddress() : buildDefaultAddress(resident));
        order.setContactPhone(StringUtils.hasText(request.getContactPhone()) ? request.getContactPhone() : resident.getPhone());
        order.setRemark(request.getRemark());
        applyBuildingLeaderBinding(order, resident);
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
     * 订单归属业主小店店铺主体（merchant_source=resident_shop，经 listing→application→merchantId 解析），
     * 订单记录 listing_id，商品金额=零售价×件数，完成时按件数结算差价与货款。
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
        int quantity = request.getQuantity() == null || request.getQuantity() < 1 ? 1 : request.getQuantity();
        int remaining = listing.getQuantityRemaining() == null ? 0 : listing.getQuantityRemaining();
        if (remaining < quantity) {
            throw new BizException(ErrorCode.LISTING_STOCK_INSUFFICIENT);
        }
        DistributorProduct product = distributorProductRepository.findById(listing.getDistributorProductId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "货源商品不存在"));
        if (!"active".equals(product.getStatus())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "货源商品已停售");
        }
        // v8.21：订单/配送/结算主体为一级代理商；业主小店仅保留佣金归属
        Merchant shopMerchant = resolveShopMerchant(listing);
        assertMerchantAvailable(shopMerchant);
        Merchant merchant = resolveDistributorMerchant(product, resident);
        assertMerchantAvailable(merchant);
        loadListingMerchantConfig(merchant, resident);

        BigDecimal unitPrice = defaultDecimal(listing.getRetailPrice());
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deliveryFee = calculateDeliveryFee(merchant, totalAmount);
        // 配送满额减免快照（规范§7.6）
        BigDecimal originalDeliveryFee = defaultDecimal(merchant.getDeliveryFee());
        BigDecimal deliveryWaiverAmount = originalDeliveryFee.subtract(deliveryFee).max(BigDecimal.ZERO);

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
        order.setOriginalDeliveryFee(originalDeliveryFee);
        order.setDeliverySubsidySponsor(deliveryWaiverAmount.compareTo(BigDecimal.ZERO) > 0 ? "merchant" : "none");
        order.setDeliverySubsidyAmount(deliveryWaiverAmount);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        order.setPointEarned(0);
        order.setCoinEarned(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setAddress(StringUtils.hasText(request.getAddress())
                ? request.getAddress() : buildDefaultAddress(resident));
        order.setContactPhone(StringUtils.hasText(request.getContactPhone())
                ? request.getContactPhone() : resident.getPhone());
        order.setRemark(request.getRemark());
        order.setListingId(listing.getId());
        // v8.21：业主小店代销一级代理货源——订单与结算面向一级代理商，业主小店仅保留佣金归属
        order.setSourceType("distributor");
        order.setDistributorId(product.getDistributorMerchantId());
        order.setResidentMerchantId(shopMerchant.getId());
        order.setSettlementOwner("distributor");
        // 业主商户上架商品：走常规配送履约链路（支付后进入 pending_choice）
        order.setRequiresDelivery(Boolean.TRUE);
        order.setFulfillmentMode(null);
        applyBuildingLeaderBinding(order, resident);
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

    /**
     * 解析业主小店店铺主体：listing → 店主 residentId → 业主商户申请 → merchantId。
     * 店铺主体缺失（存量数据未回填）时拒绝下单，避免订单错挂到一级代理名下。
     */
    private Merchant resolveShopMerchant(ResidentMerchantListing listing) {
        ResidentMerchantApplication application = residentMerchantApplicationRepository
                .findByResidentId(listing.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.SHOP_MERCHANT_NOT_FOUND, "业主小店申请记录不存在"));
        if (!StringUtils.hasText(application.getMerchantId())) {
            throw new BizException(ErrorCode.SHOP_MERCHANT_NOT_FOUND);
        }
        return merchantRepository.findById(application.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.SHOP_MERCHANT_NOT_FOUND));
    }

    /**
     * 代销订单的商家配置：优先住户所属物业公司；跨物业公司的 platform 代理商回退到代理商本物业配置。
     */
    private MerchantPropertyConfig loadListingMerchantConfig(Merchant distributor, Resident resident) {
        return merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(distributor.getId(), resident.getPropertyCompanyId())
                .filter(config -> "active".equals(config.getStatus()))
                .or(() -> merchantPropertyConfigRepository
                        .findByMerchantIdAndPropertyCompanyId(
                                distributor.getId(), distributor.getPropertyCompanyId())
                        .filter(config -> "active".equals(config.getStatus())))
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_CONFIG_NOT_FOUND));
    }

    /**
     * 解析一级代理商主体：货源所属商家必须启用/审核通过；
     * 普通代理仅服务本物业公司，platform 供货范围允许跨物业公司代销。
     */
    private Merchant resolveDistributorMerchant(DistributorProduct product, Resident resident) {
        Merchant distributor = merchantRepository.findById(product.getDistributorMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "一级代理商不存在"));
        if (!"platform".equals(product.getSupplyScope())
                && !distributor.getPropertyCompanyId().equals(resident.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "该货源仅限本物业公司的业主小店代销");
        }
        return distributor;
    }

    /**
     * 下单时绑定单元楼长：按住户所属「小区 + 单元」解析启用中的楼长，
     * 并将楼长ID与分成比例快照写入订单。分成定向从订单产生那一刻算绑定关系，
     * 后续修改/撤销楼长不会影响本单分成归属。
     * 同步快照物业经理（property_managers）：同一物业在职(active)经理唯一，
     * 经理按比例从楼长分成中抽成（快照口径，后续变更不影响已产生订单）。
     */
    private void applyBuildingLeaderBinding(Order order, Resident resident) {
        if (resident == null
                || !StringUtils.hasText(resident.getCommunityId())
                || !StringUtils.hasText(resident.getUnit())) {
            applyPropertyManagerBinding(order, resident);
            return;
        }
        buildingLeaderRepository
                .findFirstByCommunityIdAndUnitAndStatusOrderByCreatedAtDesc(
                        resident.getCommunityId(), resident.getUnit(), "active")
                .ifPresent(leader -> {
                    order.setBuildingLeaderId(leader.getId());
                    order.setBuildingLeaderRate(leader.getCommissionRate());
                });
        applyPropertyManagerBinding(order, resident);
    }

    /** 物业经理快照：按住户所属物业公司解析在职经理（无楼长绑定的订单同样适用）。 */
    private void applyPropertyManagerBinding(Order order, Resident resident) {
        if (resident == null || !StringUtils.hasText(resident.getPropertyCompanyId())) {
            return;
        }
        propertyManagerRepository.findByPropertyCompanyIdAndStatus(resident.getPropertyCompanyId(), "active")
                .ifPresent(manager -> {
                    order.setManagerId(manager.getId());
                    order.setManagerRate(manager.getCommissionRate());
                });
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
        // 固化家庭池快照：后续回调/退款按 order.familyId 判断原路退回，不依赖住户当前状态。
        if (useFamilyPool && !StringUtils.hasText(order.getFamilyId())
                && StringUtils.hasText(resident.getFamilyId())) {
            order.setFamilyId(resident.getFamilyId());
        }

        // 优惠券：锁定并抵扣商品金额（由商家承担；配送费不参与抵扣）
        BigDecimal couponAmount = couponService.lockForOrder(request.getUserCouponId(),
                resident.getId(), merchant.getId(), order.getTotalAmount(), order.getId());
        if (couponAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setCouponId(request.getUserCouponId());
            order.setCouponAmount(couponAmount);
            orderRepository.save(order);
            payable = payable.subtract(couponAmount).max(BigDecimal.ZERO);
        }

        PaymentBreakdown breakdown = resolvePaymentBreakdown(request, payable, config, resident, useFamilyPool);
        if (breakdown.cashAmount().compareTo(BigDecimal.ZERO) > 0) {
            // 含现金部分：微信预下单，订单保持 pending，支付成功后由回调 completeChannelPayment 落账
            return initiateChannelPayment(order, resident, merchant, items, request, payable, breakdown);
        }

        // 纯积分/物业币即时支付：直接落账
        deductStock(order, items);
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
        // 支付成功：核销优惠券
        couponService.markUsedByOrder(order.getId());

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
                .couponAmount(defaultDecimal(order.getCouponAmount()))
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
        deductStock(order, items);
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
                .couponAmount(defaultDecimal(order.getCouponAmount()))
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
            // 取消/退款竞态：订单已取消但微信侧支付成功，钱必须原路退回。
            if ("cancelled".equals(order.getOrderStatus())) {
                refundCashForCancelledPayment(order, record);
            }
            return; // 幂等：已支付/已取消/已关闭
        }

        Resident resident = residentRepository.findById(order.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        Merchant merchant = merchantRepository.findById(order.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        int pointUsed = record.getPointUsed() != null ? record.getPointUsed() : 0;
        BigDecimal coinUsed = record.getCoinUsed() != null ? record.getCoinUsed() : BigDecimal.ZERO;
        // 与预下单保持一致：订单有 familyId 或住户属于家庭时，先扣家庭池，不足再扣个人余额。
        boolean useFamilyPool = StringUtils.hasText(order.getFamilyId())
                || StringUtils.hasText(resident.getFamilyId());
        if (pointUsed > 0) {
            pointService.spendPoints(resident, pointUsed, "order", order.getId(),
                    "订单消费抵扣积分", null, useFamilyPool);
        }
        if (coinUsed.compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(resident, coinUsed, "order", order.getId(),
                    "订单消费抵扣物业币", null, useFamilyPool);
        }

        // 库存已在预下单（payOrder → initiateChannelPayment）预占扣减，此处严禁再扣一次：
        // 重复扣减在库存充足时会静默超卖，末件商品时抛「商品库存不足」使 finalize 永久失败，
        // 造成「钱已收、订单永久 pending」。超时未支付由 PendingPaymentScheduler 归还预占库存。
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
        // 支付成功：核销优惠券（券在 payOrder 预定时已锁定并记录在订单上）
        couponService.markUsedByOrder(order.getId());

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
        Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);

        OrderDetailResponse detail = orderConverter.toDetail(order, merchant, items, maskPhone, delivery);
        // 支付流水状态独立于订单状态：amount_mismatch 时订单仍是 pending，前端需并列展示“支付待对账”。
        paymentRecordRepository.findFirstByBizTypeAndBizIdOrderByCreatedAtDesc(
                        PaymentBizType.ORDER, order.getId())
                .ifPresent(record -> detail.setPaymentStatus(record.getStatus()));
        return detail;
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
                || "refunded".equals(order.getOrderStatus())
                || "refunding".equals(order.getOrderStatus())
                || "refund_rejected".equals(order.getOrderStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        BigDecimal refundCoin = defaultDecimal(order.getCoinUsed());
        int refundPoint = defaultPoint(order.getPointUsed());
        BigDecimal refundAmount = refundCoin.add(defaultDecimal(order.getCashAmount()));

        cancelAndRefund(order, resident, request.getReason(), "user");

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
     * 统一的取消/拒单退款入口（用户取消、系统超时、商家拒单共用）。
     *
     * <p>pending：只关微信单/恢复预占库存，不退积分/物业币（尚未扣减）；
     * 已支付：退积分/物业币、扣回返现、恢复库存、撤销分账/配送、退现金。
     * 现金退款异步完成，refundStatus 先置 refunding，由退款轮询落 refunded/failed。</p>
     */
    @Transactional
    public void cancelAndRefund(Order order, Resident resident, String reason, String cancelledBy) {
        if ("cancelled".equals(order.getOrderStatus())
                || "refunded".equals(order.getOrderStatus())
                || "refunding".equals(order.getOrderStatus())
                || "refund_rejected".equals(order.getOrderStatus())
                || "refunding".equals(order.getRefundStatus())) {
            return;
        }
        boolean pending = "pending".equals(order.getOrderStatus());
        boolean hasPaidRecord = !paymentRecordRepository
                .findByBizTypeAndBizIdAndStatus(PaymentBizType.ORDER, order.getId(), "paid").isEmpty();

        if (pending) {
            if (hasPaidRecord) {
                // 支付已成功但 finalize 尚未执行：只退现金，积分/物业币尚未扣减，不能退。
                // 此时 order.cashAmount 可能还没被 finalize 写入，必须以支付流水快照为准。
                PaymentRecord paidRecord = paymentRecordRepository
                        .findByBizTypeAndBizIdAndStatus(PaymentBizType.ORDER, order.getId(), "paid")
                        .stream().findFirst().orElse(null);
                refundCashForCancelledPayment(order, paidRecord);
            } else {
                closePendingPayments(order);
            }
            restoreStock(order, orderItemRepository.findByOrderId(order.getId()));
        } else {
            int refundPoint = defaultPoint(order.getPointUsed());
            BigDecimal refundCoin = defaultDecimal(order.getCoinUsed());
            refundPayment(resident, order, refundPoint, refundCoin);
            restoreStock(order, orderItemRepository.findByOrderId(order.getId()));
            reverseRewards(resident, order);
            refundCashIfAny(order, reason);
        }

        order.setOrderStatus("cancelled");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        order.setCancelledBy(cancelledBy);
        if ("refunded".equals(order.getRefundStatus()) || "refunding".equals(order.getRefundStatus())) {
            order.setRefundFailReason(null);
        }

        try {
            orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        deliveryService.failDeliveryForCancelledOrder(order.getId());
        distributionService.reverseForCancelledOrder(order.getId());
        // 取消/退款：释放优惠券回用户账户
        couponService.releaseByOrder(order.getId());
    }

    /** 关闭订单下所有 pending 微信支付单；关单失败也本地置 closed，回调到账时走自动退款兜底。 */
    private void closePendingPayments(Order order) {
        List<PaymentRecord> pendingRecords = paymentRecordRepository
                .findByBizTypeAndBizIdAndStatus(PaymentBizType.ORDER, order.getId(), "pending");
        for (PaymentRecord record : pendingRecords) {
            if (wechatPayService.isEnabled()) {
                try {
                    wechatPayService.close(record);
                } catch (Exception e) {
                    record.setStatus("closed");
                    record.setErrorMsg("订单取消关单失败，已本地关闭: " + e.getMessage());
                    paymentRecordRepository.save(record);
                    log.warn("订单取消关单失败，已本地置 closed，若用户支付将由回调退款 orderId={} recordId={}",
                            order.getId(), record.getId(), e);
                }
            } else {
                record.setStatus("closed");
                record.setErrorMsg("订单取消自动关闭");
                paymentRecordRepository.save(record);
            }
        }
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
        // v8.21：积分仅用于转物业抵扣物业费，商城下单不再支持积分抵扣
        if (request.getPointUsed() != null && request.getPointUsed() > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "积分仅可用于抵扣物业费，商城下单不支持积分抵扣");
        }
        int pointUsed = 0;
        MerchantPropertyConfig config = loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());
        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate()
                : new BigDecimal("100.0000");
        BigDecimal pointValue = BigDecimal.valueOf(pointUsed)
                .divide(exchangeRate, 2, RoundingMode.HALF_UP);
        BigDecimal couponAmount = couponService.previewDiscount(request.getUserCouponId(),
                resident.getId(), merchant.getId(), productAmount);
        BigDecimal cashAmount = totalAmount.subtract(couponAmount).subtract(coinUsed).subtract(pointValue)
                .max(BigDecimal.ZERO);

        return PreviewOrderResponse.builder()
                .totalAmount(totalAmount)
                .subtotal(productAmount)
                .productAmount(productAmount)
                .couponId(couponAmount.compareTo(BigDecimal.ZERO) > 0 ? request.getUserCouponId() : null)
                .couponAmount(couponAmount)
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
        // 试算口径与 createListingOrder 一致：配送费/结算按一级代理商主体计算
        Merchant shopMerchant = resolveShopMerchant(listing);
        assertMerchantAvailable(shopMerchant);
        Merchant merchant = resolveDistributorMerchant(product, resident);
        assertMerchantAvailable(merchant);
        loadListingMerchantConfig(merchant, resident);

        int quantity = request.getQuantity() == null || request.getQuantity() < 1 ? 1 : request.getQuantity();
        int remaining = listing.getQuantityRemaining() == null ? 0 : listing.getQuantityRemaining();
        if (remaining < quantity) {
            throw new BizException(ErrorCode.LISTING_STOCK_INSUFFICIENT);
        }
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
        // v8.21：积分仅用于转物业抵扣物业费，商城下单不再支持积分抵扣
        if (request.getPointUsed() != null && request.getPointUsed() > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "积分仅可用于抵扣物业费，商城下单不支持积分抵扣");
        }
        int pointUsed = 0;
        MerchantPropertyConfig config = loadMerchantConfig(merchant.getId(), resident.getPropertyCompanyId());
        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate()
                : new BigDecimal("100.0000");
        BigDecimal pointValue = BigDecimal.valueOf(pointUsed)
                .divide(exchangeRate, 2, RoundingMode.HALF_UP);
        BigDecimal couponAmount = couponService.previewDiscount(request.getUserCouponId(),
                resident.getId(), merchant.getId(), productAmount);
        BigDecimal cashAmount = totalAmount.subtract(couponAmount).subtract(coinUsed).subtract(pointValue)
                .max(BigDecimal.ZERO);

        return PreviewOrderResponse.builder()
                .totalAmount(totalAmount)
                .subtotal(productAmount)
                .productAmount(productAmount)
                .couponId(couponAmount.compareTo(BigDecimal.ZERO) > 0 ? request.getUserCouponId() : null)
                .couponAmount(couponAmount)
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
        restoreStock(order, orderItemRepository.findByOrderId(order.getId()));
        reverseRewards(resident, order);
        refundCashIfAny(order, "退货审核通过");
        if (!StringUtils.hasText(order.getRefundStatus())) {
            order.setRefundStatus("refunded");
        }
        // 业主小店订单：已结算的结算单冲正（负向钱包流水 + sold_quantity 回退 + 结算单置 reversed）
        residentMerchantSettlementService.refundOnOrderRefunded(order);

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
     *
     * <p>用户主动退款受运营开关 {@code wechat-split.user-refund-enabled} 控制（P1-4 修复：
     * 此前该配置无任何读取方，开关形同虚设）；默认关闭，需管理端显式开启。</p>
     */
    @Transactional
    public RefundResponse requestRefund(String orderId, RefundRequestDTO request) {
        roleGuard.requireAnyRole("resident");
        if (!wechatSplitProperties.isUserRefundEnabled()) {
            throw new BizException(ErrorCode.REFUND_NOT_ENABLED);
        }
        if (!orderId.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }
        Resident resident = getActiveResident(UserContext.currentUserId());
        Order order = getOrderForUpdate(orderId);
        assertOrderOwner(order, resident.getId());

        String status = order.getOrderStatus();
        // 「确认收货 = 资金终态」业务规则：completed 订单已触发微信分账（资金按分账链路解付），
        // 系统内不再支持退货退款；有质量问题走物业线下客服处理。此前 completed 在可申请列表内，
        // 而分账完结后微信侧不可回退（普通分账）/ 回退受限，会让审核流进入必然失败的中间态。
        if ("completed".equals(status)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                    "订单已完成确认收货，不支持线上退货退款，如有问题请联系物业线下处理");
        }
        if (!"paid".equals(status) && !"delivering".equals(status)
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
     *   <li>其它 → {@code paid} + {@code pending_choice} + 30 分钟选择时限（此时<b>不</b>生成配送单，
     *       待商家选择 {@code merchant_self}/{@code courier_hall} 或超时兜底时才生成）。</li>
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
        // 支付后进入「待商家选择」阶段：此时不生成配送单，配送单在商家选择
        // merchant_self / courier_hall（或超时兜底）时才创建，避免未选择的订单提前出现在抢单大厅。
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

        // 确认收货：生成分成记录（含楼长分成）与业主卖货明细
        orderCompletionService.settleOnCompleted(order);

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

        // 商家/快递员置为已完成时，同样必须生成分成记录（含楼长分成）并结算业主卖货明细
        if ("completed".equals(order.getOrderStatus())) {
            orderCompletionService.settleOnCompleted(order);
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

    /**
     * 订单状态流转（§8.8）。
     *
     * <p><b>资金终态规则（2026-10-16 v8.12 收紧）</b>：{@code completed} 是资金终态——
     * 一旦置完成即生成分成记录并触发微信分账冻结，且按「确认收货 = 资金终态」规则
     * 不再支持线上退货退款。因此平台配送单的完成权归居民（{@code POST /orders/{id}/confirm}），
     * 配送员完成配送只推进配送单（{@code POST /deliveries/{id}/complete}），
     * 订单停留 {@code delivering} + {@code deliveredAt}（已送达待确认），
     * 超期由 {@code OrderAutoConfirmScheduler} 自动确认兜底。</p>
     *
     * <p>商家自配单由商家确认送达时后端直接级联完成订单（{@code DeliveryService.merchantConfirm}），
     * 不经过本接口；本接口中 courier 角色不再允许置 {@code completed}，
     * 防止「配送完成即跳过居民确认收货、资金提前解冻分账」。</p>
     */
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
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                        "配送完成不等于订单完成：订单完成须由居民确认收货（POST /orders/{id}/confirm），"
                                + "或超期自动确认；配送员请调用 POST /deliveries/{id}/complete");
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
        Map<String, Delivery> deliveryMap = loadDeliveryMap(orderPage.getContent());

        List<OrderListItemResponse> list = orderPage.getContent().stream()
                .map(order -> orderConverter.toListItem(
                        order,
                        merchantMap.get(order.getMerchantId()),
                        itemMap.getOrDefault(order.getId(), List.of()),
                        previewLimit,
                        deliveryMap.get(order.getId())))
                .toList();

        return PageResponse.of(list, page, pageSize, orderPage.getTotalElements());
    }

    /** 按订单批量加载配送单（避免列表 N+1；无配送单的订单不出现在 Map 中） */
    private Map<String, Delivery> loadDeliveryMap(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Map.of();
        }
        List<String> orderIds = orders.stream().map(Order::getId).distinct().toList();
        return deliveryRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(Delivery::getOrderId, Function.identity(), (a, b) -> a));
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
        Map<String, Delivery> deliveryMap = loadDeliveryMap(orderPage.getContent());
        List<OrderListItemResponse> list = orderPage.getContent().stream()
                .map(order -> orderConverter.toListItem(
                        order,
                        merchantMap.get(order.getMerchantId()),
                        itemMap.getOrDefault(order.getId(), List.of()),
                        3,
                        deliveryMap.get(order.getId())))
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

    /**
     * 商城库存扣减：统一委托 {@link ProductStockService}（行锁 + @Version 乐观锁，防并发超卖）。
     * 业主上架货源商品（dpr_）不占用商城库存：改为锁定 listing 可售件数（quantity_remaining 扣减）。
     */
    private void deductStock(Order order, List<OrderItem> items) {
        productStockService.deductAll(listingSourcedExcluded(items));
        if (order.getListingId() != null) {
            listingStockService.deductForOrder(order.getListingId(), listingQuantity(items));
        }
    }

    /**
     * 商城库存恢复（取消 / 超时关单 / 退货）：同样统一委托 {@link ProductStockService}。
     * 业主上架货源商品（dpr_）恢复 listing 可售件数（quantity_remaining 加回）。
     */
    private void restoreStock(Order order, List<OrderItem> items) {
        // pending 订单只有发起过微信预下单才扣过库存；从未发起支付的 pending 不能回补，否则库存虚增。
        if ("pending".equals(order.getOrderStatus())
                && !paymentRecordRepository.existsByBizId(order.getId())) {
            return;
        }
        productStockService.restoreAll(listingSourcedExcluded(items));
        if (order.getListingId() != null) {
            listingStockService.restoreForOrder(order.getListingId(), listingQuantity(items));
        }
    }

    /** 业主上架货源商品（dpr_）明细的总件数（listing 订单为单 item）。 */
    private int listingQuantity(List<OrderItem> items) {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .filter(this::isListingSourcedItem)
                .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();
    }

    /** 过滤掉业主上架货源商品（dpr_），仅保留占用商城库存的商品明细。 */
    private List<OrderItem> listingSourcedExcluded(List<OrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().filter(item -> !isListingSourcedItem(item)).toList();
    }

    /** 是否业主上架货源商品（OrderItem 记录的一级经销商商品 id，前缀 dpr_）。 */
    private boolean isListingSourcedItem(OrderItem item) {
        return item != null && item.getProductId() != null && item.getProductId().startsWith("dpr_");
    }

    /**
     * 服务端支付拆分与规则校验。
     *
     * <p>金额以服务端为准：物业币走商城规则（开关/最低金额/最大抵扣比例/真实余额），
     * 积分校验真实可用余额；混合支付不允许“积分+物业币超过应付”，不再保留前端本地兜底空间。</p>
     */
    private PaymentBreakdown resolvePaymentBreakdown(PayOrderRequest request,
                                                     BigDecimal payable,
                                                     MerchantPropertyConfig config,
                                                     Resident resident,
                                                     boolean useFamilyPool) {
        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate()
                : new BigDecimal("100.0000");
        PropertyCompany company = StringUtils.hasText(resident.getPropertyCompanyId())
                ? propertyCompanyRepository.findById(resident.getPropertyCompanyId()).orElse(null)
                : null;
        BigDecimal availableCoin = resolveAvailableCoin(resident, useFamilyPool);
        int availablePoints = resolveAvailablePoints(resident, useFamilyPool);

        return switch (request.getPaymentMethod()) {
            case "property_coin" -> {
                requireCoinRules(company);
                propertyCoinMallRuleService.assertCoinAllowed(company, payable, payable, availableCoin);
                yield new PaymentBreakdown(0, payable, BigDecimal.ZERO);
            }
            case "point" -> {
                int pointsNeeded = payable.multiply(exchangeRate)
                        .setScale(0, RoundingMode.UP)
                        .intValue();
                if (pointsNeeded > availablePoints) {
                    throw new BizException(ErrorCode.POINT_INSUFFICIENT);
                }
                yield new PaymentBreakdown(pointsNeeded, BigDecimal.ZERO, BigDecimal.ZERO);
            }
            case "wechat" -> new PaymentBreakdown(0, BigDecimal.ZERO, payable);
            case "mixed" -> {
                // v8.21：积分仅用于转物业抵扣物业费，商城下单不再支持积分抵扣
        if (request.getPointUsed() != null && request.getPointUsed() > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "积分仅可用于抵扣物业费，商城下单不支持积分抵扣");
        }
        int pointUsed = 0;
                BigDecimal coinUsed = request.getCoinUsed() != null ? request.getCoinUsed() : BigDecimal.ZERO;
                if (pointUsed < 0 || coinUsed.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "积分/物业币抵扣不能为负数");
                }
                if (pointUsed > availablePoints) {
                    throw new BizException(ErrorCode.POINT_INSUFFICIENT);
                }
                BigDecimal pointValue = BigDecimal.valueOf(pointUsed)
                        .divide(exchangeRate, 2, RoundingMode.HALF_UP);
                if (pointValue.add(coinUsed).compareTo(payable) > 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "积分与物业币抵扣金额超过应付金额");
                }
                if (coinUsed.compareTo(BigDecimal.ZERO) > 0) {
                    requireCoinRules(company);
                    // 使用规则服务归一化后的金额，避免 10.005 这类超出 2 位小数的输入绕过余额/比例校验。
                    coinUsed = propertyCoinMallRuleService.assertCoinAllowed(
                            company, payable, coinUsed, availableCoin);
                }
                BigDecimal cashAmount = payable.subtract(pointValue).subtract(coinUsed);
                yield new PaymentBreakdown(pointUsed, coinUsed, cashAmount);
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        };
    }

    private void requireCoinRules(PropertyCompany company) {
        if (company == null) {
            throw new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND, "当前住户未关联物业公司，无法使用物业币");
        }
    }

    private BigDecimal resolveAvailableCoin(Resident resident, boolean useFamilyPool) {
        BigDecimal available = defaultDecimal(resident.getCoinBalance());
        if (useFamilyPool && StringUtils.hasText(resident.getFamilyId())) {
            available = available.add(resolveFamilyBalance(resident.getFamilyId()));
        }
        return available;
    }

    private int resolveAvailablePoints(Resident resident, boolean useFamilyPool) {
        int available = defaultPoint(resident.getPointBalance());
        if (useFamilyPool && StringUtils.hasText(resident.getFamilyId())) {
            available += familyRepository.findById(resident.getFamilyId())
                    .map(family -> family.getPointBalance() != null ? family.getPointBalance() : 0)
                    .orElse(0);
        }
        return available;
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
        boolean refunded = false;
        if (refundPoint > 0) {
            pointService.refundOrderPoints(resident, order.getId(), refundPoint, order.getFamilyId());
            refunded = true;
        }
        if (refundCoin.compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.refundOrderCoins(resident, order.getId(), refundCoin, order.getFamilyId());
            refunded = true;
        }
        if (refunded && !StringUtils.hasText(order.getRefundStatus())) {
            // 纯积分/物业币退款即时完成；若有现金退款，随后 refundCashIfAny 会覆盖为 refunding。
            order.setRefundStatus("refunded");
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
            if (!"pending".equals(order.getOrderStatus())) {
                order.setRefundStatus("failed");
                order.setRefundFailReason("未找到已支付流水，需人工对账");
            }
            return;
        }
        BigDecimal cash = defaultDecimal(record.getCashAmount());
        if (cash.compareTo(BigDecimal.ZERO) <= 0) {
            if (!"pending".equals(order.getOrderStatus())) {
                order.setRefundStatus("failed");
                order.setRefundFailReason("支付流水现金金额为 0，需人工对账");
            }
            return;
        }
        // 分账回退 fail-closed（P0-3 修复）：任一笔分账未回退完成即抛错回滚整个取消 / 审核事务，
        // 杜绝「部分回退 + 已发起退款」的资金中间态；本地状态随事务回滚，重试时微信侧按
        // 确定性 out_return_no 幂等处理，不会重复回退。订单无分账记录时（分账未启用 / 确认收货前取消）
        // reverseForOrder 直接通过，不影响既有退款流程。
        wechatSplitService.reverseForOrder(order.getId());
        String outRefundNo = wechatPayService.refund(record, cash, reason);
        record.setOutRefundNo(outRefundNo);
        record.setStatus("refunding");
        paymentRecordRepository.save(record);
        order.setRefundStatus("refunding");
        order.setRefundFailReason(null);
        log.info("订单现金退款已发起 orderId={} cash={} outRefundNo={}", order.getId(), cash, outRefundNo);
    }

    /**
     * 订单已取消但微信侧支付成功：只退现金，不重复退积分/物业币（取消时尚未扣减）。
     */
    private void refundCashForCancelledPayment(Order order, PaymentRecord record) {
        if (record == null
                || !"paid".equals(record.getStatus())
                || "refunding".equals(order.getRefundStatus())
                || "refunded".equals(order.getRefundStatus())) {
            return;
        }
        BigDecimal cash = record.getCashAmount() != null ? record.getCashAmount() : BigDecimal.ZERO;
        if (cash.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        try {
            wechatSplitService.reverseForOrder(order.getId());
            String outRefundNo = wechatPayService.refund(record, cash, "订单已取消，支付成功后原路退款");
            record.setOutRefundNo(outRefundNo);
            record.setStatus("refunding");
            paymentRecordRepository.save(record);
            order.setRefundStatus("refunding");
            order.setRefundFailReason(null);
            orderRepository.save(order);
            log.warn("订单已取消但支付成功，已发起原路退款 orderId={} cash={} outRefundNo={}",
                    order.getId(), cash, outRefundNo);
        } catch (Exception e) {
            record.setStatus("refund_failed");
            record.setErrorMsg("订单取消后支付成功，自动退款失败，需人工处理: " + e.getMessage());
            paymentRecordRepository.save(record);
            order.setRefundStatus("failed");
            order.setRefundFailReason("自动退款失败，需人工处理");
            orderRepository.save(order);
            log.error("订单取消后支付成功，自动退款失败，需人工对账 orderId={}", order.getId(), e);
        }
    }

    private void reverseRewards(Resident resident, Order order) {
        if (defaultPoint(order.getPointEarned()) > 0) {
            pointService.reverseOrderEarnedPoints(resident, order.getId(), order.getPointEarned());
        }
        if (defaultDecimal(order.getCoinEarned()).compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.reverseOrderEarnedCoins(resident, order.getId(), order.getCoinEarned());
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
        if (StringUtils.hasText(order.getFamilyId())) {
            return true;
        }
        // 家庭成员默认使用家庭池：家庭建立时个人余额已并入家庭池，个人余额通常为 0。
        return StringUtils.hasText(resident.getFamilyId());
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
