package com.wuye.points.scheduler;

import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.entity.WechatSplitRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    /**
     * 失败态补偿轮询。
     *
     * <p><b>不持有外层事务（上线前加固）</b>：每条 {@code retryFailed} 自行管理事务，
     * 单条记录的微信调用 + 状态落库独立提交，避免批量重试时一个长事务包裹全部 HTTP 调用
     * （连接长时间占用、批量回滚）。重试路径本身按单号幂等（分账单号 / 回退单号确定性生成），
     * 即使极端情况下重复调用也不会产生重复资金动作。</p>
     */
    @Scheduled(fixedDelayString = "${wechat-split.split-failure-poll-delay-ms:120000}")
    @SchedulerLock(name = "split_failure_retry", lockAtMostFor = "PT10M", lockAtLeastFor = "PT0S")
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
