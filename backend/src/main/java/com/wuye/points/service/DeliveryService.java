package com.wuye.points.service;

import com.wuye.points.constant.FulfillmentConstants;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.delivery.AssignDeliveryRequest;
import com.wuye.points.dto.delivery.AssignDeliveryResponse;
import com.wuye.points.dto.delivery.CompleteDeliveryRequest;
import com.wuye.points.dto.delivery.CompleteDeliveryResponse;
import com.wuye.points.dto.delivery.CourierOptionResponse;
import com.wuye.points.dto.delivery.GrabDeliveryResponse;
import com.wuye.points.dto.delivery.MyDeliveryItemResponse;
import com.wuye.points.dto.delivery.PendingDeliveryItemResponse;
import com.wuye.points.dto.delivery.UpdateDeliveryStatusRequest;
import com.wuye.points.dto.delivery.UpdateDeliveryStatusResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DeliveryRule;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.DeliveryRuleRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.DeliverySpecifications;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "acceptedAt", "deliveredAt");
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;
    /** 平台配送费分成比例默认值（商家自配时按此比例抽成，余下归商家） */
    private static final BigDecimal DEFAULT_PLATFORM_DELIVERY_SHARE_RATE = new BigDecimal("0.1000");

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRuleRepository deliveryRuleRepository;
    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final DeliveryConverter deliveryConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final WechatSplitService wechatSplitService;
    private final OrderCompletionService orderCompletionService;
    private final PlatformSettingService platformSettingService;

    /**
     * 按履约方式确保配送单存在并处于正确状态（幂等）。
     *
     * <ul>
     *   <li>{@code courier_hall}：承运方=平台配送员，状态 {@code pending}，进入待抢大厅；</li>
     *   <li>{@code merchant_self}：承运方=商家，状态 {@code merchant_self}，骑手不可见/不可抢。</li>
     * </ul>
     *
     * @return 配送单结果（{@code created=true} 表示本次新建，{@code false} 表示复用/改写已有配送单）
     */
    @Transactional
    public FulfillmentDeliveryResult ensureDelivery(Order order, Merchant merchant, String carrierType) {
        boolean merchantCarrier = FulfillmentConstants.CARRIER_TYPE_MERCHANT.equals(carrierType);
        DeliveryRule rule = loadDeliveryRule(order.getPropertyCompanyId());

        return deliveryRepository.findByOrderId(order.getId())
                .map(existing -> {
                    boolean created = false;
                    if (!carrierType.equals(existing.getCarrierType())) {
                        existing.setCarrierType(carrierType);
                        created = false;
                    }
                    if (merchantCarrier) {
                        // 商家自配：从待抢大厅撤回，配送员收入清零，配送费转成商家分成
                        existing.setStatus(FulfillmentConstants.DELIVERY_STATUS_MERCHANT_SELF);
                        existing.setCourierId(null);
                        applyMerchantSelfSplit(existing, order.getPropertyCompanyId());
                    } else if (FulfillmentConstants.DELIVERY_STATUS_MERCHANT_SELF.equals(existing.getStatus())
                            || FulfillmentConstants.DELIVERY_STATUS_MERCHANT_DELIVERING.equals(existing.getStatus())) {
                        // 商家自配 -> 平台配送：重新发布到待抢大厅
                        existing.setStatus("pending");
                        existing.setMerchantDeliveryFeeShare(null);
                        applyPlatformCourierSplit(existing, order);
                    } else if (existing.getCourierEarning() == null || existing.getPlatformEarning() == null) {
                        // 兼容存量：补齐配送分账快照
                        applyPlatformCourierSplit(existing, order);
                    }
                    deliveryRepository.save(existing);
                    return new FulfillmentDeliveryResult(existing, created);
                })
                .orElseGet(() -> {
                    Delivery delivery = new Delivery();
                    delivery.setId(idGenerator.generateId("dlv"));
                    delivery.setOrderId(order.getId());
                    delivery.setPropertyCompanyId(order.getPropertyCompanyId());
                    delivery.setCarrierType(carrierType);
                    delivery.setStatus(merchantCarrier
                            ? FulfillmentConstants.DELIVERY_STATUS_MERCHANT_SELF
                            : "pending");
                    delivery.setFee(resolveDeliveryFee(order, merchant, rule));
                    if (merchantCarrier) {
                        applyMerchantSelfSplit(delivery, order.getPropertyCompanyId());
                    } else {
                        applyPlatformCourierSplit(delivery, order);
                    }
                    delivery.setPickupAddress(merchant != null ? merchant.getAddress() : null);
                    delivery.setDeliveryAddress(order.getAddress());
                    delivery.setContactPhone(order.getContactPhone());
                    deliveryRepository.save(delivery);
                    return new FulfillmentDeliveryResult(delivery, true);
                });
    }

    /**
     * 平台配送分账（规范§5/§7.6，A1 修正）：
     * <pre>
     * 配送结算基数 = 实付配送费 + 配送补贴（满额减免额快照）
     * 公司配送收入 = 基数 × platform_delivery_share_rate
     * 配送员收入   = 基数 − 公司配送收入   ← 减法吸收分币误差
     * </pre>
     * 不再读取 {@code delivery_rules.courier_per_order} 固定每单金额（该列仅作历史兼容保留）。
     */
    private void applyPlatformCourierSplit(Delivery delivery, Order order) {
        BigDecimal base = resolveDeliverySettlementBase(order, delivery.getFee());
        BigDecimal rate = resolvePlatformDeliveryShareRate(
                order != null ? order.getPropertyCompanyId() : delivery.getPropertyCompanyId());
        BigDecimal platformEarning = money(base.multiply(rate)).max(BigDecimal.ZERO);
        delivery.setPlatformEarning(platformEarning);
        delivery.setCourierEarning(money(base.subtract(platformEarning)).max(BigDecimal.ZERO));
    }

    /** 商家自配分账：配送员收入为 0，配送费按比例拆分给平台与商家（商家分成 = 配送费 − 平台抽成） */
    private void applyMerchantSelfSplit(Delivery delivery, String propertyCompanyId) {
        BigDecimal merchantShare = resolveMerchantDeliveryFeeShare(delivery.getFee(), propertyCompanyId);
        delivery.setCourierEarning(BigDecimal.ZERO);
        delivery.setMerchantDeliveryFeeShare(merchantShare);
        delivery.setPlatformEarning(money(defaultDecimal(delivery.getFee()).subtract(merchantShare)).max(BigDecimal.ZERO));
    }

    /** 配送结算基数 = 实付配送费 + 配送补贴（满额减免额，订单快照） */
    private BigDecimal resolveDeliverySettlementBase(Order order, BigDecimal deliveryFee) {
        BigDecimal subsidy = order != null && order.getDeliverySubsidyAmount() != null
                ? order.getDeliverySubsidyAmount() : BigDecimal.ZERO;
        return money(defaultDecimal(deliveryFee).add(subsidy));
    }

    /** 公司配送抽成比例；未配置按 0 处理（该物业配送员拿全额，见回归检查点 E9） */
    private BigDecimal resolvePlatformDeliveryShareRate(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return BigDecimal.ZERO;
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getPlatformDeliveryShareRate)
                .map(rate -> rate != null ? rate : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 商家确认自配送达：仅 {@code carrier_type = merchant} 的配送单可被本单商家确认。
     * 完成后配送单置 {@code completed}，订单置 {@code completed}（与骑手送达同口径）。
     */
    @Transactional
    public CompleteDeliveryResponse merchantConfirm(String id, CompleteDeliveryRequest request) {
        roleGuard.requireAnyRole("merchant");
        validateDeliveryId(id);

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.DELIVERY_NOT_FOUND, "配送不存在"));
        if (!FulfillmentConstants.CARRIER_TYPE_MERCHANT.equals(delivery.getCarrierType())) {
            throw new BizException(ErrorCode.DELIVERY_NOT_COURIER_ORDER, "仅商家自配单可由商家确认送达");
        }

        Order order = orderRepository.findById(delivery.getOrderId()).orElse(null);
        assertMerchantOwnsOrder(order);

        if ("completed".equals(delivery.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "该配送单已完成");
        }
        if (request != null && request.getProofImageUrls() != null && request.getProofImageUrls().size() > 5) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "配送凭证最多5张");
        }

        LocalDateTime now = LocalDateTime.now();
        delivery.setStatus("completed");
        delivery.setDeliveredAt(now);
        if (request != null) {
            delivery.setCompleteRemark(request.getRemark());
            delivery.setProofImageUrls(toJsonArray(request.getProofImageUrls()));
        }
        deliveryRepository.save(delivery);

        if (order != null && !"completed".equals(order.getOrderStatus())
                && !"cancelled".equals(order.getOrderStatus())) {
            order.setOrderStatus("completed");
            order.setDeliveredAt(now);
            order.setCompletedAt(now);
            orderRepository.save(order);
            // 商家自配不经买家确认收货，订单直接完成，必须在此结算分成（含楼长分成）与业主卖货明细
            orderCompletionService.settleOnCompleted(order);
        }

        return buildCompleteResponse(delivery, order);
    }

    /**
     * 超期自动确认收货：对已送达（{@code deliveredAt} 非空）但买家长期未点「确认收货」的
     * {@code delivering} 订单，超过售后窗口期后自动置 {@code completed} 并结算。
     *
     * <p>缺失该兜底时，买家不确认收货会让订单永久停留在 {@code delivering}：
     * 商家货款、楼长分成、四级分账永不触发，而骑手配送费已先行入账。<p>
     *
     * <p>逐单处理（每单独立事务），单笔失败不影响其余订单。</p>
     *
     * @return 本次自动确认的订单数
     */
    public int autoConfirmDeliveredOrders() {
        int windowDays = platformSettingService.getRefundWindowDays();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(Math.max(windowDays, 1));
        List<Order> orders = orderRepository.findByOrderStatusAndDeliveredAtBefore("delivering", cutoff);
        int confirmed = 0;
        for (Order order : orders) {
            try {
                orderCompletionService.completeAndSettle(order);
                confirmed++;
            } catch (Exception e) {
                log.error("[订单] 超期自动确认收货失败 orderId={}", order.getId(), e);
            }
        }
        if (confirmed > 0) {
            log.info("[订单] 超期自动确认收货完成 {} 笔", confirmed);
        }
        return confirmed;
    }

    /** 商家自配：配送费扣除平台抽成后的商家分成（平台配送费分成比例默认 10%） */
    private BigDecimal resolveMerchantDeliveryFeeShare(BigDecimal fee, String propertyCompanyId) {
        BigDecimal base = defaultDecimal(fee);
        BigDecimal platformRate = propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getPlatformDeliveryShareRate)
                .orElse(null);
        if (platformRate == null) {
            platformRate = DEFAULT_PLATFORM_DELIVERY_SHARE_RATE;
        }
        return base.subtract(base.multiply(platformRate)).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
    }

    private void assertMerchantOwnsOrder(Order order) {
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        String userId = UserContext.currentUserId();
        Merchant owned = merchantRepository
                .findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(userId, order.getPropertyCompanyId())
                .orElse(null);
        if (owned == null || !owned.getId().equals(order.getMerchantId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其它商家的配送单");
        }
    }

    private String toJsonArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(values.get(i).replace("\"", "\\\"")).append('"');
        }
        return sb.append(']').toString();
    }

    /** 履约配送单处理结果 */
    public record FulfillmentDeliveryResult(Delivery delivery, boolean created) {
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
        assertCourierCarrier(delivery);
        if (!"pending".equals(delivery.getStatus()) && !"property_fallback".equals(delivery.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "配送状态不允许抢单");
        }

        Order order = orderRepository.findById(delivery.getOrderId()).orElse(null);
        BigDecimal base = resolveDeliverySettlementBase(order, delivery.getFee());
        BigDecimal platformEarning = money(base.multiply(resolvePlatformDeliveryShareRate(propertyCompanyId)))
                .max(BigDecimal.ZERO);
        BigDecimal courierEarning = money(base.subtract(platformEarning)).max(BigDecimal.ZERO);
        LocalDateTime acceptedAt = LocalDateTime.now();
        delivery.setPlatformEarning(platformEarning);
        delivery.setCourierEarning(courierEarning);

        int updated;
        if ("pending".equals(delivery.getStatus())) {
            updated = deliveryRepository.grabIfPending(id, courierId, acceptedAt, courierEarning, platformEarning);
        } else {
            delivery.setCourierId(courierId);
            delivery.setStatus("accepted");
            delivery.setAcceptedAt(acceptedAt);
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
        DeliveryConverter.DeliverySettlement settlement = deliveryConverter.resolveSettlement(saved, order);
        return GrabDeliveryResponse.builder()
                .id(saved.getId())
                .orderId(saved.getOrderId())
                .courierId(saved.getCourierId())
                .status(saved.getStatus())
                .courierEarning(defaultDecimal(saved.getCourierEarning()))
                .fee(defaultDecimal(saved.getFee()))
                .originalDeliveryFee(settlement.originalDeliveryFee())
                .deliverySubsidySponsor(settlement.subsidySponsor())
                .deliverySubsidyAmount(settlement.subsidyAmount())
                .deliverySettlementBase(settlement.settlementBase())
                .platformDeliveryShare(defaultDecimal(saved.getPlatformEarning()))
                .courierShare(defaultDecimal(saved.getCourierEarning()))
                .settlementStatus(resolveSettlementStatus(saved))
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
        assertCourierCarrier(delivery);
        assertAssignedCourier(delivery, courierId);

        if (!"accepted".equals(delivery.getStatus()) && !"delivering".equals(delivery.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "配送状态不允许完成");
        }

        LocalDateTime now = LocalDateTime.now();
        delivery.setStatus("completed");
        delivery.setDeliveredAt(now);
        if (request != null) {
            delivery.setCompleteRemark(request.getRemark());
            delivery.setProofImageUrls(toJsonArray(request.getProofImageUrls()));
        }
        deliveryRepository.save(delivery);

        syncOrderAfterDeliveryComplete(delivery.getOrderId(), now);

        // 电商收付通：配送完成后把配送费直分给快递员（幂等；内部判断开关/进件/账户校验）
        try {
            wechatSplitService.freezeCourierDeliveryEarning(delivery);
        } catch (Exception e) {
            log.error("[配送] 配送费直分快递员异常 deliveryId={} orderId={}", delivery.getId(), delivery.getOrderId(), e);
        }

        return buildCompleteResponse(delivery, orderRepository.findById(delivery.getOrderId()).orElse(null));
    }

    /** 组装完成配送响应（骑手送达 / 商家自配确认送达共用），补齐配送结算链与凭证字段。 */
    private CompleteDeliveryResponse buildCompleteResponse(Delivery delivery, Order order) {
        DeliveryConverter.DeliverySettlement settlement = deliveryConverter.resolveSettlement(delivery, order);
        return CompleteDeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .status(delivery.getStatus())
                .courierEarning(defaultDecimal(delivery.getCourierEarning()))
                .fee(defaultDecimal(delivery.getFee()))
                .originalDeliveryFee(settlement.originalDeliveryFee())
                .deliverySubsidySponsor(settlement.subsidySponsor())
                .deliverySubsidyAmount(settlement.subsidyAmount())
                .deliverySettlementBase(settlement.settlementBase())
                .platformDeliveryShare(defaultDecimal(delivery.getPlatformEarning()))
                .courierShare(defaultDecimal(delivery.getCourierEarning()))
                .settlementStatus(resolveSettlementStatus(delivery))
                .merchantDeliveryFeeShare(delivery.getMerchantDeliveryFeeShare())
                .carrierType(delivery.getCarrierType())
                .proofImageUrls(parseStringArray(delivery.getProofImageUrls()))
                .remark(delivery.getCompleteRemark())
                .deliveredAt(DateTimeUtil.toIsoUtc(delivery.getDeliveredAt()))
                .build();
    }

    /** 配送结算状态：返回存储值，回退 pending（实际结算由冻结到期后的分账写入） */
    private String resolveSettlementStatus(Delivery delivery) {
        return StringUtils.hasText(delivery.getSettlementStatus())
                ? delivery.getSettlementStatus() : "pending";
    }

    private java.util.List<String> parseStringArray(String json) {
        if (!StringUtils.hasText(json)) {
            return java.util.List.of();
        }
        try {
            return JSON_MAPPER.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {
            });
        } catch (Exception e) {
            return java.util.List.of();
        }
    }

    @Transactional
    public UpdateDeliveryStatusResponse updateStatus(String id, UpdateDeliveryStatusRequest request) {
        roleGuard.requireAnyRole("courier", "property_admin");
        validateDeliveryId(id);
        validateDeliveryStatus(request.getStatus());

        Delivery delivery = getDeliveryInScope(id);
        if (roleGuard.isCourier()) {
            assertCourierCarrier(delivery);
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
        assertCourierCarrier(delivery);
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

        Order order = orderRepository.findById(delivery.getOrderId()).orElse(null);
        LocalDateTime acceptedAt = LocalDateTime.now();
        delivery.setCourierId(courier.getId());
        delivery.setStatus("accepted");
        delivery.setAcceptedAt(acceptedAt);
        applyPlatformCourierSplit(delivery, order);
        deliveryRepository.save(delivery);

        syncOrderCourier(delivery.getOrderId(), courier.getId());

        DeliveryConverter.DeliverySettlement settlement = deliveryConverter.resolveSettlement(delivery, order);
        return AssignDeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .courierId(delivery.getCourierId())
                .status(delivery.getStatus())
                .courierEarning(defaultDecimal(delivery.getCourierEarning()))
                .fee(defaultDecimal(delivery.getFee()))
                .originalDeliveryFee(settlement.originalDeliveryFee())
                .deliverySubsidySponsor(settlement.subsidySponsor())
                .deliverySubsidyAmount(settlement.subsidyAmount())
                .deliverySettlementBase(settlement.settlementBase())
                .platformDeliveryShare(defaultDecimal(delivery.getPlatformEarning()))
                .courierShare(defaultDecimal(delivery.getCourierEarning()))
                .settlementStatus(resolveSettlementStatus(delivery))
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

    /** 商家自配单守卫：{@code carrier_type = merchant} 的配送单不可被骑手抢单/改状态/完成，也不可被指派 */
    private void assertCourierCarrier(Delivery delivery) {
        if (FulfillmentConstants.CARRIER_TYPE_MERCHANT.equals(delivery.getCarrierType())) {
            throw new BizException(ErrorCode.DELIVERY_NOT_COURIER_ORDER, "商家自配单，配送员不可操作");
        }
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

    /**
     * 可用配送员分页（§9.8 GET /deliveries/couriers，与管理端 §10.5.3 GET /admin/couriers 同源）。
     * 角色：merchant / property_admin / platform_admin / coordinator / sector_leader / individual_leader；
     * 非平台管理员只能查看本人所属物业公司的配送员。
     */
    @Transactional(readOnly = true)
    public PageResponse<CourierOptionResponse> listCouriers(int page, int pageSize, String propertyCompanyId) {
        roleGuard.requireAnyRole("merchant", "property_admin", "platform_admin",
                "coordinator", "sector_leader", "individual_leader");

        String scopedPropertyCompanyId = roleGuard.isPlatformAdmin() && StringUtils.hasText(propertyCompanyId)
                ? propertyCompanyId
                : UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(scopedPropertyCompanyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无法确定物业公司");
        }

        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Resident> courierPage =
                residentRepository.findByPropertyCompanyIdAndRole(scopedPropertyCompanyId, "courier", pageable);

        List<CourierOptionResponse> list = courierPage.getContent().stream()
                .map(courier -> CourierOptionResponse.builder()
                        .id(courier.getId())
                        .courierId(courier.getId())
                        .courierName(courier.getName())
                        .status(deliveryRepository.existsByCourierIdAndStatusIn(
                                courier.getId(), List.of("accepted", "picked")) ? "online" : "offline")
                        .createdAt(DateTimeUtil.toIsoUtc(courier.getCreatedAt()))
                        .build())
                .toList();

        return PageResponse.of(list, safePage, safePageSize, courierPage.getTotalElements());
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

    private BigDecimal money(BigDecimal value) {
        return defaultDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }
}
