package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.coinexpiry.CoinExpiryRecordItemResponse;
import com.wuye.points.dto.admin.coinexpiry.RunCoinExpiryRequest;
import com.wuye.points.dto.admin.coinexpiry.RunCoinExpiryResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.CoinExpiryRecord;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoinExpiryRecordRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCoinExpiryService {

    private final CoinExpiryRecordRepository coinExpiryRecordRepository;
    private final PropertyCoinRecordRepository propertyCoinRecordRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ResidentRepository residentRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final OperationLogService operationLogService;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public RunCoinExpiryResponse run(RunCoinExpiryRequest request) {
        permissionGuard.requirePermissionWithFallback("coin.expiry_run", "points.input", "data.view");

        LocalDate expiryDate = adminSupport.parseDate(request.getExpiryDate());
        if (expiryDate == null) {
            throw new BizException(ErrorCode.PARAM_MISSING, "expiryDate不能为空");
        }

        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(request.getPropertyCompanyId());
        if (!StringUtils.hasText(scopedPropertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }

        PropertyCompany company = propertyCompanyRepository.findById(scopedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        Integer expiryDays = company.getCoinExpiryDays();
        if (expiryDays == null || expiryDays <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该物业公司未设置物业币有效期");
        }

        LocalDate issueDeadline = expiryDate.minusDays(expiryDays);

        List<Resident> residents;
        if (request.getResidentIds() != null && !request.getResidentIds().isEmpty()) {
            Set<String> residentIds = new HashSet<>(request.getResidentIds());
            residents = residentRepository.findAllById(residentIds).stream()
                    .filter(r -> scopedPropertyCompanyId.equals(r.getPropertyCompanyId()))
                    .toList();
        } else {
            residents = residentRepository.findByPropertyCompanyId(scopedPropertyCompanyId);
        }

        BigDecimal totalExpiredAmount = BigDecimal.ZERO;
        BigDecimal clearedToPool = BigDecimal.ZERO;
        int processedCount = 0;
        String operatorId = UserContext.currentUserId();

        for (Resident resident : residents) {
            BigDecimal balance = defaultCoinBalance(resident);
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
            processedCount++;
            totalExpiredAmount = totalExpiredAmount.add(actualExpired);
            clearedToPool = clearedToPool.add(actualExpired);

            if (!dryRun) {
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
                record.setSourceId(null);
                record.setDescription("物业币过期清零");
                record.setOperatorId(operatorId);
                propertyCoinRecordRepository.save(record);

                CoinExpiryRecord expiryRecord = new CoinExpiryRecord();
                expiryRecord.setId(idGenerator.generateId("cer"));
                expiryRecord.setResidentId(resident.getId());
                expiryRecord.setPropertyCompanyId(scopedPropertyCompanyId);
                expiryRecord.setExpiredAmount(actualExpired);
                expiryRecord.setPoolInAmount(actualExpired);
                expiryRecord.setExpiredAt(expiryDate.atStartOfDay());
                coinExpiryRecordRepository.save(expiryRecord);
            }
        }

        if (!dryRun) {
            operationLogService.log("coin_expiry", "run", "coin_expiry_task",
                    idGenerator.generateId("task"), null,
                    Map.of("expiryDate", request.getExpiryDate(),
                            "processedCount", processedCount,
                            "totalExpiredAmount", totalExpiredAmount));
        }

        return RunCoinExpiryResponse.builder()
                .taskId(idGenerator.generateId("task"))
                .expiryDate(expiryDate.toString())
                .dryRun(dryRun)
                .processedCount(processedCount)
                .totalExpiredAmount(totalExpiredAmount)
                .clearedToPool(clearedToPool)
                .status("completed")
                .executedAt(DateTimeUtil.nowIsoUtc())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<CoinExpiryRecordItemResponse> listRecords(
            int page,
            int pageSize,
            String residentId,
            String startDate,
            String endDate,
            String propertyCompanyId) {
        permissionGuard.requirePermissionWithFallback("coin.expiry_run", "points.input", "data.view");

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Specification<CoinExpiryRecord> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (StringUtils.hasText(scopedPropertyCompanyId)) {
                predicates = cb.and(predicates, cb.equal(root.get("propertyCompanyId"), scopedPropertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates = cb.and(predicates, cb.equal(root.get("residentId"), residentId));
            }
            if (start != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("expiredAt"), start));
            }
            if (end != null) {
                predicates = cb.and(predicates, cb.lessThan(root.get("expiredAt"), end));
            }
            return predicates;
        };

        Page<CoinExpiryRecord> recordPage = coinExpiryRecordRepository.findAll(spec, pageable);

        Map<String, String> residentNames = resolveResidentNames(recordPage.getContent());

        List<CoinExpiryRecordItemResponse> list = recordPage.getContent().stream()
                .map(record -> CoinExpiryRecordItemResponse.builder()
                        .id(record.getId())
                        .residentId(record.getResidentId())
                        .residentName(residentNames.get(record.getResidentId()))
                        .propertyCompanyId(record.getPropertyCompanyId())
                        .expiredAmount(record.getExpiredAmount())
                        .poolInAmount(record.getPoolInAmount())
                        .expiredAt(DateTimeUtil.toIsoUtc(record.getExpiredAt()))
                        .build())
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    private Map<String, String> resolveResidentNames(List<CoinExpiryRecord> records) {
        Set<String> residentIds = records.stream()
                .map(CoinExpiryRecord::getResidentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (residentIds.isEmpty()) {
            return Map.of();
        }
        Map<String, String> names = new HashMap<>();
        residentRepository.findAllById(residentIds).forEach(resident ->
                names.put(resident.getId(), resident.getName()));
        return names;
    }

    private BigDecimal defaultCoinBalance(Resident resident) {
        return resident.getCoinBalance() != null ? resident.getCoinBalance() : BigDecimal.ZERO;
    }

    private LocalDateTime parseStartDate(String value) {
        LocalDate date = adminSupport.parseDate(value);
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime parseEndDate(String value) {
        LocalDate date = adminSupport.parseDate(value);
        return date != null ? date.plusDays(1).atStartOfDay() : null;
    }
}
