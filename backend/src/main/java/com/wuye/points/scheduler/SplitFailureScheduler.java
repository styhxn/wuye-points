package com.wuye.points.scheduler;

import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.entity.WechatSplitRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分账失败态补偿调度：扫描 {@code freeze_failed} / {@code finish_failed} / {@code cancel_failed} 的 微信 分账记录，
 * 重新发起对应的冻结/完结/撤销调用，避免资金卡在失败态造成资损滞留。
 *
 * <p>与 {@link ReverseFailedScheduler}（仅处理 {@code reverse_failed} 冲正）互补，覆盖分账状态机的全部失败终态。
 * 每次最多处理 {@link #BATCH_SIZE} 条，逐条重试、单条异常不影响其余，失败记录保持失败态等待下一轮。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SplitFailureScheduler {

    private static final int BATCH_SIZE = 50;

    private final WechatSplitRecordRepository splitRecordRepository;
    private final WechatSplitService wechatSplitService;

    @Scheduled(fixedDelayString = "${wechat-split.split-failure-poll-delay-ms:120000}")
    @SchedulerLock(name = "split_failure_retry", lockAtMostFor = "PT10M", lockAtLeastFor = "PT0S")
    @Transactional
    public void pollSplitFailures() {
        if (!wechatSplitService.isEnabled()) {
            return;
        }
        List<WechatSplitRecord> failures = splitRecordRepository.findByStatusIn(
                List.of("freeze_failed", "finish_failed", "cancel_failed"));
        if (failures.isEmpty()) {
            return;
        }
        log.info("[分账] 扫描到 {} 条失败态分账记录，开始补偿重试", failures.size());
        int n = 0;
        for (WechatSplitRecord split : failures) {
            if (n++ >= BATCH_SIZE) {
                break;
            }
            try {
                wechatSplitService.retryFailed(split);
            } catch (Exception e) {
                log.error("[分账] 失败态补偿重试异常 splitNo={} status={}", split.getSplitNo(), split.getStatus(), e);
            }
        }
    }
}
