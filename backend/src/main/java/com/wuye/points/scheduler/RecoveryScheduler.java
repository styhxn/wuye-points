package com.wuye.points.scheduler;

import com.wuye.points.entity.SplitRecoveryRecord;
import com.wuye.points.repository.SplitRecoveryRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 回退台账对账提示调度（阶段2）。
 *
 * <p>每日扫描待追回（pending）台账：存在未核销的垫付资金即告警，
 * 提醒运营线下对账追回（催收 / 后续配送费抵扣）。</p>
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class RecoveryScheduler {

    private final SplitRecoveryRecordRepository recoveryRepository;

    @Scheduled(cron = "0 0 4 * * ?")
    @SchedulerLock(name = "split_recovery_alert", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void alertPendingRecoveries() {
        List<SplitRecoveryRecord> pending = recoveryRepository.findByStatus("pending");
        if (pending.isEmpty()) {
            return;
        }
        log.warn("[回退台账] 存在 {} 笔平台垫付待追回，需线下对账处理，示例 id: {}",
                pending.size(),
                pending.stream().limit(20).map(SplitRecoveryRecord::getId).toList());
    }
}
