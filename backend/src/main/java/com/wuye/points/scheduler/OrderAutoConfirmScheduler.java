package com.wuye.points.scheduler;

import com.wuye.points.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超期自动确认收货定时任务。
 *
 * <p>买家长期不点「确认收货」时，{@code delivering} 订单会永久停留，导致商家货款、楼长分成、
 * 四级分账永不结算，而骑手配送费已先行入账。本任务每小时扫描「已送达且超过售后窗口期」的订单，
 * 自动置 {@code completed} 并执行结算，保证账目闭环。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoConfirmScheduler {

    private final DeliveryService deliveryService;

    @Scheduled(fixedDelay = 3600000) // 1 小时
    @SchedulerLock(name = "order_auto_confirm", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void autoConfirmDeliveredOrders() {
        try {
            deliveryService.autoConfirmDeliveredOrders();
        } catch (Exception e) {
            log.error("[订单] 超期自动确认收货调度异常", e);
        }
    }
}