package com.wuye.points.scheduler;

import com.wuye.points.service.MerchantVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 商家核实超时 + 冻结到期结算调度器。
 *
 * <p>两个定时任务：
 * <ul>
 *   <li>每 10 分钟扫描超过核实时限的 pending_verification 订单，自动视为通过（进入冻结与分账结算）</li>
 *   <li>每 1 小时扫描冻结期满的 verified 订单，执行分账结算</li>
 * </ul>
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class MerchantVerificationScheduler {

    private final MerchantVerificationService merchantVerificationService;

    /**
     * 商家核实超时自动通过：每 10 分钟执行一次。
     */
    @Scheduled(fixedRate = 600000) // 10 分钟
    @SchedulerLock(name = "merchant_verification_auto_approve", lockAtMostFor = "PT9M", lockAtLeastFor = "PT0S")
    public void autoApproveExpiredVerifications() {
        try {
            int approved = merchantVerificationService.autoApproveExpiredVerifications();
            if (approved > 0) {
                log.info("[商家核实] 超时自动通过订单 {} 笔", approved);
            }
        } catch (Exception e) {
            log.error("[商家核实] 超时自动通过调度异常", e);
        }
    }

    /**
     * 冻结到期自动结算：每 1 小时执行一次。
     */
    @Scheduled(fixedRate = 3600000) // 1 小时
    @SchedulerLock(name = "merchant_verification_settle_frozen", lockAtMostFor = "PT50M", lockAtLeastFor = "PT1M")
    public void settleFrozenOrders() {
        try {
            int settled = merchantVerificationService.settleFrozenOrders();
            if (settled > 0) {
                log.info("[冻结结算] 冻结到期结算完成 {} 笔", settled);
            }
        } catch (Exception e) {
            log.error("[冻结结算] 冻结到期结算调度异常", e);
        }
    }
}
