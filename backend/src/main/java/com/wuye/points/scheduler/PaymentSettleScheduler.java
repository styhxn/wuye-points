package com.wuye.points.scheduler;

import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PaymentSettleOutbox;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.payment.PaymentSettleService;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PaymentSettleOutboxRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付 finalize 兜底调度。
 *
 * <p>① retryPending：扫描 PENDING/FAILED 且已过退避期的 outbox 重新 finalize，
 *    应对事件丢失、异步线程池拒绝、JVM 重启等场景（官方要求回调 3 秒内应答，业务必须异步且可重试）。</p>
 * <p>② reconcilePending：官方明确建议“主动调用 4.5 支付查询确认订单状态”。回调完全未达时，
 *    对超时未 finalize 的 pending 流水主动查询，已支付则补标 paid + finalize，超期未付则关单。</p>
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentSettleScheduler {

    private static final int RECONCILE_PENDING_SECONDS = 30; // 下单超过 30s 才主动查询，避免与回调竞争
    private static final int CLOSE_ORDER_HOURS = 2;          // 超过 2h 仍非支付成功则关单
    private static final int STUCK_PROCESSING_MINUTES = 5;   // outbox 卡在 PROCESSING 超过 5 分钟视为 JVM 崩溃遗留，回收重试

    private final PaymentSettleOutboxRepository outboxRepository;
    private final PaymentSettleService settleService;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PayChannel wechatPayService;
    private final SnowflakeIdGenerator snowflake;

    /** ① finalize 兜底。 */
    @Scheduled(fixedDelay = 10_000)
    @SchedulerLock(name = "payment_settle_retry_pending", lockAtMostFor = "PT4M", lockAtLeastFor = "PT0S")
    public void retryPending() {
        List<PaymentSettleOutbox> due = settleService.findDue(LocalDateTime.now());
        for (PaymentSettleOutbox outbox : due) {
            try {
                settleService.settle(outbox.getRecordId());
            } catch (Exception e) {
                log.error("[finalize][调度] 处理异常 recordId={}", outbox.getRecordId(), e);
            }
        }
        // 回收 JVM 崩溃遗留的卡死 PROCESSING（markProcessing 已落库、markDone 未落库）
        settleService.reclaimStuckProcessing(STUCK_PROCESSING_MINUTES);
    }

    /** ② 主动查询兜底（官方建议）。 */
    @Scheduled(fixedDelay = 30_000)
    @SchedulerLock(name = "payment_settle_reconcile_pending", lockAtMostFor = "PT4M", lockAtLeastFor = "PT0S")
    public void reconcilePending() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(RECONCILE_PENDING_SECONDS);
        List<PaymentRecord> pending = paymentRecordRepository.findByStatusAndCreatedAtBefore("pending", before);
        for (PaymentRecord record : pending) {
            PaymentSettleOutbox existing = outboxRepository.findByRecordId(record.getId()).orElse(null);
            if (existing != null && PaymentSettleOutbox.STATUS_DONE.equals(existing.getStatus())) {
                continue;
            }
            LocalDateTime now = LocalDateTime.now();
            boolean tooOld = record.getCreatedAt() != null
                    && record.getCreatedAt().isBefore(now.minusHours(CLOSE_ORDER_HOURS));
            // 无 out_trade_no（预下单失败、旧版残留等）无法在微信侧查询：跳过无效查询，超时仍直接关单收尾
            if (!StringUtils.hasText(record.getOutTradeNo())) {
                if (tooOld) {
                    record.setStatus("closed");
                    record.setErrorMsg("支付超时自动关闭");
                    paymentRecordRepository.save(record);
                    log.warn("[reconcile] 流水无 out_trade_no 且超时，直接关单 recordId={}", record.getId());
                }
                continue;
            }
            try {
                String tradeState = wechatPayService.query(record);
                if ("SUCCESS".equals(tradeState)) {
                    wechatPayService.markPaidFromQuery(record);
                    if (existing == null) {
                        outboxRepository.save(PaymentSettleOutbox.pending(record, snowflake.generateId("pso_")));
                    }
                    settleService.settle(record.getId());
                    log.info("[reconcile] 主动查询确认已支付并完成 finalize recordId={}", record.getId());
                } else if (tooOld) {
                    // 与 PendingPaymentScheduler 一致：先调微信关单消除「本地关单后仍可支付」竞态
                    try {
                        wechatPayService.close(record); // 成功后内部已置 closed 并保存
                    } catch (Exception e) {
                        log.warn("[reconcile] 微信关单失败，继续本地关单 recordId={}", record.getId(), e);
                        record.setStatus("closed");
                        paymentRecordRepository.save(record);
                    }
                    log.warn("[reconcile] 超时未支付，关单 recordId={} tradeState={}", record.getId(), tradeState);
                }
            } catch (Exception e) {
                log.warn("[reconcile] 主动查询异常 recordId={}", record.getId(), e);
            }
        }
    }
}
