package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.point.DeductFeeRequest;
import com.wuye.points.dto.point.DeductFeeResponse;
import com.wuye.points.dto.point.EarnPointRequest;
import com.wuye.points.dto.point.EarnPointResponse;
import com.wuye.points.dto.point.ExpiringPointsResponse;
import com.wuye.points.dto.point.FamilyPointBalanceResponse;
import com.wuye.points.dto.point.PointBalanceResponse;
import com.wuye.points.dto.point.PointRecordItemResponse;
import com.wuye.points.dto.point.RedeemPointRequest;
import com.wuye.points.dto.point.RedeemPointResponse;
import com.wuye.points.dto.point.SpendPointRequest;
import com.wuye.points.dto.point.SpendPointResponse;
import com.wuye.points.dto.point.TransferPointRequest;
import com.wuye.points.dto.point.TransferPointResponse;
import com.wuye.points.dto.resident.TransferToPropertyPointRequest;
import com.wuye.points.dto.resident.TransferToPropertyPointResponse;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.PointType;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PointRecordSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.FeatureSwitchGuard;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");
    private static final Set<String> EARN_SOURCES = Set.of(
            "order", "activity", "sign_in", "manual", "merchant_grant");
    private static final Set<String> SPEND_SOURCES = Set.of(
            "order", "fee_deduct", "manual", "transfer");

    /** 住户主动转给物业的流水来源标识，须与管理端对账查询（AdminTransferToPropertyService）保持一致。 */
    public static final String SOURCE_TRANSFER_TO_PROPERTY = "transfer_to_property";

    private final PointRecordRepository pointRecordRepository;
    private final ResidentRepository residentRepository;
    private final FamilyRepository familyRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyFeeRepository propertyFeeRepository;
    private final PointConverter pointConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final FeatureSwitchGuard featureSwitchGuard;

    @Transactional(readOnly = true)
    public PointBalanceResponse getBalance() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        Integer todayEarned = pointRecordRepository.sumEarnedBetween(
                resident.getId(), dayStart, dayEnd);
        Integer todaySpent = pointRecordRepository.sumSpentBetween(
                resident.getId(), dayStart, dayEnd);

        Integer familyPointBalance = null;
        String familyId = resident.getFamilyId();
        if (StringUtils.hasText(familyId)) {
            familyPointBalance = familyRepository.findById(familyId)
                    .map(Family::getPointBalance)
                    .orElse(0);
        }

        return PointBalanceResponse.builder()
                .pointBalance(defaultPointBalance(resident))
                .familyPointBalance(familyPointBalance)
                .familyId(familyId)
                .todayEarned(todayEarned != null ? todayEarned : 0)
                .todaySpent(todaySpent != null ? todaySpent : 0)
                .expiringPoints(0)
                .expiringDate(null)
                .build();
    }

    @Transactional(readOnly = true)
    public FamilyPointBalanceResponse getFamilyBalance() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(resident.getFamilyId())) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在");
        }

        Family family = familyRepository.findById(resident.getFamilyId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));

        List<Resident> members = residentRepository.findByFamilyId(family.getId());
        List<FamilyPointBalanceResponse.Member> memberItems = members.stream()
                .filter(m -> ResidentStatus.active.name().equalsIgnoreCase(m.getStatus()))
                .map(m -> FamilyPointBalanceResponse.Member.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .pointBalance(m.getPointBalance() != null ? m.getPointBalance() : 0)
                        .relation(m.getFamilyRelation())
                        .build())
                .toList();

        return FamilyPointBalanceResponse.builder()
                .familyId(family.getId())
                .familyName(family.getName())
                .pointBalance(defaultFamilyPointBalance(family))
                .memberCount(memberItems.size())
                .members(memberItems)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PointRecordItemResponse> listRecords(
            int page,
            int pageSize,
            String pointType,
            String source,
            String startDate,
            String endDate,
            String familyId,
            String sort) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validatePointType(pointType);
        validatePointSource(source);

        Resident resident = getActiveResident(UserContext.currentUserId());
        String scopedFamilyId = resolveFamilyScope(resident, familyId);

        Pageable pageable = buildPageable(page, pageSize, sort);
        LocalDateTime start = parseIsoDateTime(startDate);
        LocalDateTime end = parseIsoDateTime(endDate);

        Page<PointRecord> recordPage = pointRecordRepository.findAll(
                PointRecordSpecifications.withFilters(
                        scopedFamilyId != null ? null : resident.getId(),
                        scopedFamilyId,
                        pointType,
                        source,
                        start,
                        end),
                pageable);

        List<PointRecordItemResponse> list = recordPage.getContent().stream()
                .map(pointConverter::toRecordItem)
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    @Transactional
    public TransferPointResponse transfer(TransferPointRequest request) {
        roleGuard.requireAnyRole("resident");
        if (!request.getFamilyId().startsWith("fam_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "familyId格式不正确");
        }
        if (!request.getToMemberId().startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "toMemberId格式不正确");
        }

        String fromMemberId = UserContext.currentUserId();
        Resident fromMember = getActiveResident(fromMemberId);
        Family family = familyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));

        if (!request.getFamilyId().equals(fromMember.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员");
        }

        Resident toMember = residentRepository.findById(request.getToMemberId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!request.getFamilyId().equals(toMember.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员");
        }
        if (fromMemberId.equals(request.getToMemberId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "不能向自己转移积分");
        }

        int fromBalance = defaultPointBalance(fromMember);
        if (fromBalance < request.getAmount()) {
            throw new BizException(ErrorCode.POINT_INSUFFICIENT);
        }

        fromMember.setPointBalance(fromBalance - request.getAmount());
        int toBalance = defaultPointBalance(toMember) + request.getAmount();
        toMember.setPointBalance(toBalance);

        residentRepository.save(fromMember);
        residentRepository.save(toMember);

        String description = StringUtils.hasText(request.getRemark())
                ? request.getRemark()
                : "家庭内积分转移";

        PointRecord record = createRecord(
                fromMember,
                family.getId(),
                PointType.transfer.name(),
                -request.getAmount(),
                fromMember.getPointBalance(),
                "transfer",
                toMember.getId(),
                description,
                null);

        createRecord(
                toMember,
                family.getId(),
                PointType.transfer.name(),
                request.getAmount(),
                toMember.getPointBalance(),
                "transfer",
                fromMember.getId(),
                description,
                null);

        return TransferPointResponse.builder()
                .recordId(record.getId())
                .fromMemberId(fromMemberId)
                .toMemberId(request.getToMemberId())
                .amount(request.getAmount())
                .fromBalance(fromMember.getPointBalance())
                .toBalance(toMember.getPointBalance())
                .transferredAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional
    public DeductFeeResponse deductFee(DeductFeeRequest request) {
        roleGuard.requireAnyRole("resident");
        validatePropertyFeeId(request.getPropertyFeeId());

        Resident resident = getActiveResident(UserContext.currentUserId());
        PropertyFee fee = propertyFeeRepository.findById(request.getPropertyFeeId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_FEE_NOT_FOUND));

        assertCanAccessFee(resident, fee);
        assertFeePayable(fee);

        BigDecimal pointToFeeRate = resolvePointToFeeRate(resident.getPropertyCompanyId());
        BigDecimal deductedAmount = pointToFeeRate
                .multiply(BigDecimal.valueOf(request.getPoints()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingAmount = fee.getAmount()
                .subtract(defaultPaidAmount(fee))
                .subtract(deductedAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "抵扣积分超出账单剩余金额");
        }

        boolean useFamilyPool = StringUtils.hasText(request.getFamilyId());
        if (useFamilyPool) {
            if (!request.getFamilyId().startsWith("fam_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "familyId格式不正确");
            }
            if (!request.getFamilyId().equals(resident.getFamilyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员");
            }
            deductFromFamilyPool(request.getFamilyId(), request.getPoints());
        } else {
            deductFromPersonal(resident, request.getPoints());
        }

        fee.setPointDeducted(defaultPointDeducted(fee) + request.getPoints());
        fee.setPaidAmount(defaultPaidAmount(fee).add(deductedAmount));
        fee.setPaymentStatus(resolvePaymentStatus(fee.getAmount(), fee.getPaidAmount(), fee.getDueDate()));
        if ("paid".equals(fee.getPaymentStatus())) {
            fee.setPaidAt(LocalDateTime.now());
        }
        propertyFeeRepository.save(fee);

        PointRecord record = createRecord(
                resident,
                useFamilyPool ? request.getFamilyId() : resident.getFamilyId(),
                PointType.spend.name(),
                -request.getPoints(),
                defaultPointBalance(resident),
                "fee_deduct",
                fee.getId(),
                "积分抵扣物业费",
                null);

        Integer familyBalance = null;
        if (StringUtils.hasText(resident.getFamilyId())) {
            familyBalance = familyRepository.findById(resident.getFamilyId())
                    .map(this::defaultFamilyPointBalance)
                    .orElse(null);
        }

        return DeductFeeResponse.builder()
                .propertyFeeId(fee.getId())
                .pointsUsed(request.getPoints())
                .deductedAmount(deductedAmount)
                .remainingAmount(fee.getAmount().subtract(fee.getPaidAmount()))
                .paymentStatus(fee.getPaymentStatus())
                .residentBalance(defaultPointBalance(resident))
                .familyBalance(familyBalance)
                .deductedAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional
    public EarnPointResponse earn(EarnPointRequest request) {
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

        PointRecord record = earnPoints(
                resident,
                request.getPointAmount(),
                request.getSource(),
                request.getSourceId(),
                request.getDescription(),
                UserContext.currentUserId());

        Integer familyBalanceAfter = null;
        if (StringUtils.hasText(resident.getFamilyId())) {
            familyBalanceAfter = familyRepository.findById(resident.getFamilyId())
                    .map(this::defaultFamilyPointBalance)
                    .orElse(null);
        }

        return EarnPointResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .pointAmount(request.getPointAmount())
                .balanceAfter(record.getBalance())
                .familyBalanceAfter(familyBalanceAfter)
                .source(request.getSource())
                .operatorId(UserContext.currentUserId())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional
    public SpendPointResponse spend(SpendPointRequest request) {
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
        PointRecord record = spendPoints(
                resident,
                request.getPointAmount(),
                request.getSource(),
                request.getSourceId(),
                request.getDescription(),
                UserContext.currentUserId(),
                useFamilyPool);

        Integer familyBalanceAfter = null;
        if (StringUtils.hasText(resident.getFamilyId())) {
            familyBalanceAfter = familyRepository.findById(resident.getFamilyId())
                    .map(this::defaultFamilyPointBalance)
                    .orElse(null);
        }

        return SpendPointResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .pointAmount(request.getPointAmount())
                .balanceAfter(record.getBalance())
                .familyBalanceAfter(familyBalanceAfter)
                .source(request.getSource())
                .operatorId(UserContext.currentUserId())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional
    public RedeemPointResponse redeem(RedeemPointRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        BigDecimal pointToFeeRate = resolvePointToFeeRate(resident.getPropertyCompanyId());
        BigDecimal redeemedAmount = pointToFeeRate
                .multiply(BigDecimal.valueOf(request.getPoints()))
                .setScale(2, RoundingMode.HALF_UP);

        boolean useFamilyPool = StringUtils.hasText(resident.getFamilyId());
        if (useFamilyPool) {
            deductFromFamilyPool(resident.getFamilyId(), request.getPoints());
        } else {
            deductFromPersonal(resident, request.getPoints());
        }

        PointRecord record = createRecord(
                resident,
                resident.getFamilyId(),
                PointType.spend.name(),
                -request.getPoints(),
                defaultPointBalance(resident),
                "manual",
                null,
                "积分兑换",
                null);

        Integer familyBalanceAfter = null;
        if (StringUtils.hasText(resident.getFamilyId())) {
            familyBalanceAfter = familyRepository.findById(resident.getFamilyId())
                    .map(this::defaultFamilyPointBalance)
                    .orElse(null);
        }

        return RedeemPointResponse.builder()
                .pointsUsed(request.getPoints())
                .redeemedAmount(redeemedAmount)
                .balanceAfter(defaultPointBalance(resident))
                .familyBalanceAfter(familyBalanceAfter)
                .redeemedAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public ExpiringPointsResponse getExpiringPoints() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        if (!StringUtils.hasText(resident.getPropertyCompanyId())) {
            return ExpiringPointsResponse.builder()
                    .expiringPoints(0)
                    .expiringDate(null)
                    .build();
        }

        PropertyCompany company = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                .orElse(null);
        if (company == null || !Boolean.TRUE.equals(company.getTwoYearClearEnabled())) {
            return ExpiringPointsResponse.builder()
                    .expiringPoints(0)
                    .expiringDate(null)
                    .build();
        }

        Page<PropertyFee> unpaidPage = propertyFeeRepository.findOldestUnpaidFees(
                resident.getId(), PageRequest.of(0, 1));
        if (unpaidPage.isEmpty()) {
            return ExpiringPointsResponse.builder()
                    .expiringPoints(0)
                    .expiringDate(null)
                    .build();
        }

        PropertyFee oldestUnpaid = unpaidPage.getContent().get(0);
        if (oldestUnpaid.getDueDate() == null) {
            return ExpiringPointsResponse.builder()
                    .expiringPoints(0)
                    .expiringDate(null)
                    .build();
        }

        LocalDate expiryDate = oldestUnpaid.getDueDate().plusYears(2);
        LocalDate now = LocalDate.now();
        LocalDate in30Days = now.plusDays(30);

        if (expiryDate.isAfter(now) && !expiryDate.isAfter(in30Days)) {
            return ExpiringPointsResponse.builder()
                    .expiringPoints(defaultPointBalance(resident))
                    .expiringDate(DateTimeUtil.toIsoUtc(expiryDate.atStartOfDay()))
                    .build();
        }

        return ExpiringPointsResponse.builder()
                .expiringPoints(0)
                .expiringDate(null)
                .build();
    }

    /**
     * 供其他模块调用的积分获取
     */
    @Transactional
    public PointRecord earnPoints(Resident resident,
                                  int amount,
                                  String source,
                                  String sourceId,
                                  String description,
                                  String operatorId) {
        int newBalance = defaultPointBalance(resident) + amount;
        resident.setPointBalance(newBalance);

        if (StringUtils.hasText(resident.getFamilyId())) {
            Family family = familyRepository.findById(resident.getFamilyId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
            family.setPointBalance(defaultFamilyPointBalance(family) + amount);
            familyRepository.save(family);
        }

        residentRepository.save(resident);

        return createRecord(
                resident,
                resident.getFamilyId(),
                PointType.earn.name(),
                amount,
                newBalance,
                source,
                sourceId,
                description,
                operatorId);
    }

    /**
     * 住户主动将积分转赠给物业（个人积分扣减，记 spend 流水，不计入家庭池）。
     */
    @Transactional
    public TransferToPropertyPointResponse transferToProperty(TransferToPropertyPointRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        featureSwitchGuard.assertPointEnabled(resident.getPropertyCompanyId());
        int amount = request.getPointAmount();
        PointRecord record = spendPoints(resident, amount, SOURCE_TRANSFER_TO_PROPERTY, resident.getId(),
                request.getRemark() != null ? request.getRemark() : "住户积分转赠物业", null, false);
        return TransferToPropertyPointResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .pointAmount(amount)
                .balanceAfter(record.getBalance())
                .transferredAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    /**
     * 供其他模块调用的积分消费（优先扣家庭池）
     */
    @Transactional
    public PointRecord spendPoints(Resident resident,
                                   int amount,
                                   String source,
                                   String sourceId,
                                   String description,
                                   String operatorId,
                                   boolean useFamilyPool) {
        int remaining = amount;

        if (useFamilyPool && StringUtils.hasText(resident.getFamilyId())) {
            Family family = familyRepository.findById(resident.getFamilyId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
            int familyBalance = defaultFamilyPointBalance(family);
            int fromFamily = Math.min(remaining, familyBalance);
            if (fromFamily > 0) {
                family.setPointBalance(familyBalance - fromFamily);
                familyRepository.save(family);
                remaining -= fromFamily;
            }
        }

        if (remaining > 0) {
            int personalBalance = defaultPointBalance(resident);
            if (personalBalance < remaining) {
                throw new BizException(ErrorCode.POINT_INSUFFICIENT);
            }
            resident.setPointBalance(personalBalance - remaining);
            residentRepository.save(resident);
        }

        return createRecord(
                resident,
                resident.getFamilyId(),
                PointType.spend.name(),
                -amount,
                defaultPointBalance(resident),
                source,
                sourceId,
                description,
                operatorId);
    }

    private PointRecord createRecord(Resident resident,
                                     String familyId,
                                     String pointType,
                                     int amount,
                                     int balance,
                                     String source,
                                     String sourceId,
                                     String description,
                                     String operatorId) {
        PointRecord record = new PointRecord();
        record.setId(idGenerator.generateId("pt"));
        record.setResidentId(resident.getId());
        record.setFamilyId(familyId);
        record.setPropertyCompanyId(resident.getPropertyCompanyId());
        record.setPointType(pointType);
        record.setAmount(amount);
        record.setBalance(balance);
        record.setSource(source);
        record.setSourceId(sourceId);
        record.setDescription(description);
        record.setOperatorId(operatorId);
        return pointRecordRepository.save(record);
    }

    private void deductFromPersonal(Resident resident, int amount) {
        int balance = defaultPointBalance(resident);
        if (balance < amount) {
            throw new BizException(ErrorCode.POINT_INSUFFICIENT);
        }
        resident.setPointBalance(balance - amount);
        residentRepository.save(resident);
    }

    private void deductFromFamilyPool(String familyId, int amount) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
        int balance = defaultFamilyPointBalance(family);
        if (balance < amount) {
            throw new BizException(ErrorCode.POINT_INSUFFICIENT);
        }
        family.setPointBalance(balance - amount);
        familyRepository.save(family);
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

    private void assertCanAccessFee(Resident resident, PropertyFee fee) {
        if (resident.getId().equals(fee.getResidentId())) {
            return;
        }
        if (StringUtils.hasText(fee.getFamilyId())
                && fee.getFamilyId().equals(resident.getFamilyId())) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertFeePayable(PropertyFee fee) {
        if ("paid".equals(fee.getPaymentStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "账单已支付");
        }
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

    private String resolvePaymentStatus(BigDecimal amount, BigDecimal paidAmount, LocalDate dueDate) {
        if (paidAmount.compareTo(amount) >= 0) {
            return "paid";
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            return "partial";
        }
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            return "overdue";
        }
        return "unpaid";
    }

    private void validatePropertyFeeId(String propertyFeeId) {
        if (!StringUtils.hasText(propertyFeeId)) {
            throw new BizException(ErrorCode.PARAM_MISSING);
        }
        if (!propertyFeeId.startsWith("pf_") && !propertyFeeId.startsWith("fee_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyFeeId格式不正确");
        }
    }

    private void validatePointType(String pointType) {
        if (StringUtils.hasText(pointType) && !RoleGuard.POINT_TYPES.contains(pointType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "pointType值无效");
        }
    }

    private void validatePointSource(String source) {
        if (StringUtils.hasText(source) && !RoleGuard.POINT_SOURCES.contains(source)) {
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

    private int defaultPointBalance(Resident resident) {
        return resident.getPointBalance() != null ? resident.getPointBalance() : 0;
    }

    private int defaultFamilyPointBalance(Family family) {
        return family.getPointBalance() != null ? family.getPointBalance() : 0;
    }

    private int defaultPointDeducted(PropertyFee fee) {
        return fee.getPointDeducted() != null ? fee.getPointDeducted() : 0;
    }

    private BigDecimal defaultPaidAmount(PropertyFee fee) {
        return fee.getPaidAmount() != null ? fee.getPaidAmount() : BigDecimal.ZERO;
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
