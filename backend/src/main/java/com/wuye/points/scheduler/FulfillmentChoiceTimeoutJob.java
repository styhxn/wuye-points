package com.wuye.points.scheduler;

import com.wuye.points.constant.FulfillmentConstants;
import com.wuye.points.entity.Order;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 履约方式选择超时兜底定时任务。
 *
 * <p>扫描仍处于 {@code pending_choice} 且已超过选择截止时间的订单，
 * 默认改派为平台配送（courier_hall），并写入审计日志（留痕）。
 * 每分钟执行一次；单条失败不影响其余订单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentChoiceTimeoutJob {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(name = "fulfillment_choice_timeout", lockAtMostFor = "PT5M", lockAtLeastFor = "PT0S")
    public void process() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> expired = orderRepository.findByFulfillmentModeAndFulfillmentDeadlineBefore(
                FulfillmentConstants.FULFILLMENT_MODE_PENDING_CHOICE, now);
        if (expired.isEmpty()) {
            return;
        }
        log.info("履约超时兜底扫描：发现 {} 个待处理订单", expired.size());
        for (Order order : expired) {
            try {
                orderService.autoResolvePendingChoice(order.getId());
            } catch (Exception e) {
                log.error("履约超时兜底处理失败 orderId={}", order.getId(), e);
            }
        }
    }
}
