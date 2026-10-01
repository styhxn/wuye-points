package com.wuye.points.scheduler;

import com.wuye.points.entity.Merchant;
import com.wuye.points.service.PlatformSettingService;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.CoinWithdrawalRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 自动提现定时任务（A8）
 * 每日凌晨执行，查询 auto_withdrawal_enabled=true 的物业公司，
 * 按周期检查是否到提现日，自动为 withdrawal_blocked=false 的商家创建提现申请。
 *
 * 注意：批处理本身不再持有事务，每个商家的提现由 AutoWithdrawalExecutor
 * 在独立的 REQUIRES_NEW 事务中执行，单商家失败仅回滚自身。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoWithdrawalScheduler {

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final CoinWithdrawalRepository coinWithdrawalRepository;
    private final AutoWithdrawalExecutor autoWithdrawalExecutor;
    private final PlatformSettingService platformSetting;

    @Scheduled(cron = "0 0 4 * * ?")
    @SchedulerLock(name = "auto_withdrawal_daily", lockAtMostFor = "PT3H", lockAtLeastFor = "PT5M")
    public void processAutoWithdrawal() {
        if (!platformSetting.isAutoWithdrawalEnabled()) {
            log.info("自动提现全局开关未启用，跳过本次调度");
            return;
        }
        LocalDate today = LocalDate.now();
        List<PropertyCompany> companies = propertyCompanyRepository.findByAutoWithdrawalEnabledTrue();
        if (companies.isEmpty()) {
            return;
        }

        int totalProcessed = 0;
        for (PropertyCompany company : companies) {
            Integer periodDays = company.getAutoWithdrawalPeriodDays();
            if (periodDays == null || periodDays <= 0) {
                periodDays = 1;
            }
            // 按周期判断今日是否为提现日
            if (!isWithdrawalDay(today, periodDays)) {
                continue;
            }

            BigDecimal feeRate = resolveFeeRate(company);
            List<Merchant> merchants = merchantRepository
                    .findByPropertyCompanyIdAndStatus(company.getId(), "approved");
            for (Merchant merchant : merchants) {
                if (Boolean.TRUE.equals(merchant.getWithdrawalBlocked())) {
                    continue;
                }
                if (coinWithdrawalRepository.existsByMerchantIdAndAuditStatus(
                        merchant.getId(), "pending")
                        || coinWithdrawalRepository.existsByMerchantIdAndAuditStatus(
                        merchant.getId(), "pending_audit")) {
                    continue;
                }

                Resident merchantUser = resolveMerchantUser(merchant);
                if (merchantUser == null) {
                    continue;
                }
                if (Boolean.TRUE.equals(merchantUser.getWithdrawalBlocked())) {
                    continue;
                }

                BigDecimal coinBalance = merchantUser.getCoinBalance() != null
                        ? merchantUser.getCoinBalance() : BigDecimal.ZERO;
                if (coinBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                try {
                    autoWithdrawalExecutor.createAutoWithdrawal(merchant, merchantUser, coinBalance, feeRate);
                    totalProcessed++;
                } catch (Exception ex) {
                    log.warn("自动提现失败 merchantId={} : {}", merchant.getId(), ex.getMessage());
                }
            }
        }

        if (totalProcessed > 0) {
            log.info("自动提现任务完成，共处理 {} 笔", totalProcessed);
        }
    }

    private boolean isWithdrawalDay(LocalDate today, int periodDays) {
        if (periodDays <= 1) {
            return true;
        }
        return today.getDayOfYear() % periodDays == 0;
    }

    private Resident resolveMerchantUser(Merchant merchant) {
        if (!StringUtils.hasText(merchant.getContactPhone())) {
            return null;
        }
        return residentRepository.findByPhone(merchant.getContactPhone()).orElse(null);
    }

    private BigDecimal resolveFeeRate(PropertyCompany company) {
        return platformSetting.resolveWithdrawalFeeRate(
                company.getWithdrawalFeeRate(), PlatformSettingService.FEE_CATEGORY_MERCHANT);
    }
}
