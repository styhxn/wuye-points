package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertyfee.CreatePropertyFeeRequest;
import com.wuye.points.dto.propertyfee.PayPropertyFeeRequest;
import com.wuye.points.dto.propertyfee.PayPropertyFeeResponse;
import com.wuye.points.dto.propertyfee.PropertyFeeDetailResponse;
import com.wuye.points.dto.propertyfee.PropertyFeeListItemResponse;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.PropertyFeeRule;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.PropertyFeeRuleRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PropertyFeeSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyFeeService {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "dueDate", "amount", "period");

    private final PropertyFeeRepository propertyFeeRepository;
    private final PropertyFeeRuleRepository propertyFeeRuleRepository;
    private final ResidentRepository residentRepository;
    private final CommunityRepository communityRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyFeeConverter propertyFeeConverter;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;
    private final PointRecordRepository pointRecordRepository;
    private final FamilyRepository familyRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<PropertyFeeListItemResponse> listPropertyFees(
            int page,
            int pageSize,
            String feeType,
            String paymentStatus,
            String period,
            String communityId,
            String residentId,
            String propertyCompanyId,
            String keyword,
            String sort) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateFeeType(feeType);
        validatePaymentStatus(paymentStatus);
        validateOptionalId(communityId, "com_");
        validateOptionalId(residentId, "res_");

        String scopedPropertyCompanyId = roleGuard.resolveListPropertyCompanyId(propertyCompanyId);
        if (StringUtils.hasText(propertyCompanyId) && !propertyCompanyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
        }

        Pageable pageable = buildPageable(page, pageSize, sort);
        Specification<PropertyFee> spec = PropertyFeeSpecifications.withFilters(
                scopedPropertyCompanyId,
                residentId,
                null,
                communityId,
                feeType,
                paymentStatus,
                period,
                keyword);

        Page<PropertyFee> feePage = propertyFeeRepository.findAll(spec, pageable);
        return buildListPage(feePage, page, pageSize);
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertyFeeListItemResponse> listMyPropertyFees(
            int page,
            int pageSize,
            String feeType,
            String paymentStatus,
            String period,
            String sort) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateFeeType(feeType);
        validatePaymentStatus(paymentStatus);

        Resident resident = getActiveResident(UserContext.currentUserId());
        Pageable pageable = buildPageable(page, pageSize, sort);

        Specification<PropertyFee> spec = Specification.where(
                        PropertyFeeSpecifications.forResidentScope(resident.getId(), resident.getFamilyId()))
                .and(PropertyFeeSpecifications.withFilters(
                        resident.getPropertyCompanyId(),
                        null,
                        null,
                        null,
                        feeType,
                        paymentStatus,
                        period,
                        null));

        Page<PropertyFee> feePage = propertyFeeRepository.findAll(spec, pageable);
        return buildListPage(feePage, page, pageSize);
    }

    @Transactional(readOnly = true)
    public PropertyFeeDetailResponse getPropertyFee(String id) {
        validatePropertyFeeId(id);
        PropertyFee fee = propertyFeeRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_FEE_NOT_FOUND));

        if (roleGuard.isResident()) {
            Resident resident = getActiveResident(UserContext.currentUserId());
            assertCanAccessFee(resident, fee);
        } else {
            roleGuard.requireAnyRole("property_admin", "platform_admin");
            assertFeeInAdminScope(fee);
        }

        return toDetail(fee);
    }

    @Transactional
    public PropertyFeeDetailResponse createPropertyFee(CreatePropertyFeeRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateFeeType(request.getFeeType());
        validatePeriod(request.getPeriod());
        if (!request.getResidentId().startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        String communityId = request.getCommunityId();
        if (StringUtils.hasText(communityId)) {
            if (!communityId.startsWith("com_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "communityId格式不正确");
            }
        } else {
            communityId = resident.getCommunityId();
        }

        // 校验同住户+同账期是否已存在账单
        if (propertyFeeRepository.existsByResidentIdAndPeriod(resident.getId(), request.getPeriod())) {
            throw new BizException(ErrorCode.PROPERTY_FEE_DUPLICATE_PERIOD, "该账期已存在物业费账单");
        }

        PropertyFee fee = new PropertyFee();
        fee.setId(idGenerator.generateId("pf"));
        fee.setResidentId(resident.getId());
        fee.setFamilyId(resident.getFamilyId());
        fee.setPropertyCompanyId(resident.getPropertyCompanyId());
        fee.setCommunityId(communityId);
        fee.setFeeType(request.getFeeType());
        fee.setPeriod(request.getPeriod());
        fee.setAmount(request.getAmount());
        fee.setPaidAmount(BigDecimal.ZERO);
        fee.setPointDeducted(0);
        fee.setCoinDeducted(BigDecimal.ZERO);
        fee.setPaymentStatus("unpaid");
        fee.setLateFee(BigDecimal.ZERO);
        fee.setDueDate(parseDate(request.getDueDate()));
        fee.setAutoDeduct(Boolean.TRUE.equals(request.getAutoDeduct()));
        propertyFeeRepository.save(fee);

        return toDetail(fee);
    }

    @Transactional
    public PayPropertyFeeResponse payPropertyFee(String id, PayPropertyFeeRequest request) {
        roleGuard.requireAnyRole("resident");
        validatePropertyFeeId(id);
        validatePaymentMethod(request.getPaymentMethod());

        Resident resident = getActiveResident(UserContext.currentUserId());
        PropertyFee fee = propertyFeeRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_FEE_NOT_FOUND));

        assertCanAccessFee(resident, fee);
        assertFeePayable(fee);

        BigDecimal remainingAmount = fee.getAmount().subtract(propertyFeeConverter.defaultPaidAmount(fee));
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "账单已支付");
        }
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "支付金额超出账单剩余金额");
        }

        boolean useFamilyPool = resolveUseFamilyPool(request.getFamilyId(), fee, resident);
        BigDecimal pointToFeeRate = resolvePointToFeeRate(resident.getPropertyCompanyId());
        FeePaymentBreakdown breakdown = resolvePaymentBreakdown(
                request, request.getAmount(), pointToFeeRate);

        if (breakdown.pointUsed() > 0) {
            pointService.spendPoints(
                    resident,
                    breakdown.pointUsed(),
                    "fee_deduct",
                    fee.getId(),
                    "积分抵扣物业费",
                    null,
                    useFamilyPool);
        }
        if (breakdown.coinUsed().compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(
                    resident,
                    breakdown.coinUsed(),
                    "property_fee",
                    fee.getId(),
                    "物业币缴纳物业费",
                    null,
                    useFamilyPool);
        }

        fee.setPointDeducted(propertyFeeConverter.defaultPointDeducted(fee) + breakdown.pointUsed());
        fee.setCoinDeducted(propertyFeeConverter.defaultCoinDeducted(fee).add(breakdown.coinUsed()));
        fee.setPaidAmount(propertyFeeConverter.defaultPaidAmount(fee).add(request.getAmount()));
        fee.setPaymentStatus(propertyFeeConverter.resolvePaymentStatus(fee));
        if ("paid".equals(fee.getPaymentStatus())) {
            fee.setPaidAt(LocalDateTime.now());

            if (fee.getDueDate() != null && LocalDate.now().isBefore(fee.getDueDate())) {
                PointRecord rewardRecord = new PointRecord();
                rewardRecord.setId(idGenerator.generateId("pt"));
                rewardRecord.setResidentId(fee.getResidentId());
                rewardRecord.setFamilyId(fee.getFamilyId());
                rewardRecord.setPropertyCompanyId(fee.getPropertyCompanyId());
                rewardRecord.setPointType("earn");
                rewardRecord.setAmount(100);
                rewardRecord.setBalance(resident.getPointBalance() + 100);
                rewardRecord.setSource("on_time_payment_reward");
                rewardRecord.setSourceId(fee.getId());
                rewardRecord.setDescription("准时缴费奖励");
                pointRecordRepository.save(rewardRecord);

                resident.setPointBalance(resident.getPointBalance() + 100);
                resident.setOnTimePaymentBadge(true);
                residentRepository.save(resident);

                if (fee.getFamilyId() != null) {
                    familyRepository.findById(fee.getFamilyId()).ifPresent(family -> {
                        family.setPointBalance(family.getPointBalance() + 100);
                        familyRepository.save(family);
                    });
                }
            }
        }
        propertyFeeRepository.save(fee);

        BigDecimal newRemaining = fee.getAmount().subtract(propertyFeeConverter.defaultPaidAmount(fee))
                .max(BigDecimal.ZERO);

        return PayPropertyFeeResponse.builder()
                .propertyFeeId(fee.getId())
                .paidAmount(request.getAmount())
                .totalPaidAmount(propertyFeeConverter.defaultPaidAmount(fee))
                .remainingAmount(newRemaining)
                .paymentStatus(fee.getPaymentStatus())
                .paymentMethod(request.getPaymentMethod())
                .pointUsed(breakdown.pointUsed())
                .coinUsed(breakdown.coinUsed())
                .cashAmount(breakdown.cashAmount())
                .paidAt(DateTimeUtil.toIsoUtc(fee.getPaidAt()))
                .build();
    }

    private PageResponse<PropertyFeeListItemResponse> buildListPage(Page<PropertyFee> feePage,
                                                                    int page,
                                                                    int pageSize) {
        List<PropertyFee> fees = feePage.getContent();
        Map<String, Resident> residentMap = loadResidents(fees);
        Map<String, Community> communityMap = loadCommunities(fees);
        Map<String, PropertyFeeRule> ruleCache = loadRules(fees);

        for (PropertyFee fee : fees) {
            PropertyFeeRule rule = propertyFeeConverter.pickRule(
                    ruleCache, fee.getPropertyCompanyId(), fee.getFeeType());
            fee.setLateFee(calculateLateFee(fee, rule));
        }

        List<PropertyFeeListItemResponse> list = fees.stream()
                .map(fee -> propertyFeeConverter.toListItem(
                        fee,
                        residentMap.get(fee.getResidentId()),
                        communityMap.get(fee.getCommunityId()),
                        propertyFeeConverter.pickRule(
                                ruleCache, fee.getPropertyCompanyId(), fee.getFeeType())))
                .toList();

        return PageResponse.of(list, page, pageSize, feePage.getTotalElements());
    }

    private PropertyFeeDetailResponse toDetail(PropertyFee fee) {
        Resident resident = residentRepository.findById(fee.getResidentId()).orElse(null);
        Community community = StringUtils.hasText(fee.getCommunityId())
                ? communityRepository.findById(fee.getCommunityId()).orElse(null)
                : null;
        PropertyFeeRule rule = propertyFeeRuleRepository
                .findFirstByPropertyCompanyIdAndFeeTypeAndStatus(
                        fee.getPropertyCompanyId(), fee.getFeeType(), "active")
                .orElse(null);
        fee.setLateFee(calculateLateFee(fee, rule));
        return propertyFeeConverter.toDetail(fee, resident, community, rule);
    }

    private Map<String, Resident> loadResidents(List<PropertyFee> fees) {
        List<String> residentIds = fees.stream()
                .map(PropertyFee::getResidentId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity()));
    }

    private Map<String, Community> loadCommunities(List<PropertyFee> fees) {
        List<String> communityIds = fees.stream()
                .map(PropertyFee::getCommunityId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (communityIds.isEmpty()) {
            return Map.of();
        }
        return communityRepository.findAllById(communityIds).stream()
                .collect(Collectors.toMap(Community::getId, Function.identity()));
    }

    private Map<String, PropertyFeeRule> loadRules(List<PropertyFee> fees) {
        Map<String, PropertyFeeRule> cache = new HashMap<>();
        for (PropertyFee fee : fees) {
            String cacheKey = fee.getPropertyCompanyId() + ":" + fee.getFeeType();
            if (cache.containsKey(cacheKey)) {
                continue;
            }
            propertyFeeRuleRepository
                    .findFirstByPropertyCompanyIdAndFeeTypeAndStatus(
                            fee.getPropertyCompanyId(), fee.getFeeType(), "active")
                    .ifPresent(rule -> cache.put(cacheKey, rule));
        }
        return cache;
    }

    private FeePaymentBreakdown resolvePaymentBreakdown(PayPropertyFeeRequest request,
                                                        BigDecimal payAmount,
                                                        BigDecimal pointToFeeRate) {
        return switch (request.getPaymentMethod()) {
            case "property_coin" -> {
                BigDecimal coinUsed = request.getCoinUsed() != null ? request.getCoinUsed() : payAmount;
                if (coinUsed.compareTo(payAmount) != 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "物业币支付金额不匹配");
                }
                yield new FeePaymentBreakdown(0, coinUsed, BigDecimal.ZERO);
            }
            case "point" -> {
                int pointUsed = request.getPointUsed() != null
                        ? request.getPointUsed()
                        : payAmount.divide(pointToFeeRate, 0, RoundingMode.UP).intValue();
                BigDecimal pointValue = pointToFeeRate
                        .multiply(BigDecimal.valueOf(pointUsed))
                        .setScale(2, RoundingMode.HALF_UP);
                if (pointValue.compareTo(payAmount) < 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "积分抵扣金额不足");
                }
                yield new FeePaymentBreakdown(pointUsed, BigDecimal.ZERO, BigDecimal.ZERO);
            }
            case "wechat" -> new FeePaymentBreakdown(0, BigDecimal.ZERO, payAmount);
            case "mixed" -> {
                int pointUsed = request.getPointUsed() != null ? request.getPointUsed() : 0;
                BigDecimal coinUsed = request.getCoinUsed() != null ? request.getCoinUsed() : BigDecimal.ZERO;
                BigDecimal pointValue = pointToFeeRate
                        .multiply(BigDecimal.valueOf(pointUsed))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal cashAmount = payAmount.subtract(pointValue).subtract(coinUsed);
                if (cashAmount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "混合支付金额超出应付金额");
                }
                BigDecimal covered = pointValue.add(coinUsed).add(cashAmount);
                if (covered.compareTo(payAmount) < 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "混合支付金额不足");
                }
                yield new FeePaymentBreakdown(pointUsed, coinUsed, cashAmount);
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        };
    }

    private boolean resolveUseFamilyPool(String familyId, PropertyFee fee, Resident resident) {
        if (StringUtils.hasText(familyId)) {
            if (!familyId.startsWith("fam_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "familyId格式不正确");
            }
            if (!familyId.equals(resident.getFamilyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员");
            }
            return true;
        }
        return StringUtils.hasText(fee.getFamilyId())
                && fee.getFamilyId().equals(resident.getFamilyId());
    }

    private BigDecimal resolvePointToFeeRate(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return DEFAULT_POINT_TO_FEE_RATE;
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getPointToFeeRate)
                .filter(rate -> rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                .orElse(DEFAULT_POINT_TO_FEE_RATE);
    }

    private void assertCanAccessFee(Resident resident, PropertyFee fee) {
        if (!resident.getPropertyCompanyId().equals(fee.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        if (resident.getId().equals(fee.getResidentId())) {
            return;
        }
        if (StringUtils.hasText(fee.getFamilyId())
                && fee.getFamilyId().equals(resident.getFamilyId())) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertFeeInAdminScope(PropertyFee fee) {
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (currentPropertyCompanyId == null
                || !currentPropertyCompanyId.equals(fee.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PROPERTY_FEE_NOT_FOUND);
        }
    }

    private void assertFeePayable(PropertyFee fee) {
        if ("paid".equals(propertyFeeConverter.resolvePaymentStatus(fee))) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "账单已支付");
        }
    }

    private void validatePropertyFeeId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BizException(ErrorCode.PARAM_MISSING);
        }
        if (!id.startsWith("pf_") && !id.startsWith("fee_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "物业费ID格式不正确");
        }
    }

    private void validatePaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod)
                || !RoleGuard.PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        }
    }

    private void validateFeeType(String feeType) {
        if (StringUtils.hasText(feeType) && !RoleGuard.FEE_TYPES.contains(feeType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "feeType值无效");
        }
    }

    private void validatePaymentStatus(String paymentStatus) {
        if (StringUtils.hasText(paymentStatus)
                && !RoleGuard.FEE_PAYMENT_STATUS.contains(paymentStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentStatus值无效");
        }
    }

    private void validatePeriod(String period) {
        if (!StringUtils.hasText(period)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "period不能为空");
        }
    }

    private void validateOptionalId(String id, String prefix) {
        if (StringUtils.hasText(id) && !id.startsWith(prefix)) {
            throw new BizException(ErrorCode.PARAM_INVALID, prefix + "格式不正确");
        }
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "dueDate格式不正确");
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
        if (!SORTABLE_FIELDS.contains(field)) {
            field = "createdAt";
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }

    private BigDecimal calculateLateFee(PropertyFee fee, PropertyFeeRule rule) {
        String status = propertyFeeConverter.resolvePaymentStatus(fee);
        if (!"overdue".equals(status)) {
            return BigDecimal.ZERO;
        }
        if (fee.getDueDate() == null || rule == null || rule.getOverdueRate() == null
                || rule.getOverdueRate().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal paidAmount = propertyFeeConverter.defaultPaidAmount(fee);
        BigDecimal remainingAmount = fee.getAmount().subtract(paidAmount).max(BigDecimal.ZERO);
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(fee.getDueDate(), LocalDate.now());
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal lateFee = remainingAmount
                .multiply(rule.getOverdueRate())
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal cap = fee.getAmount().multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
        return lateFee.min(cap);
    }

    private record FeePaymentBreakdown(int pointUsed, BigDecimal coinUsed, BigDecimal cashAmount) {
    }
}
