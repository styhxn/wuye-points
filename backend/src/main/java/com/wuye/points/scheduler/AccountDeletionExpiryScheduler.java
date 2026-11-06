package com.wuye.points.scheduler;

import com.wuye.points.service.ResidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 账号注销冷静期到期处理（整改清单条目9）。
 *
 * <p>申请注销只把到期时间写进 {@code residents.deletion_expires_at}，此前没有任何
 * 消费方，账号会永久停在 {@code pending_deletion}。本调度器每 10 分钟扫一遍，
 * 到期的按「已注销」落地（状态、软删时间、令牌版本），多实例由 ShedLock 互斥。</p>
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionExpiryScheduler {

    private final ResidentService residentService;

    @Scheduled(fixedRate = 600000) // 10 分钟
    @SchedulerLock(name = "account_deletion_expiry", lockAtMostFor = "PT9M", lockAtLeastFor = "PT0S")
    public void finalizeExpiredDeletions() {
        try {
            int deleted = residentService.finalizeExpiredDeletions();
            if (deleted > 0) {
                log.info("[账号注销] 冷静期到期自动注销 {} 个账号", deleted);
            }
        } catch (Exception e) {
            log.error("[账号注销] 冷静期到期处理调度异常", e);
        }
    }
}
