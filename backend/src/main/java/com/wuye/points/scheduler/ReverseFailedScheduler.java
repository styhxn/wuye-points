package com.wuye.points.scheduler;

import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.WechatSplitRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 分账退回补偿调度（微信 6.9 分账退回）。
 *
 * <p>普通支付退款 / 订单取消时，物业分成通过 6.9 allocateRefund 从物业退回给商户。
 * 当物业余额不足时首刷会落 {@code reverse_failed}；本调度定时重试这类记录，
 * 把冻结的分成资金重新退回，避免「买家已退、物业仍占款」的资损滞留。
 * 物业回款后下次轮询即可成功，无需人工干预。
 *
 * <p>微信 未启用（开发模式）时不执行。@EnableScheduling 本类即开启调度。
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReverseFailedScheduler {

    private static final String STATUS_REVERSE_FAILED = "reverse_failed";

    private final WechatSplitRecordRepository wechatSplitRecordRepository;
    private final WechatSplitService wechatSplitService;

    @Scheduled(fixedDelay = 30 * 60_000)
    @SchedulerLock(name = "split_reverse_failed_retry", lockAtMostFor = "PT25M", lockAtLeastFor = "PT1M")
    public void pollReverseFailed() {
        if (!wechatSplitService.isEnabled()) {
            return;
        }
        List<WechatSplitRecord> failed = wechatSplitRecordRepository.findByStatus(STATUS_REVERSE_FAILED);
        for (WechatSplitRecord split : failed) {
            try {
                wechatSplitService.retryReverse(split);
            } catch (Exception e) {
                log.error("[分账退回补偿] 重试失败 splitNo={}", split.getSplitNo(), e);
            }
        }
    }
}
