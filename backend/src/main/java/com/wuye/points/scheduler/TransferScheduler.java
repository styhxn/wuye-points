package com.wuye.points.scheduler;

import com.wuye.points.service.MerchantTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 内部结算转账调度（阶段2）。
 *
 * <p>周期性推进 {@code merchant_transfers}：pending 单校验收款账号并调微信商家转账出款；
 * processing 超时单查微信推进终态；失败超重试上限置 failed 待人工处理。</p>
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class TransferScheduler {

    private final MerchantTransferService transferService;

    @Scheduled(fixedDelay = 5 * 60_000)
    @SchedulerLock(name = "merchant_transfer_process", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void processTransfers() {
        try {
            transferService.processTransfers();
        } catch (Exception e) {
            log.error("[商家转账] 调度异常", e);
        }
    }
}
