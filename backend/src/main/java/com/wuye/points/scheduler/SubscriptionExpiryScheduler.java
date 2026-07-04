package com.wuye.points.scheduler;

import com.wuye.points.entity.ActivityGroupSubscription;
import com.wuye.points.entity.ServiceSubscription;
import com.wuye.points.repository.ActivityGroupSubscriptionRepository;
import com.wuye.points.repository.ServiceSubscriptionRepository;
import com.wuye.points.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
