package com.wuye.points.scheduler;

import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分账修复调度：为「订单已完成、支付成功、有分成记录，但分账记录缺失或不完整」的订单补跑分账。
 *
 * <p><b>覆盖的场景</b>：冻结循环在逐接收方落库前发生硬故障（JVM 崩溃 / 数据库瞬时异常），
 * 导致部分接收方没有分账记录——这些订单既不在 {@code skipped}（无记录）也不在
 * {@code freeze_failed}（无记录），现有补偿调度都扫不到，分成将永久不到账。</p>
 *
 * <p><b>为什么可以安全重跑</b>：{@code WechatSplitService#freezeOnOrderCompleted} 逐接收方判重
 * （同订单同主体已存在记录则跳过），且分账单号按「订单 + 主体」确定性生成——微信侧同分账单号
 * 多次请求等同一次，重跑不会重复出钱。</p>
 *
 * <p><b>窗口约束</b>：微信分账有可分账期限（普通服务商分账约支付后 30 天），修复只扫描
 * 最近 {@link #REPAIR_WINDOW_DAYS} 天的已支付订单，避免对超期订单发起必然失败的请求。</p>
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SplitRepairScheduler {

    /** 修复扫描窗口（天）：保守取 20 天，留在微信可分账期限（约 30 天）内 */
    private static final int REPAIR_WINDOW_DAYS = 20;

    private final PaymentRecordRepository paymentRecordRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final WechatSplitService wechatSplitService;

    @Scheduled(cron = "0 40 3 * * ?")
    @SchedulerLock(name = "split_repair_missing_records", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void repairMissingSplitRecords() {
        if (!wechatSplitService.isEnabled()) {
            return;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(REPAIR_WINDOW_DAYS);
        List<PaymentRecord> paidPayments = paymentRecordRepository
                .findByBizTypeAndStatusAndCreatedAtAfter(PaymentBizType.ORDER, "paid", cutoff);
        int scanned = 0;
        int repaired = 0;
        for (PaymentRecord payment : paidPayments) {
            String orderId = payment.getBizId();
            // 无分成记录 = 业务上本就不分账（如商家无物业配置），跳过
            if (distributionRecordRepository.findFirstByOrderId(orderId).isEmpty()) {
                continue;
            }
            scanned++;
            try {
                wechatSplitService.freezeOnOrderCompleted(orderId);
                repaired++;
            } catch (Exception e) {
                log.warn("[分账修复] 补跑分账异常 orderId={}: {}", orderId, e.getMessage());
            }
        }
        if (scanned > 0) {
            log.info("[分账修复] 补跑完成：窗口 {} 天内已支付订单 {} 笔，补跑 {} 笔（幂等，无缺失时为 0 变更）",
                    REPAIR_WINDOW_DAYS, scanned, repaired);
        }
    }
}
