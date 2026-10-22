package com.wuye.points.service;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.Resident;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * 订单完成结算编排：订单进入 {@code completed} 后统一生成分成记录 + 业主卖货明细 + 触发微信分账。
 *
 * <p>覆盖所有「订单完成」入口，避免任一入口漏结算导致商家货款、楼长分成、四级分账永久不落账：</p>
 * <ul>
 *   <li>买家确认收货 {@code OrderService.confirmOrder}；</li>
 *   <li>商家自配确认送达 {@code DeliveryService.merchantConfirm}；</li>
 *   <li>超期自动确认收货 {@code DeliveryService.autoConfirmDeliveredOrders}。</li>
 * </ul>
 *
 * <p>幂等：{@link DistributionService#calculateAndSave} 按订单去重，
 * {@link ResidentMerchantSettlementService#settleOnOrderCompleted} 亦按订单去重，重复调用安全。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompletionService {

    private final ResidentRepository residentRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final OrderRepository orderRepository;
    private final DistributionService distributionService;
    private final ResidentMerchantSettlementService residentMerchantSettlementService;
    private final WechatSplitService wechatSplitService;

    /**
     * 将 {@code delivering} 订单置为 {@code completed} 并结算（同一事务，避免「已标记完成却未结算」）。
     * 供超期自动确认收货使用；非 delivering 订单直接跳过。
     */
    @Transactional
    public void completeAndSettle(Order order) {
        if (order == null || !"delivering".equals(order.getOrderStatus())) {
            return;
        }
        order.setOrderStatus("completed");
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
        settleOnCompleted(order);
    }

    /**
     * 订单完成结算：生成分账记录（含楼长分成）与业主卖货明细。
     * 商家物业配置缺失/未激活时跳过分成并告警，不阻断订单完成。
     *
     * <p>业主小店上架商品订单（listingId 非空）禁止跑常规物业四级分成/商家配置逻辑——
     * 其收益链路为「店主差价 + 一级代理货款 + 平台佣金」，由
     * {@link ResidentMerchantSettlementService#settleOnOrderCompleted} 按件数结算。</p>
     *
     * <p><b>微信分账触发（P0-0 修复）</b>：分成记录在本方法内生成，因此微信分账也在此
     * 「订单完成」时点发起——注册事务提交后回调调用
     * {@link WechatSplitService#freezeOnOrderCompleted}。此前分账在支付成功时触发，
     * 而分成记录尚未生成，导致每个订单都被静默跳过、微信分账实际零执行。
     * 提交后触发的原因：分账要读已提交的分成记录，且微信 HTTP 调用不应持有订单事务。</p>
     */
    @Transactional
    public void settleOnCompleted(Order order) {
        if (order == null) {
            return;
        }
        if (order.getListingId() != null) {
            residentMerchantSettlementService.settleOnOrderCompleted(order);
            return;
        }
        Resident resident = residentRepository.findById(order.getResidentId()).orElse(null);
        if (resident == null) {
            log.warn("[订单结算] 住户不存在，跳过分成 orderId={} residentId={}",
                    order.getId(), order.getResidentId());
            return;
        }
        Merchant merchant = merchantRepository.findById(order.getMerchantId()).orElse(null);
        if (merchant == null) {
            log.warn("[订单结算] 商家不存在，跳过分成 orderId={} merchantId={}",
                    order.getId(), order.getMerchantId());
            return;
        }
        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), resident.getPropertyCompanyId())
                .filter(c -> "active".equals(c.getStatus()))
                .orElse(null);
        if (config != null) {
            distributionService.calculateAndSave(order, resident, config);
        } else {
            log.warn("[订单结算] 商家物业配置缺失/未激活，跳过分成 orderId={} merchantId={}",
                    order.getId(), merchant.getId());
        }
        residentMerchantSettlementService.settleOnOrderCompleted(order);
        triggerSplitAfterCommit(order.getId());
    }

    /**
     * 事务提交后触发微信分账（无事务上下文时直接调用兜底）。
     * 分账内部逐笔 try-catch 兜底并落失败态由调度补偿，异常不影响订单完成主流程。
     */
    private void triggerSplitAfterCommit(String orderId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        wechatSplitService.freezeOnOrderCompleted(orderId);
                    } catch (Exception e) {
                        log.error("[订单结算] 订单完成后分账触发异常 orderId={}", orderId, e);
                    }
                }
            });
        } else {
            try {
                wechatSplitService.freezeOnOrderCompleted(orderId);
            } catch (Exception e) {
                log.error("[订单结算] 订单完成后分账触发异常 orderId={}", orderId, e);
            }
        }
    }
}