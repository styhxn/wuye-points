package com.wuye.points.scheduler;

import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.PointPoolRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 积分清零定时任务（连续2年不交物业费→积分清零进池）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointClearScheduler {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyFeeRepository propertyFeeRepository;
    private final ResidentRepository residentRepository;
    private final PointRecordRepository pointRecordRepository;
    private final PointPoolRepository pointPoolRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void processPointClear() {
        LocalDate today = LocalDate.now();
        LocalDate lookbackDate = today.minusYears(2);
        int totalProcessed = 0;

        List<PropertyCompany> companies = propertyCompanyRepository.findAll();
        for (PropertyCompany company : companies) {
            if (!Boolean.TRUE.equals(company.getTwoYearClearEnabled())) {
                continue;
            }

            Specification<PropertyFee> spec = (root, query, cb) -> {
                var predicates = cb.conjunction();
                predicates = cb.and(predicates, cb.equal(root.get("propertyCompanyId"), company.getId()));
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("dueDate"), lookbackDate));
                predicates = cb.and(predicates, cb.notEqual(root.get("paymentStatus"), "paid"));
                return predicates;
            };

            List<PropertyFee> overdueFees = propertyFeeRepository.findAll(spec);
            Set<String> eligibleResidentIds = overdueFees.stream()
                    .map(PropertyFee::getResidentId)
                    .filter(id -> id != null && !id.isEmpty())
                    .collect(Collectors.toSet());

            BigDecimal rate = resolvePointToFeeRate(company);

            for (String residentId : eligibleResidentIds) {
                Resident resident = residentRepository.findById(residentId).orElse(null);
                if (resident == null || !company.getId().equals(resident.getPropertyCompanyId())) {
                    continue;
                }
                int balance = resident.getPointBalance() != null ? resident.getPointBalance() : 0;
                if (balance <= 0) {
                    continue;
                }

                resident.setPointBalance(0);
                residentRepository.save(resident);

                PointRecord record = new PointRecord();
                record.setId(idGenerator.generateId("pt"));
                record.setResidentId(resident.getId());
                record.setFamilyId(resident.getFamilyId());
                record.setPropertyCompanyId(resident.getPropertyCompanyId());
                record.setPointType("expire");
                record.setAmount(-balance);
                record.setBalance(0);
                record.setSource("fee_clear");
                record.setDescription("2年欠费积分清零");
                record.setOperatorId("system");
                pointRecordRepository.save(record);

                BigDecimal poolAmount = BigDecimal.valueOf(balance)
                        .multiply(rate)
                        .setScale(2, RoundingMode.HALF_UP);
                addToPool(company.getId(), poolAmount, "fee_clear", record.getId(),
                        "2年欠费积分清零入池");

                totalProcessed++;
            }
        }

        if (totalProcessed > 0) {
            log.info("积分清零定时任务完成，共处理 {} 个用户", totalProcessed);
        }
    }

    private void addToPool(String propertyCompanyId, BigDecimal amount, String source,
                           String sourceId, String description) {
        PointPool pool = pointPoolRepository.findByPropertyCompanyId(propertyCompanyId)
                .orElse(null);
        if (pool == null) {
            pool = new PointPool();
            pool.setId(idGenerator.generateId("pp"));
            pool.setPropertyCompanyId(propertyCompanyId);
            pool.setBalance(BigDecimal.ZERO);
            pool.setTotalIn(BigDecimal.ZERO);
            pool.setTotalOut(BigDecimal.ZERO);
        }
        pool.setBalance(pool.getBalance().add(amount));
        pool.setTotalIn(pool.getTotalIn().add(amount));
        pointPoolRepository.save(pool);
    }

    private BigDecimal resolvePointToFeeRate(PropertyCompany company) {
        BigDecimal rate = company.getPointToFeeRate();
        if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
            return rate;
        }
        return DEFAULT_POINT_TO_FEE_RATE;
    }
}
