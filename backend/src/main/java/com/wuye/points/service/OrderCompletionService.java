package com.wuye.points.service;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单完成结算编排：订单进入 {@code completed} 后统一生成分成记录 + 业主卖货明细。
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
     */
    @Transactional
    public void settleOnCompleted(Order order) {
        if (order == null) {
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
    }
}