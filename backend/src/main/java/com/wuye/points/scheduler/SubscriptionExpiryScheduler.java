package com.wuye.points.scheduler;

import com.wuye.points.entity.ActivityGroupSubscription;
import com.wuye.points.entity.ServiceSubscription;
import com.wuye.points.repository.ActivityGroupSubscriptionRepository;
import com.wuye.points.repository.ServiceSubscriptionRepository;
import com.wuye.points.service.ActivityGroupService;
import com.wuye.points.service.CommunityServiceService;
import com.wuye.points.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 订阅到期提醒定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final ServiceSubscriptionRepository serviceSubscriptionRepository;
    private final ActivityGroupSubscriptionRepository activityGroupSubscriptionRepository;
    private final WebSocketService webSocketService;
    private final CommunityServiceService communityServiceService;
    private final ActivityGroupService activityGroupService;

    /**
     * 到期状态落库兜底。列表接口里的 refreshSubscriptionStatus 处于 readOnly 事务，改了不入库，
     * 所以过期行会长期停在 active；取消订阅时同键命中多行会直接 500。启动后先跑一次，之后每天 03:30。
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 30 3 * * ?")
    public void expireOverdueSubscriptions() {
        try {
            int expired = communityServiceService.expireOverdueSubscriptions()
                    + activityGroupService.expireOverdueSubscriptions();
            if (expired > 0) {
                log.info("Subscription expiry sweep done. expired={}", expired);
            }
        } catch (Exception ex) {
            log.error("Subscription expiry sweep failed", ex);
        }
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkExpiringSubscriptions() {
        LocalDate now = LocalDate.now();
        LocalDate warningDate = now.plusDays(7);

        List<ServiceSubscription> expiringServiceSubs = serviceSubscriptionRepository
                .findByEndDateBetweenAndStatus(now, warningDate, "active");

        for (ServiceSubscription sub : expiringServiceSubs) {
            webSocketService.sendNotification(sub.getResidentId(), null, "subscription_expiry",
                    "您购买的服务即将到期，请及时续费");
        }

        List<ActivityGroupSubscription> expiringGroupSubs = activityGroupSubscriptionRepository
                .findByEndDateBetweenAndStatus(now, warningDate, "active");

        for (ActivityGroupSubscription sub : expiringGroupSubs) {
            webSocketService.sendNotification(sub.getResidentId(), null, "subscription_expiry",
                    "您参与的活动组即将到期，请及时续费");
        }

        log.info("Subscription expiry check completed. Service: {}, Group: {}",
                expiringServiceSubs.size(), expiringGroupSubs.size());
    }
}
