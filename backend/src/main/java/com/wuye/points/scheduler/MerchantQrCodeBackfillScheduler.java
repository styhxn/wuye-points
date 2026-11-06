package com.wuye.points.scheduler;

import com.wuye.points.service.MerchantQrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 商家专属收款二维码补生成。
 *
 * <p>两端都还没有「生成收款码」按钮（前端只读），而 PRD P0-05 要求「商家入驻与专属二维码」可验收，
 * 因此启动后补一次、之后每天凌晨补一次；已有码的店铺不会被重复打微信接口。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantQrCodeBackfillScheduler {

    private final MerchantQrCodeService merchantQrCodeService;

    /** dev 环境 skip-api=true 时微信接口不通，直接跳过，避免整点刷失败日志。 */
    @Value("${app.wechat.skip-api:false}")
    private boolean skipApi;

    @Value("${app.qr-code.auto-generate:true}")
    private boolean autoGenerate;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 25 4 * * ?")
    @SchedulerLock(name = "merchant_qrcode_backfill", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void backfillMissingQrCodes() {
        if (!autoGenerate || skipApi) {
            log.info("[收款码] 跳过自动补生成: autoGenerate={}, skipApi={}", autoGenerate, skipApi);
            return;
        }
        int generated = merchantQrCodeService.backfillMissing();
        if (generated > 0) {
            log.info("[收款码] 本次补生成 {} 个商家专属二维码", generated);
        }
    }
}
