package com.wuye.points.payment;

import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PaymentSettleOutbox;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PaymentSettleOutboxRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import com.wuye.points.payment.WechatSplitApiService;
import com.wuye.points.payment.WechatSplitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付业务 finalize（异步）。
 *
 * <p>职责：收到 {@link PaymentSettledEvent}（事务提交后触发）或调度器调用后，完成：
 * ① 冻结分账（微信，幂等）；② 按业务类型完成落地（订单 paid、配送、返积分/物业币、分账计算等）。</p>
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>不包大事务——内部每个业务步骤各自开启独立事务（REQUIRED），单步失败不影响其它，且不长期占用 DB 连接。</li>
 *   <li>通过 outbox 的乐观锁（@Version）抢占处理权，防止“事件触发”与“调度兜底”并发重复处理同一笔。</li>
 *   <li>失败按指数退避重试，超过上限转入 FAILED_PERMANENT 并告警（需人工对账）。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSettleService {

    private static final int MAX_RETRY = 10;
    private static final long BASE_BACKOFF_SECONDS = 30L;
    private static final long MAX_BACKOFF_SECONDS = 1800L; // 30 分钟

    private final SnowflakeIdGenerator snowflake;
    private final PaymentSettleOutboxRepository outboxRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final WechatSplitService wechatSplitService;
    private final PaymentBizDispatcher bizDispatcher;
    private final WechatSplitApiService wechatSplitApiService;
    private final WechatSplitProperties wechatSplitProperties;

    /** 事务提交后异步触发（由 PaymentCompletionService 发布）。 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("settleTaskExecutor")
    public void onSettled(PaymentSettledEvent event) {
        settle(event.getRecordId());
    }

    /**
     * finalize 单笔。可被事件或调度器调用。
     */
    public void settle(String recordId) {
        PaymentSettleOutbox outbox = outboxRepository.findByRecordId(recordId).orElse(null);
        if (outbox == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!outbox.isDue(now)) {
            return;
        }

        // 乐观锁抢占处理权，防止事件与调度器并发重复处理
        outbox.markProcessing();
        try {
            outboxRepository.saveAndFlush(outbox);
        } catch (OptimisticLockingFailureException e) {
            log.debug("outbox 被其它线程抢占处理，跳过 recordId={}", recordId);
            return;
        }

        PaymentRecord record = paymentRecordRepository.findById(recordId).orElse(null);
        if (record == null || !"paid".equals(record.getStatus())) {
            outbox.markFailed("record 不存在或状态非 paid: recordId=" + recordId, null, MAX_RETRY);
            try {
                outboxRepository.save(outbox);
            } catch (OptimisticLockingFailureException e) {
                // 标记失败时被并发接管（如原处理线程刚完成 DONE）：放弃写入，避免误标 FAILED_PERMANENT
                log.warn("[finalize] 标记失败时 outbox 已被并发接管，放弃写入 recordId={}", recordId);
            }
            return;
        }

        try {
            // 先冻结分账（幂等：已冻结则跳过）。按物业公司灰度：仅白名单内且全局开启时执行分账。
            try {
                if (wechatSplitApiService.isEnabled() && wechatSplitProperties.isEnabledForProperty(record.getPropertyCompanyId())) {
                    wechatSplitService.freezeOnPaymentSuccess(record);
                }
            } catch (Exception e) {
                log.error("[finalize] 分账冻结异常 recordId={}", recordId, e);
            }
            // 完成业务落地
            bizDispatcher.complete(record);

            outbox.markDone();
            try {
                outboxRepository.save(outbox);
            } catch (OptimisticLockingFailureException e) {
                // 已被回收/并发接管：业务 complete* 均幂等，由接管方负责收尾，放弃本次落库
                log.warn("[finalize] outbox 已被并发接管，由接管方完成收尾 recordId={}", recordId);
                return;
            }
            log.info("[finalize] 支付业务完成 recordId={} bizType={} bizId={}",
                    recordId, record.getBizType(), record.getBizId());
        } catch (Exception e) {
            LocalDateTime next = backoff(outbox.getRetryCount());
            outbox.markFailed(e.getMessage(), next, MAX_RETRY);
            try {
                outboxRepository.save(outbox);
            } catch (OptimisticLockingFailureException ole) {
                // 失败落库时被并发接管：本线程持旧快照，交由接管方重试，避免二次 StaleObject 异常
                log.warn("[finalize] 失败落库时 outbox 已被并发接管，交由接管方重试 recordId={}", recordId);
                return;
            }
            log.error("[finalize] 支付业务失败 recordId={} retry={} nextRetryAt={}",
                    recordId, outbox.getRetryCount(), next, e);
            if (PaymentSettleOutbox.STATUS_FAILED_PERMANENT.equals(outbox.getStatus())) {
                log.error("[finalize] 支付业务永久失败，需人工对账 recordId={} bizType={} bizId={}",
                        recordId, record.getBizType(), record.getBizId());
            }
        }
    }

    private LocalDateTime backoff(int retryCount) {
        long seconds = Math.min(BASE_BACKOFF_SECONDS * (1L << Math.min(retryCount, 6)), MAX_BACKOFF_SECONDS);
        return LocalDateTime.now().plusSeconds(seconds);
    }

    /** 供调度器批量扫描使用。 */
    public List<PaymentSettleOutbox> findDue(LocalDateTime now) {
        return outboxRepository.findDue(List.of(
                PaymentSettleOutbox.STATUS_PENDING, PaymentSettleOutbox.STATUS_FAILED), now);
    }

    /**
     * 回收卡死过久的 PROCESSING 记录（JVM 在 markProcessing 后、markDone 前崩溃时遗留，
     * {@code isDue} 不覆盖该状态）。通过单行条件 UPDATE 原子抢回收权，抢到才重置回 PENDING 并重新入队；
     * 各业务 complete* 均幂等，重复 finalize 安全。
     *
     * <p>并发安全：调度器多实例重叠、或原 finalize 线程仅“慢”而并未崩溃时，
     * 回收方与处理方会对同一行竞争。条件更新（WHERE status='PROCESSING' AND updated_at<cutoff）
     * 保证只有先到者能抢到回收权，后到者受 0 行影响即静默跳过，不再产生乐观锁 ERROR。</p>
     *
     * @param stuckMinutes PROCESSING 超过该时长视为卡死
     */
    @Transactional
    public void reclaimStuckProcessing(int stuckMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(stuckMinutes);
        List<PaymentSettleOutbox> stuck = outboxRepository.findStuckProcessing(
                PaymentSettleOutbox.STATUS_PROCESSING, cutoff);
        for (PaymentSettleOutbox outbox : stuck) {
            String recordId = outbox.getRecordId();
            try {
                int reclaimed = outboxRepository.reclaimIfStuck(
                        outbox.getId(), PaymentSettleOutbox.STATUS_PROCESSING,
                        PaymentSettleOutbox.STATUS_PENDING, cutoff, now);
                if (reclaimed == 0) {
                    // 已被并发线程/实例回收，或原处理线程已完成状态变更：正常竞争，跳过
                    log.debug("[finalize][回收] 已被并发回收或状态已变更，跳过 recordId={}", recordId);
                    continue;
                }
                settle(recordId);
                log.warn("[finalize][回收] 卡死 PROCESSING 重新入队并处理 recordId={} bizType={} bizId={}",
                        recordId, outbox.getBizType(), outbox.getBizId());
            } catch (OptimisticLockingFailureException e) {
                // 兜底：抢到回收权后与其它线程偶发竞争属正常，无需 ERROR
                log.debug("[finalize][回收] 回收后乐观锁竞争，交由下次调度处理 recordId={}", recordId);
            } catch (Exception e) {
                log.error("[finalize][回收] 处理卡死 PROCESSING 异常 recordId={}", recordId, e);
            }
        }
    }
}
