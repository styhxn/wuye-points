package com.wuye.points.scheduler;

import com.wuye.points.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 清理过期幂等记录（完成后保留 24h，processing 2min）。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyCleanupScheduler {

    private final IdempotencyService idempotencyService;

    @Scheduled(cron = "0 30 4 * * ?")
    @SchedulerLock(name = "idempotency_cleanup", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void cleanup() {
        idempotencyService.cleanup();
    }
}
