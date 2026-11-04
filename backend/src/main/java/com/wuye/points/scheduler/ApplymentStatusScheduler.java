package com.wuye.points.scheduler;

import com.wuye.points.entity.MerchantApplyment;
import com.wuye.points.repository.MerchantApplymentRepository;
import com.wuye.points.service.MerchantApplymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 进件状态推进调度（服务商特约商户进件）。
 *
 * <p>定时扫描非终态申请单（submitted/auditing/legal_validating/signing），
 * 调微信查询进件状态：审核通过 → 回填 sub_mchid 并激活商家收款；待签约 → 下发签约链接；
 * 驳回 → 记录原因待改资料重提。单次查询失败仅记 lastError + retryCount 递增，下一周期自动重试。</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplymentStatusScheduler {

    private final MerchantApplymentRepository applymentRepository;
    private final MerchantApplymentService applymentService;

    @Scheduled(cron = "0 10 1 * * ?")
    @SchedulerLock(name = "partner_applyment_status_sync", lockAtMostFor = "PT2H", lockAtLeastFor = "PT1M")
    public void syncApplymentStatus() {
        if (!applymentService.isApplymentReady()) {
            return;
        }
        List<MerchantApplyment> active = applymentRepository
                .findByApplymentStateIn(MerchantApplymentService.ACTIVE_STATES);
        for (MerchantApplyment applyment : active) {
            try {
                applymentService.refreshApplymentStatus(applyment);
            } catch (Exception e) {
                log.error("[进件] 状态推进异常 applymentId={}", applyment.getId(), e);
            }
        }
    }
}
