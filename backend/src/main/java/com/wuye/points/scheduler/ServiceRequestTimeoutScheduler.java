package com.wuye.points.scheduler;

import com.wuye.points.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 服务需求商家响应超时轮询
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceRequestTimeoutScheduler {

    private final ServiceRequestService serviceRequestService;

    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(name = "service_request_timeout", lockAtMostFor = "PT5M", lockAtLeastFor = "PT0S")
    public void processMerchantTimeouts() {
        int count = serviceRequestService.processTimeouts();
        if (count > 0) {
            log.info("服务需求超时轮询完成，共处理 {} 单", count);
        }
    }
}
