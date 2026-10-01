package com.wuye.points.scheduler;

import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 退款状态轮询调度（普通支付 4.9）。
 *
 * <p>普通支付 4.6 退款申请实时返回、<b>无异步通知</b>，故对处于 refunding 的流水定时轮询，
 * 落地最终状态（refunded / refund_failed）；配合日终对账单兜底，避免退款状态丢失。
 *
 * <p>微信 未启用（开发模式）时不轮询。@EnableScheduling 本类即开启调度（项目此前无调度开关）。
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class RefundQueryScheduler {

    private static final String STATUS_REFUNDING = "refunding";

    private final PaymentRecordRepository paymentRecordRepository;
    private final PayChannel wechatPayService;

    @Scheduled(fixedDelay = 60_000)
    @SchedulerLock(name = "refund_query_poll", lockAtMostFor = "PT5M", lockAtLeastFor = "PT0S")
    public void pollRefunding() {
        if (!wechatPayService.isEnabled()) {
            return;
        }
        List<PaymentRecord> refunding = paymentRecordRepository.findByStatus(STATUS_REFUNDING);
        for (PaymentRecord record : refunding) {
            try {
                String state = wechatPayService.queryRefund(record);
                if ("SUCCESS".equals(state)) {
                    record.setStatus("refunded");
                } else if ("FAIL".equals(state)) {
                    record.setStatus("refund_failed");
                } else {
                    continue; // 退款中，下一轮再查
                }
                paymentRecordRepository.save(record);
            } catch (Exception e) {
                log.error("[退款轮询] 失败 recordId={}", record.getId(), e);
            }
        }
    }
}
