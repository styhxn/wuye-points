package com.wuye.points.scheduler;

import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.service.CoinExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 物业币过期自动清零定时任务。
 *
 * <p>实际过期逻辑在 {@link CoinExpiryService}：按流水 {@code expired_amount} 去重，
 * 同一笔 earn/recharge 不会被后续任务重复扣减；个人池与家庭池分别处理。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoinExpiryAutoScheduler {

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final CoinExpiryService coinExpiryService;

    @Scheduled(cron = "0 0 3 * * ?")
    @SchedulerLock(name = "coin_expiry_auto", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void processCoinExpiry() {
        LocalDate today = LocalDate.now();
        List<PropertyCompany> companies = propertyCompanyRepository.findAll();
        int totalProcessed = 0;
        for (PropertyCompany company : companies) {
            try {
                CoinExpiryService.ExpiryResult result =
                        coinExpiryService.expireForCompany(company, today, null, false, "system");
                totalProcessed += result.processedCount();
            } catch (Exception e) {
                log.error("[物业币过期] 处理失败 companyId={}", company.getId(), e);
            }
        }
        if (totalProcessed > 0) {
            log.info("[物业币过期] 自动任务完成，共处理 {} 个账户", totalProcessed);
        }
    }
}
