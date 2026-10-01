package com.wuye.points.scheduler;

import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.payment.WechatSplitApiService;
import com.wuye.points.payment.WechatSplitProperties;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后窗口完结调度（两级式分账第 ② 步）。
 *
 * <p>每日扫描处于 frozen（已冻结未完结）的分账流水，若已超过售后窗口
 * （paymentRecord.createdAt + afterSaleDays），自动调 6.12 finishOrder(trans_type=1) 完结划账。
 * 窗口内不处理（等待退款走撤销解冻路径）。
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SplitWindowScheduler {

    private final WechatSplitRecordRepository splitRecordRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final WechatSplitService wechatSplitService;
    private final WechatSplitProperties wechatSplitProperties;
    private final WechatSplitApiService wechatSplitApiService;

    @Scheduled(cron = "0 30 2 * * ?")
    @SchedulerLock(name = "split_finish_expired_window", lockAtMostFor = "PT2H", lockAtLeastFor = "PT1M")
    public void finishExpiredWindow() {
        if (!wechatSplitProperties.isEnabled()) {
            return;
        }
        List<WechatSplitRecord> frozen = splitRecordRepository.findByStatus("frozen");
        for (WechatSplitRecord split : frozen) {
            if (!windowExpired(split)) {
                continue;
            }
            try {
                wechatSplitService.finishAfterWindow(split);
                // 完结事务已提交，重新加载读取最新状态，事务外触发自动提现（避免外部 HTTP 持锁）
                WechatSplitRecord reloaded = splitRecordRepository.findBySplitNo(split.getSplitNo()).orElse(split);
                wechatSplitService.maybeAutoWithdraw(reloaded);
            } catch (Exception e) {
                log.error("[分账窗口] 完结/提现失败 splitNo={}", split.getSplitNo(), e);
            }
        }
    }

    private boolean windowExpired(WechatSplitRecord split) {
        if (split.getPaymentRecordId() == null) {
            return true;
        }
        PaymentRecord record = paymentRecordRepository.findById(split.getPaymentRecordId()).orElse(null);
        if (record == null || record.getCreatedAt() == null) {
            return true;
        }
        return record.getCreatedAt().plusDays(wechatSplitProperties.getAfterSaleDays()).isBefore(LocalDateTime.now());
    }

    /** 重试提现失败的分账（Path B 兜底）：对 withdraw_status=withdraw_failed 且已完结的记录重提 */
    @Scheduled(cron = "0 50 2 * * ?")
    @SchedulerLock(name = "split_retry_failed_withdrawals", lockAtMostFor = "PT2H", lockAtLeastFor = "PT1M")
    public void retryFailedWithdrawals() {
        if (!wechatSplitProperties.isEnabled()) return;
        if (!wechatSplitProperties.isAutoWithdrawEnabled()) return;
        List<WechatSplitRecord> failed = splitRecordRepository.findByWithdrawStatus("withdraw_failed");
        for (WechatSplitRecord split : failed) {
            try {
                wechatSplitService.retryWithdraw(split);
            } catch (Exception e) {
                log.error("[微信提现] 重试调度失败 splitNo={}", split.getSplitNo(), e);
            }
        }
    }

    /**
     * 缺户补分 job（HTML ②-7）：扫描 skipped（未开户/注册中的配送员等）记录，
     * 重新解析 微信 账户并尝试补冻。开户/注册完成后下一周期自动追回，避免漏分。
     */
    @Scheduled(cron = "0 20 3 * * ?")
    @SchedulerLock(name = "split_retry_skipped", lockAtMostFor = "PT2H", lockAtLeastFor = "PT1M")
    public void retrySkippedSplitRecords() {
        try {
            wechatSplitService.retrySkippedSplitRecords();
        } catch (Exception e) {
            log.error("[微信补分] 缺户补分调度异常", e);
        }
    }

    /**
     * 每日资金对账（HTML ③-a，内部侧）：统计未完成/失败-待补记录，发现需人工介入项即告警。
     * 外部核对（本地流水 vs 微信实际划账）依赖微信对账文件/接口，待对接后补充 {@code fetchSaobeiReconciliation()}。
     */
    @Scheduled(cron = "0 10 3 * * ?")
    @SchedulerLock(name = "split_reconcile_daily", lockAtMostFor = "PT1H", lockAtLeastFor = "PT1M")
    public void reconcileDaily() {
        if (!wechatSplitProperties.isEnabled()) {
            return;
        }
        List<String> unfinished = List.of("pending", "frozen", "finished", "withdrawing");
        List<String> failed = List.of("freeze_failed", "finish_failed", "withdraw_failed",
                "reverse_failed", "refund_failed", "skipped");
        long total = splitRecordRepository.count();
        long unfinishedCount = splitRecordRepository.findByStatusIn(unfinished).size();
        long failedCount = splitRecordRepository.findByStatusIn(failed).size();
        log.info("[微信对账] 每日内部核对 total={} 未完成={} 失败或待补={}", total, unfinishedCount, failedCount);
        if (failedCount > 0) {
            List<WechatSplitRecord> failedRecords = splitRecordRepository.findByStatusIn(failed);
            log.warn("[微信对账] 存在需人工介入记录 {} 条，splitNo 示例: {}",
                    failedCount, failedRecords.stream().limit(20).map(WechatSplitRecord::getSplitNo).toList());
        }
        // TODO(外部对账): 拉取微信对账文件/接口，比对本地 freezeAmount 与微信实际划账，差异写入告警。
    }

    /**
     * 归集户余额监控（HTML ③-b）：当归集户余额超过阈值时告警，防止资金长期滞留、规避合规/挪用风险。
     * 余额查询依赖微信归集户余额接口（{@code WechatSplitApiService#queryCollectionBalance}），
     * 接口未对接时该方法安全跳过（不误报）。
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @SchedulerLock(name = "split_monitor_collection_balance", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void monitorCollectionBalance() {
        BigDecimal threshold = wechatSplitProperties.getCollectBalanceAlertThreshold();
        if (threshold == null) {
            return;
        }
        BigDecimal balance = wechatSplitApiService.queryCollectionBalance();
        if (balance == null) {
            log.info("[微信监控] 归集户余额查询未对接（WechatSplitApiService.queryCollectionBalance 待实现），跳过本次告警");
            return;
        }
        if (balance.compareTo(threshold) > 0) {
            log.warn("[微信监控] 归集户余额 {} 超过阈值 {}，请尽快发起平台代发提现，避免资金滞留", balance, threshold);
        }
    }
}
