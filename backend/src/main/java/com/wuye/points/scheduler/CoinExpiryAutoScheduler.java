package com.wuye.points.scheduler;

import com.wuye.points.entity.CoinExpiryRecord;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.CoinExpiryRecordRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物业币过期自动清零定时任务
 * 根据物业公司设定的有效期天数，自动清零过期物业币并入积分池
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoinExpiryAutoScheduler {

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCoinRecordRepository propertyCoinRecordRepository;
    private final CoinExpiryRecordRepository coinExpiryRecordRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void processCoinExpiry() {
        LocalDate today = LocalDate.now();
        int totalProcessed = 0;

        List<PropertyCompany> companies = propertyCompanyRepository.findAll();
        for (PropertyCompany company : companies) {
            Integer expiryDays = company.getCoinExpiryDays();
            if (expiryDays == null || expiryDays <= 0) {
                continue;
            }

            LocalDate issueDeadline = today.minusDays(expiryDays);
            List<Resident> residents = residentRepository.findByPropertyCompanyId(company.getId());

            for (Resident resident : residents) {
                BigDecimal balance = resident.getCoinBalance() != null
                        ? resident.getCoinBalance() : BigDecimal.ZERO;
                if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                List<PropertyCoinRecord> earnRecords = propertyCoinRecordRepository
                        .findByResidentIdAndCoinTypeAndCreatedAtBefore(
                                resident.getId(), "earn", issueDeadline.atStartOfDay());

                List<PropertyCoinRecord> rechargeRecords = propertyCoinRecordRepository
                        .findByResidentIdAndCoinTypeAndCreatedAtBefore(
                                resident.getId(), "recharge", issueDeadline.atStartOfDay());

                BigDecimal expiredFromEarn = earnRecords.stream()
                        .map(PropertyCoinRecord::getAmount)
                        .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal expiredFromRecharge = rechargeRecords.stream()
                        .map(PropertyCoinRecord::getAmount)
                        .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalExpired = expiredFromEarn.add(expiredFromRecharge);
                if (totalExpired.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal actualExpired = totalExpired.min(balance);
                BigDecimal newBalance = balance.subtract(actualExpired);
                resident.setCoinBalance(newBalance);
                residentRepository.save(resident);

                PropertyCoinRecord record = new PropertyCoinRecord();
                record.setId(idGenerator.generateId("coin"));
                record.setResidentId(resident.getId());
                record.setFamilyId(resident.getFamilyId());
                record.setPropertyCompanyId(resident.getPropertyCompanyId());
                record.setCoinType("expire");
                record.setAmount(actualExpired.negate());
                record.setBalance(newBalance);
                record.setSource("coin_expired");
                record.setDescription("物业币过期自动清零");
                record.setOperatorId("system");
                propertyCoinRecordRepository.save(record);

                CoinExpiryRecord expiryRecord = new CoinExpiryRecord();
                expiryRecord.setId(idGenerator.generateId("cer"));
                expiryRecord.setResidentId(resident.getId());
                expiryRecord.setPropertyCompanyId(company.getId());
                expiryRecord.setExpiredAmount(actualExpired);
                expiryRecord.setPoolInAmount(actualExpired);
                expiryRecord.setExpiredAt(today.atStartOfDay());
                coinExpiryRecordRepository.save(expiryRecord);

                totalProcessed++;
            }
        }

        if (totalProcessed > 0) {
            log.info("物业币过期自动清零任务完成，共处理 {} 个用户", totalProcessed);
        }
    }
}
