package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.coin.CoinBalanceResponse;
import com.wuye.points.dto.coin.CoinRecordItemResponse;
import com.wuye.points.dto.coin.EarnCoinRequest;
import com.wuye.points.dto.coin.EarnCoinResponse;
import com.wuye.points.dto.coin.ExpiringCoinResponse;
import com.wuye.points.dto.coin.FamilyCoinBalanceResponse;
import com.wuye.points.dto.coin.FreezeCoinRequest;
import com.wuye.points.dto.coin.FreezeCoinResponse;
import com.wuye.points.dto.coin.HideCoinRequest;
import com.wuye.points.dto.coin.HideCoinResponse;
import com.wuye.points.dto.coin.RechargeCoinRequest;
import com.wuye.points.dto.coin.RechargeCoinResponse;
import com.wuye.points.dto.coin.SpendCoinRequest;
import com.wuye.points.dto.coin.SpendCoinResponse;
import com.wuye.points.dto.coin.UnfreezeCoinRequest;
import com.wuye.points.dto.coin.UnfreezeCoinResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.CoinFreezeRecord;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.CoinType;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoinFreezeRecordRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PropertyCoinRecordSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PropertyCoinService {

    private static final Set<String> EARN_SOURCES = Set.of("order", "property_fee", "manual");
    private static final Set<String> SPEND_SOURCES = Set.of("order", "property_fee", "manual");

    private final PropertyCoinRecordRepository coinRecordRepository;
    private final CoinFreezeRecordRepository coinFreezeRecordRepository;
    private final ResidentRepository residentRepository;
    private final FamilyRepository familyRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyCoinConverter coinConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public CoinBalanceResponse getBalance() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        assertCoinDisplayEnabled(resident);

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        BigDecimal todayEarned = coinRecordRepository.sumEarnedBetween(
                resident.getId(), dayStart, dayEnd);
        BigDecimal todaySpent = coinRecordRepository.sumSpentBetween(
                resident.getId(), dayStart, dayEnd);

        BigDecimal familyCoinBalance = null;
        String familyId = resident.getFamilyId();
        if (StringUtils.hasText(familyId)) {
            familyCoinBalance = familyRepository.findById(familyId)
                    .map(Family::getCoinBalance)
                    .map(coinConverter::defaultCoinBalance)
                    .orElse(BigDecimal.ZERO);
        }

        return CoinBalanceResponse.builder()
                .coinBalance(defaultCoinBalance(resident))
                .familyCoinBalance(familyCoinBalance)
                .familyId(familyId)
                .coinFrozen(Boolean.TRUE.equals(resident.getCoinFrozen()))
                .coinHidden(Boolean.TRUE.equals(resident.getCoinHidden()))
                .coinDisplayEnabled(isCoinDisplayEnabled(resident))
                .todayEarned(todayEarned != null ? todayEarned : BigDecimal.ZERO)
                .todaySpent(todaySpent != null ? todaySpent : BigDecimal.ZERO)
                .expiringSoon(BigDecimal.ZERO)
                .expiryDate(null)
                .build();
    }

    @Transactional(readOnly = true)
    public FamilyCoinBalanceResponse getFamilyBalance() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(resident.getFamilyId())) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在");
        }

        Family family = familyRepository.findById(resident.getFamilyId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));

        return FamilyCoinBalanceResponse.builder()
                .familyId(family.getId())
                .familyName(family.getName())
                .coinBalance(defaultFamilyCoinBalance(family))
                .memberCount(family.getMemberCount() != null ? family.getMemberCount() : 0)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<CoinRecordItemResponse> listRecords(
            int page,
            int pageSize,
            String coinType,
            String source,
            String startDate,
            String endDate,
            String familyId,
            String sort) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateCoinType(coinType);
        validateCoinSource(source);

        Resident resident = getActiveResident(UserContext.currentUserId());
        String scopedFamilyId = resolveFamilyScope(resident, familyId);

        Pageable pageable = buildPageable(page, pageSize, sort);
        LocalDateTime start = parseIsoDateTime(startDate);
        LocalDateTime end = parseIsoDateTime(endDate);

        Page<PropertyCoinRecord> recordPage = coinRecordRepository.findAll(
                PropertyCoinRecordSpecifications.withFilters(
                        scopedFamilyId != null ? null : resident.getId(),
                        scopedFamilyId,
                        coinType,
                        source,
                        start,
                        end),
                pageable);

        List<CoinRecordItemResponse> list = recordPage.getContent().stream()
                .map(coinConverter::toRecordItem)
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ExpiringCoinResponse getExpiringCoins() {
        roleGuard.requireAnyRole("resident");
        return ExpiringCoinResponse.builder()
                .expiringSoon(BigDecimal.ZERO)
                .expiryDate(null)
                .build();
    }

    @Transactional
    public RechargeCoinResponse recharge(RechargeCoinRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        assertCoinNotFrozen(resident);
        assertCoinDisplayEnabled(resident);

        PropertyCoinRecord record = earnCoins(
                resident,
                request.getAmount(),
                CoinType.recharge.name(),
                "manual",
                null,
                "物业币充值",
                null);

        return RechargeCoinResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .amount(request.getAmount())
                .balanceAfter(record.getBalance())
                .familyBalanceAfter(resolveFamilyBalanceAfter(resident))
                .rechargedAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional
    public EarnCoinResponse earn(EarnCoinRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (!request.getResidentId().startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }
        if (!EARN_SOURCES.contains(request.getSource())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "source值无效");
        }

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        PropertyCoinRecord record = earnCoins(
                resident,
                request.getCoinAmount(),
                CoinType.earn.name(),
                request.getSource(),
                request.getSourceId(),
                request.getDescription(),
                UserContext.currentUserId());

        return EarnCoinResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .coinAmount(request.getCoinAmount())
                .balanceAfter(record.getBalance())
                .familyBalanceAfter(resolveFamilyBalanceAfter(resident))
                .source(request.getSource())
                .operatorId(UserContext.currentUserId())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional
    public SpendCoinResponse spend(SpendCoinRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (!request.getResidentId().startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }
        if (!SPEND_SOURCES.contains(request.getSource())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "source值无效");
        }

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        boolean useFamilyPool = Boolean.TRUE.equals(request.getUseFamilyPool())
                || StringUtils.hasText(request.getFamilyId());
        PropertyCoinRecord record = spendCoins(
                resident,
                request.getCoinAmount(),
                request.getSource(),
                request.getSourceId(),
                request.getDescription(),
                UserContext.currentUserId(),
                useFamilyPool);

        return SpendCoinResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .coinAmount(request.getCoinAmount())
                .balanceAfter(record.getBalance())
                .familyBalanceAfter(resolveFamilyBalanceAfter(resident))
                .source(request.getSource())
                .operatorId(UserContext.currentUserId())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional
    public FreezeCoinResponse freeze(FreezeCoinRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (!request.getResidentId().startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        if (Boolean.TRUE.equals(resident.getCoinFrozen())) {
            throw new BizException(ErrorCode.COIN_FROZEN, "物业币已处于冻结状态");
        }

        BigDecimal balance = defaultCoinBalance(resident);
        BigDecimal freezeAmount = request.getAmount() != null ? request.getAmount() : balance;
        if (freezeAmount.compareTo(balance) > 0) {
            throw new BizException(ErrorCode.COIN_INSUFFICIENT);
        }

        resident.setCoinFrozen(true);
        residentRepository.save(resident);

        String operatorId = UserContext.currentUserId();
        CoinFreezeRecord freezeRecord = new CoinFreezeRecord();
        freezeRecord.setId(idGenerator.generateId("cfr"));
        freezeRecord.setResidentId(resident.getId());
        freezeRecord.setPropertyCompanyId(resident.getPropertyCompanyId());
        freezeRecord.setAction("freeze");
        freezeRecord.setAmount(freezeAmount);
        freezeRecord.setReason(request.getReason());
        freezeRecord.setOperatorId(operatorId);
        coinFreezeRecordRepository.save(freezeRecord);

        return FreezeCoinResponse.builder()
                .id(freezeRecord.getId())
                .residentId(resident.getId())
                .residentName(resident.getName())
                .action("freeze")
                .amount(freezeAmount)
                .reason(request.getReason())
                .operatorId(operatorId)
                .operatorName(resolveResidentName(operatorId))
                .frozenAt(DateTimeUtil.toIsoUtc(freezeRecord.getCreatedAt()))
                .build();
    }

    @Transactional
    public UnfreezeCoinResponse unfreeze(UnfreezeCoinRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (!request.getResidentId().startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }
        if (StringUtils.hasText(request.getFrozenRecordId())
                && !request.getFrozenRecordId().startsWith("cfr_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "frozenRecordId格式不正确");
        }

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        if (!Boolean.TRUE.equals(resident.getCoinFrozen())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "物业币未处于冻结状态");
        }

        BigDecimal unfrozenAmount;
        if (StringUtils.hasText(request.getFrozenRecordId())) {
            CoinFreezeRecord freezeRecord = coinFreezeRecordRepository
                    .findByIdAndResidentId(request.getFrozenRecordId(), resident.getId())
                    .filter(record -> "freeze".equals(record.getAction()))
                    .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "冻结记录不存在"));
            unfrozenAmount = freezeRecord.getAmount() != null ? freezeRecord.getAmount() : defaultCoinBalance(resident);
        } else {
            unfrozenAmount = coinFreezeRecordRepository.findByResidentIdAndAction(resident.getId(), "freeze")
                    .stream()
                    .map(CoinFreezeRecord::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (unfrozenAmount.compareTo(BigDecimal.ZERO) == 0) {
                unfrozenAmount = defaultCoinBalance(resident);
            }
        }

        resident.setCoinFrozen(false);
        residentRepository.save(resident);

        String operatorId = UserContext.currentUserId();
        CoinFreezeRecord unfreezeRecord = new CoinFreezeRecord();
        unfreezeRecord.setId(idGenerator.generateId("cfr"));
        unfreezeRecord.setResidentId(resident.getId());
        unfreezeRecord.setPropertyCompanyId(resident.getPropertyCompanyId());
        unfreezeRecord.setAction("unfreeze");
        unfreezeRecord.setAmount(unfrozenAmount);
        unfreezeRecord.setReason(request.getReason());
        unfreezeRecord.setOperatorId(operatorId);
        coinFreezeRecordRepository.save(unfreezeRecord);

        return UnfreezeCoinResponse.builder()
                .residentId(resident.getId())
                .unfrozenAmount(unfrozenAmount)
                .newBalance(defaultCoinBalance(resident))
                .unfrozenAt(DateTimeUtil.toIsoUtc(unfreezeRecord.getCreatedAt()))
                .operatorId(operatorId)
                .build();
    }

    @Transactional
    public HideCoinResponse hide(HideCoinRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (!isCoinDisplayEnabled(resident)) {
            throw new BizException(ErrorCode.COIN_EXCHANGE_NOT_ALLOWED, "物业公司已禁用物业币显示");
        }

        resident.setCoinHidden(Boolean.TRUE.equals(request.getHidden()));
        residentRepository.save(resident);

        return HideCoinResponse.builder()
                .residentId(resident.getId())
                .coinHidden(resident.getCoinHidden())
                .updatedAt(DateTimeUtil.toIsoUtc(resident.getUpdatedAt()))
                .build();
    }

    /**
     * 供其他模块调用的物业币获取
     */
    @Transactional
    public PropertyCoinRecord earnCoins(Resident resident,
                                        BigDecimal amount,
                                        String coinType,
                                        String source,
                                        String sourceId,
                                        String description,
                                        String operatorId) {
        assertCoinNotFrozen(resident);

        BigDecimal newBalance = defaultCoinBalance(resident).add(amount);
        resident.setCoinBalance(newBalance);

        if (StringUtils.hasText(resident.getFamilyId())) {
            Family family = familyRepository.findById(resident.getFamilyId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
            family.setCoinBalance(defaultFamilyCoinBalance(family).add(amount));
            familyRepository.save(family);
        }

        residentRepository.save(resident);

        return createRecord(
                resident,
                resident.getFamilyId(),
                coinType,
                amount,
                newBalance,
                source,
                sourceId,
                description,
                operatorId);
    }

    /**
     * 供其他模块调用的物业币消费（优先扣家庭池）
     */
    @Transactional
    public PropertyCoinRecord spendCoins(Resident resident,
                                         BigDecimal amount,
                                         String source,
                                         String sourceId,
                                         String description,
                                         String operatorId,
                                         boolean useFamilyPool) {
        assertCoinNotFrozen(resident);

        BigDecimal remaining = amount;

        if (useFamilyPool && StringUtils.hasText(resident.getFamilyId())) {
            Family family = familyRepository.findById(resident.getFamilyId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
            BigDecimal familyBalance = defaultFamilyCoinBalance(family);
            BigDecimal fromFamily = remaining.min(familyBalance);
            if (fromFamily.compareTo(BigDecimal.ZERO) > 0) {
                family.setCoinBalance(familyBalance.subtract(fromFamily));
                familyRepository.save(family);
                remaining = remaining.subtract(fromFamily);
            }
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal personalBalance = defaultCoinBalance(resident);
            if (personalBalance.compareTo(remaining) < 0) {
                throw new BizException(ErrorCode.COIN_INSUFFICIENT);
            }
            resident.setCoinBalance(personalBalance.subtract(remaining));
            residentRepository.save(resident);
        }

        return createRecord(
                resident,
                resident.getFamilyId(),
                CoinType.spend.name(),
                amount.negate(),
                defaultCoinBalance(resident),
                source,
                sourceId,
                description,
                operatorId);
    }

    private PropertyCoinRecord createRecord(Resident resident,
                                            String familyId,
                                            String coinType,
                                            BigDecimal amount,
                                            BigDecimal balance,
                                            String source,
                                            String sourceId,
                                            String description,
                                            String operatorId) {
        PropertyCoinRecord record = new PropertyCoinRecord();
        record.setId(idGenerator.generateId("coin"));
        record.setResidentId(resident.getId());
        record.setFamilyId(familyId);
        record.setPropertyCompanyId(resident.getPropertyCompanyId());
        record.setCoinType(coinType);
        record.setAmount(amount);
        record.setBalance(balance);
        record.setSource(source);
        record.setSourceId(sourceId);
        record.setDescription(description);
        record.setOperatorId(operatorId);
        return coinRecordRepository.save(record);
    }

    private void assertCoinNotFrozen(Resident resident) {
        if (Boolean.TRUE.equals(resident.getCoinFrozen())) {
            throw new BizException(ErrorCode.COIN_FROZEN);
        }
    }

    private void assertCoinDisplayEnabled(Resident resident) {
        if (!isCoinDisplayEnabled(resident)) {
            throw new BizException(ErrorCode.COIN_EXCHANGE_NOT_ALLOWED, "物业公司已禁用物业币显示");
        }
    }

    private boolean isCoinDisplayEnabled(Resident resident) {
        if (!StringUtils.hasText(resident.getPropertyCompanyId())) {
            return true;
        }
        return propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                .map(PropertyCompany::getCoinDisplayEnabled)
                .orElse(true);
    }

    private BigDecimal resolveFamilyBalanceAfter(Resident resident) {
        if (!StringUtils.hasText(resident.getFamilyId())) {
            return null;
        }
        return familyRepository.findById(resident.getFamilyId())
                .map(this::defaultFamilyCoinBalance)
                .orElse(null);
    }

    private String resolveResidentName(String residentId) {
        if (residentId == null) {
            return null;
        }
        return residentRepository.findById(residentId)
                .map(Resident::getName)
                .orElse(null);
    }

    private String resolveFamilyScope(Resident resident, String familyId) {
        if (!StringUtils.hasText(familyId)) {
            return null;
        }
        if (!familyId.startsWith("fam_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "familyId格式不正确");
        }
        if (!familyId.equals(resident.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员");
        }
        return familyId;
    }

    private void validateCoinType(String coinType) {
        if (StringUtils.hasText(coinType) && !RoleGuard.COIN_TYPES.contains(coinType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "coinType值无效");
        }
    }

    private void validateCoinSource(String source) {
        if (StringUtils.hasText(source) && !RoleGuard.COIN_SOURCES.contains(source)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "source值无效");
        }
    }

    private Resident getActiveResident(String userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }

    private BigDecimal defaultCoinBalance(Resident resident) {
        return coinConverter.defaultCoinBalance(resident.getCoinBalance());
    }

    private BigDecimal defaultFamilyCoinBalance(Family family) {
        return coinConverter.defaultCoinBalance(family.getCoinBalance());
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = Sort.Direction.DESC;
        String field = sort;
        if (sort.startsWith("-")) {
            field = sort.substring(1);
            direction = Sort.Direction.DESC;
        } else if (sort.startsWith("+")) {
            field = sort.substring(1);
            direction = Sort.Direction.ASC;
        }
        if (!"createdAt".equals(field)) {
            field = "createdAt";
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }

    private LocalDateTime parseIsoDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式不正确");
        }
    }
}
