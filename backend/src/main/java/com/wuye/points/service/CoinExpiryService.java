package com.wuye.points.service;

import com.wuye.points.entity.CoinExpiryRecord;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.CoinExpiryRecordRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PointPoolRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物业币过期清零核心逻辑（自动任务与管理端手动执行共用）。
 *
 * <p>关键修复：每条 earn/recharge 流水新增 {@code expired_amount}，过期时只扣「未过期部分」，
 * 同一笔流水不会被后续任务重复清零；个人池与家庭池分别按当前余额封顶处理。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoinExpiryService {

    private final PropertyCoinRecordRepository propertyCoinRecordRepository;
    private final CoinExpiryRecordRepository coinExpiryRecordRepository;
    private final ResidentRepository residentRepository;
    private final FamilyRepository familyRepository;
    private final PointPoolRepository pointPoolRepository;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * @param residentIds 为空表示该物业公司全部住户；非空表示仅处理指定住户（家庭池按家庭成员展开）
     * @param dryRun      只试算，不写库、不入池
     */
    @Transactional
    public ExpiryResult expireForCompany(PropertyCompany company,
                                         LocalDate expiryDate,
                                         List<String> residentIds,
                                         boolean dryRun,
                                         String operatorId) {
        Integer expiryDays = company.getCoinExpiryDays();
        if (expiryDays == null || expiryDays <= 0) {
            return ExpiryResult.empty();
        }
        LocalDateTime before = expiryDate.minusDays(expiryDays).atStartOfDay();

        List<Resident> residents;
        if (residentIds != null && !residentIds.isEmpty()) {
            residents = residentRepository.findAllById(residentIds).stream()
                    .filter(r -> company.getId().equals(r.getPropertyCompanyId()))
                    .toList();
        } else {
            residents = residentRepository.findByPropertyCompanyId(company.getId());
        }

        Map<String, List<Resident>> familyMembers = residents.stream()
                .filter(r -> StringUtils.hasText(r.getFamilyId()))
                .collect(Collectors.groupingBy(Resident::getFamilyId));
        List<Resident> personalResidents = residents.stream()
                .filter(r -> !StringUtils.hasText(r.getFamilyId()))
                .toList();

        BigDecimal totalExpired = BigDecimal.ZERO;
        int processedCount = 0;

        // 1) 个人池
        for (Resident snapshot : personalResidents) {
            Resident resident = dryRun
                    ? residentRepository.findById(snapshot.getId()).orElse(null)
                    : residentRepository.findByIdForUpdate(snapshot.getId()).orElse(null);
            if (resident == null) {
                continue;
            }
            BigDecimal balance = defaultDecimal(resident.getCoinBalance());
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            List<PropertyCoinRecord> records = propertyCoinRecordRepository
                    .findExpirablePersonal(resident.getId(), before);
            BigDecimal expired = expireRecords(records, balance, dryRun);
            if (expired.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            processedCount++;
            totalExpired = totalExpired.add(expired);
            if (!dryRun) {
                BigDecimal newBalance = balance.subtract(expired);
                resident.setCoinBalance(newBalance);
                residentRepository.save(resident);
                writeExpiryRecords(resident.getId(), null, company.getId(), expired, newBalance, expiryDate, operatorId);
            }
        }

        // 2) 家庭池：家庭流水 + 成员入家庭前的个人流水
        for (Map.Entry<String, List<Resident>> entry : familyMembers.entrySet()) {
            String familyId = entry.getKey();
            List<Resident> members = entry.getValue();
            List<String> memberIds = members.stream().map(Resident::getId).toList();
            if (memberIds.isEmpty()) {
                continue;
            }
            Family family = dryRun
                    ? familyRepository.findById(familyId).orElse(null)
                    : familyRepository.findByIdForUpdate(familyId).orElse(null);
            if (family == null || !company.getId().equals(family.getPropertyCompanyId())) {
                continue;
            }
            BigDecimal balance = defaultDecimal(family.getCoinBalance());
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            List<PropertyCoinRecord> records = propertyCoinRecordRepository
                    .findExpirableFamily(familyId, memberIds, before);
            BigDecimal expired = expireRecords(records, balance, dryRun);
            if (expired.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            processedCount++;
            totalExpired = totalExpired.add(expired);
            if (!dryRun) {
                BigDecimal newBalance = balance.subtract(expired);
                family.setCoinBalance(newBalance);
                familyRepository.save(family);
                writeExpiryRecords(family.getOwnerId(), familyId, company.getId(),
                        expired, newBalance, expiryDate, operatorId);
            }
        }

        if (!dryRun && totalExpired.compareTo(BigDecimal.ZERO) > 0) {
            addToPointPool(company.getId(), totalExpired);
        }

        if (processedCount > 0) {
            log.info("[物业币过期] companyId={} dryRun={} processed={} expired={}",
                    company.getId(), dryRun, processedCount, totalExpired);
        }
        return new ExpiryResult(processedCount, totalExpired,
                dryRun ? BigDecimal.ZERO : totalExpired);
    }

    /**
     * 按最早流水逐笔过期，每笔最多扣其「未过期金额」，且总额不超过当前余额。
     * dryRun 只计算，不修改实体的 expiredAmount。
     */
    private BigDecimal expireRecords(List<PropertyCoinRecord> records, BigDecimal balance, boolean dryRun) {
        BigDecimal remaining = balance;
        BigDecimal total = BigDecimal.ZERO;
        for (PropertyCoinRecord record : records) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal amount = defaultDecimal(record.getAmount());
            BigDecimal alreadyExpired = defaultDecimal(record.getExpiredAmount());
            BigDecimal unexpired = amount.subtract(alreadyExpired);
            if (unexpired.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal expire = unexpired.min(remaining);
            if (expire.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (!dryRun) {
                record.setExpiredAmount(alreadyExpired.add(expire));
                propertyCoinRecordRepository.save(record);
            }
            remaining = remaining.subtract(expire);
            total = total.add(expire);
        }
        return total;
    }

    private void writeExpiryRecords(String residentId,
                                    String familyId,
                                    String propertyCompanyId,
                                    BigDecimal expired,
                                    BigDecimal balanceAfter,
                                    LocalDate expiryDate,
                                    String operatorId) {
        PropertyCoinRecord expireRecord = new PropertyCoinRecord();
        expireRecord.setId(idGenerator.generateId("coin"));
        expireRecord.setResidentId(residentId);
        expireRecord.setFamilyId(familyId);
        expireRecord.setPropertyCompanyId(propertyCompanyId);
        expireRecord.setCoinType("expire");
        expireRecord.setAmount(expired.negate());
        expireRecord.setBalance(balanceAfter);
        expireRecord.setFamilyBalance(familyId == null ? null : balanceAfter);
        expireRecord.setExpiredAmount(BigDecimal.ZERO);
        expireRecord.setSource("coin_expired");
        expireRecord.setDescription("物业币过期自动清零");
        expireRecord.setOperatorId(operatorId);
        propertyCoinRecordRepository.save(expireRecord);

        CoinExpiryRecord expiryRecord = new CoinExpiryRecord();
        expiryRecord.setId(idGenerator.generateId("cer"));
        expiryRecord.setResidentId(residentId);
        expiryRecord.setPropertyCompanyId(propertyCompanyId);
        expiryRecord.setExpiredAmount(expired);
        expiryRecord.setPoolInAmount(expired);
        expiryRecord.setExpiredAt(expiryDate.atStartOfDay());
        coinExpiryRecordRepository.save(expiryRecord);
    }

    private void addToPointPool(String propertyCompanyId, BigDecimal expired) {
        PointPool pool = pointPoolRepository.findByPropertyCompanyIdForUpdate(propertyCompanyId)
                .orElseGet(() -> {
                    PointPool created = new PointPool();
                    created.setId(idGenerator.generateId("pp"));
                    created.setPropertyCompanyId(propertyCompanyId);
                    created.setBalance(BigDecimal.ZERO);
                    created.setTotalIn(BigDecimal.ZERO);
                    created.setTotalOut(BigDecimal.ZERO);
                    return created;
                });
        pool.setBalance(defaultDecimal(pool.getBalance()).add(expired));
        pool.setTotalIn(defaultDecimal(pool.getTotalIn()).add(expired));
        pointPoolRepository.save(pool);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record ExpiryResult(int processedCount, BigDecimal totalExpired, BigDecimal clearedToPool) {
        public static ExpiryResult empty() {
            return new ExpiryResult(0, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }
}
