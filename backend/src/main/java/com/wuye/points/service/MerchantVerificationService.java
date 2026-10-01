package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.order.MerchantVerifyRequest;
import com.wuye.points.dto.order.MerchantVerifyResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderItemRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家核实订单服务。
 *
 * <p>团购/券类订单支付成功后进入 pending_verification 状态，商家需在核实时限内核实：
 * <ul>
 *   <li>通过 → verified → 冻结期 N 天 → 到期自动分账结算</li>
 *   <li>拒绝 → cancelled → 退款</li>
 *   <li>超时未核实 → 定时任务自动视为通过（进入 verified → 冻结期 → 自动分账结算）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantVerificationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final ResidentRepository residentRepository;
    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final ProductStockService productStockService;
    private final DistributionService distributionService;
    private final DeliveryService deliveryService;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final WechatSplitService wechatSplitService;
    private final PlatformSettingService platformSetting;

    /**
     * 商家核实订单（通过或拒绝）。
     */
    @Transactional
    public MerchantVerifyResponse verifyOrder(String orderId, MerchantVerifyRequest request) {
        roleGuard.requireAnyRole("merchant");
        if (!orderId.startsWith("ord_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID格式不正确");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND));
        assertOwnMerchantOrder(order);

        if (!"pending_verification".equals(order.getOrderStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "订单不在待核实状态");
        }

        if (order.getVerificationDeadline() != null
                && LocalDateTime.now().isAfter(order.getVerificationDeadline())) {
            throw new BizException(ErrorCode.ORDER_VERIFICATION_EXPIRED);
        }

        if (request.isApproved()) {
            return approveOrder(order);
        } else {
            return rejectOrder(order, request.getReason());
        }
    }

    /**
     * 核实通过：订单进入 verified 状态，设置冻结期。
     */
    private MerchantVerifyResponse approveOrder(Order order) {
        int freezeDays = platformSetting.getFreezeDays();
        LocalDateTime now = LocalDateTime.now();

        order.setOrderStatus("verified");
        order.setVerifiedAt(now);
        order.setVerifiedBy(UserContext.currentUserId());
        order.setFreezeEndDate(now.plusDays(freezeDays));

        try {
            order = orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        log.info("商家核实通过 orderId={} freezeEndDate={}", order.getId(), order.getFreezeEndDate());

        return MerchantVerifyResponse.builder()
                .orderId(order.getId())
                .verificationResult("approved")
                .orderStatus(order.getOrderStatus())
                .freezeEndDate(DateTimeUtil.toIsoUtc(order.getFreezeEndDate()))
                .verifiedAt(DateTimeUtil.toIsoUtc(order.getVerifiedAt()))
                .message("核实通过，冻结期" + freezeDays + "天")
                .build();
    }

    /**
     * 核实拒绝：订单取消 + 退款 + 恢复库存。
     */
    private MerchantVerifyResponse rejectOrder(Order order, String reason) {
        order.setOrderStatus("cancelled");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason != null ? reason : "商家核实拒绝");
        order.setVerifiedBy(UserContext.currentUserId());
        order.setVerificationRemark(reason);

        // 退款：退积分/物业币/现金
        Resident owner = residentRepository.findById(order.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        BigDecimal refundCoin = order.getCoinUsed() != null ? order.getCoinUsed() : BigDecimal.ZERO;
        int refundPoint = order.getPointUsed() != null ? order.getPointUsed() : 0;
        BigDecimal refundAmount = refundCoin.add(
                order.getCashAmount() != null ? order.getCashAmount() : BigDecimal.ZERO);

        if (refundPoint > 0) {
            pointService.spendPoints(owner, -refundPoint, "order_cancel",
                    order.getId(), "商家核实拒绝退款-积分", null, false);
        }
        if (refundCoin.compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(owner, refundCoin.negate(),
                    "order_cancel", order.getId(), "商家核实拒绝退款-物业币", null, false);
        }

        // 恢复库存（行锁下操作，与下单预占串行化，避免并发下库存错乱）
        productStockService.restoreByOrderId(order.getId());

        try {
            order = orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        // 撤销分账记录
        distributionService.reverseForCancelledOrder(order.getId());

        // 撤销配送单
        deliveryService.failDeliveryForCancelledOrder(order.getId());

        log.info("商家核实拒绝 orderId={} reason={}", order.getId(), reason);

        return MerchantVerifyResponse.builder()
                .orderId(order.getId())
                .verificationResult("rejected")
                .orderStatus(order.getOrderStatus())
                .verifiedAt(DateTimeUtil.toIsoUtc(order.getVerifiedAt()))
                .message("核实拒绝，订单已取消并退款")
                .build();
    }

    /**
     * 超时自动通过：由定时任务调用，将超过核实时限仍未核实的 pending_verification 订单自动视为通过。
     */
    @Transactional
    public int autoApproveExpiredVerifications() {
        int approved = 0;
        List<Order> expiredOrders = orderRepository
                .findByOrderStatusAndVerificationDeadlineBefore(
                        "pending_verification", LocalDateTime.now());

        for (Order order : expiredOrders) {
            try {
                approveOrder(order);
                approved++;
            } catch (Exception e) {
                log.error("超时自动通过核实订单失败 orderId={}", order.getId(), e);
            }
        }
        if (approved > 0) {
            log.info("超时自动通过核实订单 {} 笔", approved);
        }
        return approved;
    }

    /**
     * 冻结到期结算：由定时任务调用，对冻结期满的 verified 订单执行分账。
     */
    @Transactional
    public int settleFrozenOrders() {
        int settled = 0;
        List<Order> frozenOrders = orderRepository
                .findByOrderStatusAndFreezeEndDateBefore(
                        "verified", LocalDateTime.now());

        for (Order order : frozenOrders) {
            try {
                settleOrder(order);
                settled++;
            } catch (Exception e) {
                log.error("冻结到期结算失败 orderId={}", order.getId(), e);
            }
        }
        if (settled > 0) {
            log.info("冻结到期结算完成 {} 笔", settled);
        }
        return settled;
    }

    /**
     * 单笔订单结算：verified → completed + 分账 + 微信 冻结。
     */
    private void settleOrder(Order order) {
        order.setOrderStatus("completed");
        order.setCompletedAt(LocalDateTime.now());

        try {
            order = orderRepository.save(order);
        } catch (OptimisticLockingFailureException ex) {
            throw new BizException(ErrorCode.ORDER_CONFLICT);
        }

        // 同步配送单
        final Order finalOrder = order;
        deliveryRepository.findByOrderId(finalOrder.getId()).ifPresent(delivery -> {
            if (!"settled".equals(delivery.getSettlementStatus())) {
                delivery.setSettlementStatus("settled");
                delivery.setSettledAt(finalOrder.getCompletedAt());
                deliveryRepository.save(delivery);
            }
        });

        // 执行分账计算
        Resident resident = residentRepository.findById(order.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        Merchant merchant = merchantRepository.findById(order.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), order.getPropertyCompanyId())
                .filter(c -> "active".equals(c.getStatus()))
                .orElse(null);

        if (config != null) {
            distributionService.calculateAndSave(order, resident, config);
        }

        log.info("冻结到期结算完成 orderId={}", order.getId());
    }

    private void assertOwnMerchantOrder(Order order) {
        String merchantId = resolveMerchantIdByCurrentUser();
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非本商家订单");
        }
    }

    private String resolveMerchantIdByCurrentUser() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        return merchantRepository
                .findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(userId, propertyCompanyId)
                .map(Merchant::getId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }
}
