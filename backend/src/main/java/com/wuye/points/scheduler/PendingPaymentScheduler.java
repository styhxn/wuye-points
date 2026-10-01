package com.wuye.points.scheduler;

import com.wuye.points.entity.Order;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.repository.ActivityGroupSubscriptionRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.ServiceOrderRepository;
import com.wuye.points.repository.ServiceSubscriptionRepository;
import com.wuye.points.service.ProductStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 超时未支付流水兜底调度：
 * 用户发起微信预支付但未完成支付时，微信侧在 timeout_express（默认120分钟）后自动关闭订单，
 * 本调度负责把系统中残留的 pending 流水与对应业务单收尾（订单置为已取消），避免永久悬挂。
 *
 * <p>注意：不能按「微信支付是否启用」早退——一旦支付开关由开切关、或预下单后一直未回调，
 * 残留的 pending 流水将永远不会被清理，用户会永久卡在「支付处理中，请勿重复提交」，
 * 业务单（如业主商户保证金 pending_deposit）也无法再推进。这里对所有超时 pending 流水统一收尾。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingPaymentScheduler {

    /** 兜底超时窗口：流水创建超过该时长仍 pending 即收尾 */
    private static final int PENDING_TIMEOUT_MINUTES = 30;

    private final PaymentRecordRepository paymentRecordRepository;
    private final OrderRepository orderRepository;
    private final ActivityGroupSubscriptionRepository activityGroupSubscriptionRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceSubscriptionRepository serviceSubscriptionRepository;
    private final ProductStockService productStockService;
    private final PayChannel wechatPayService;

    @Scheduled(cron = "0 */10 * * * ?")
    @SchedulerLock(name = "pending_payment_close", lockAtMostFor = "PT9M", lockAtLeastFor = "PT0S")
    @Transactional
    public void closeExpiredPendingPayments() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PENDING_TIMEOUT_MINUTES);
        List<PaymentRecord> pending = paymentRecordRepository
                .findByStatusAndCreatedAtBefore("pending", deadline);
        if (pending.isEmpty()) {
            return;
        }
        log.info("发现 {} 笔超时未支付流水，开始收尾", pending.size());
        for (PaymentRecord record : pending) {
            try {
                settleExpired(record);
            } catch (Exception e) {
                log.error("超时流水收尾失败 recordId={}", record.getId(), e);
            }
        }
    }

    private void settleExpired(PaymentRecord record) {
        // 消除「本地 30min 关单 vs 微信 120min 超时」竞态：先主动查单/关单。
        // 已支付 → 跳过本地收尾，交由回调/finalize（或 reconcilePending 兜底）完成，避免「钱已扣、单已取消」；
        // 未支付 → 调微信关单（成功则内部已置 closed），防止用户在本地关单后仍可支付。
        if (wechatPayService.isEnabled()) {
            try {
                String tradeState = wechatPayService.query(record);
                if ("SUCCESS".equals(tradeState)) {
                    log.warn("超时流水实际已支付，跳过本地关单（等回调/finalize） recordId={}", record.getId());
                    return;
                }
                wechatPayService.close(record);
            } catch (Exception e) {
                // 查单/关单失败（微信未配置、网络、无 out_trade_no 等）不阻塞本地收尾；
                // 微信侧 120min 超时兜底，若期间用户支付，回调侧对 closed 流水做退款防御。
                log.warn("微信查单/关单失败，继续本地收尾 recordId={}", record.getId(), e);
                record.setStatus("closed");
                record.setErrorMsg("支付超时自动关闭");
                paymentRecordRepository.save(record);
            }
        } else {
            record.setStatus("closed");
            record.setErrorMsg("支付超时自动关闭");
            paymentRecordRepository.save(record);
        }

        if (PaymentBizType.ORDER.equals(record.getBizType())) {
            orderRepository.findById(record.getBizId()).ifPresent(order -> {
                if ("pending".equals(order.getOrderStatus())) {
                    order.setOrderStatus("cancelled");
                    order.setCancelledAt(LocalDateTime.now());
                    order.setCancelReason("支付超时自动关闭");
                    orderRepository.save(order);
                    // 预支付时已锁定库存，超时取消需恢复（行锁下操作，避免与并发下单冲突）
                    productStockService.restoreByOrderId(order.getId());
                    log.info("订单支付超时自动取消 orderId={}", order.getId());
                }
            });
        } else if (PaymentBizType.SERVICE_ORDER.equals(record.getBizType())) {
            serviceOrderRepository.findById(record.getBizId()).ifPresent(order -> {
                if ("pending".equals(order.getOrderStatus())) {
                    order.setOrderStatus("cancelled");
                    serviceOrderRepository.save(order);
                    log.info("服务订单支付超时自动取消 orderId={}", order.getId());
                }
            });
        } else if (PaymentBizType.SERVICE_SUBSCRIPTION.equals(record.getBizType())) {
            serviceSubscriptionRepository.findById(record.getBizId()).ifPresent(subscription -> {
                if ("pending".equals(subscription.getStatus())) {
                    subscription.setStatus("cancelled");
                    serviceSubscriptionRepository.save(subscription);
                    log.info("服务订阅支付超时自动取消 subscriptionId={}", subscription.getId());
                }
            });
        } else if (PaymentBizType.ACTIVITY_SUBSCRIPTION.equals(record.getBizType())) {
            activityGroupSubscriptionRepository.findById(record.getBizId()).ifPresent(subscription -> {
                if ("pending".equals(subscription.getStatus())) {
                    subscription.setStatus("cancelled");
                    activityGroupSubscriptionRepository.save(subscription);
                    log.info("活动组订阅支付超时自动取消 subscriptionId={}", subscription.getId());
                }
            });
        }
        record.setStatus("closed");
        record.setErrorMsg("支付超时自动关闭");
        paymentRecordRepository.save(record);
    }

}
