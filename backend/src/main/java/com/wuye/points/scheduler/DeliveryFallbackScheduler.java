package com.wuye.points.scheduler;

import com.wuye.points.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 配送超时物业兜底定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryFallbackScheduler {

    private final DeliveryService deliveryService;

    @Scheduled(fixedDelay = 60000)
    public void processPropertyFallback() {
        int count = deliveryService.processPropertyFallback();
        if (count > 0) {
            log.info("配送物业兜底处理完成，共 {} 单", count);
        }
    }
}
