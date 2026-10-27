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

        PropertyCompany company = StringUtils.hasText(resident.getPropertyCompanyId())
                ? propertyCompanyRepository.findById(resident.getPropertyCompanyId()).orElse(null)
                : null;
        PointTransferPolicy.Decision transfer = PointTransferPolicy.evaluate(company, resident);

        return PointBalanceResponse.builder()
                .pointBalance(defaultPointBalance(resident))
                .familyPointBalance(familyPointBalance)
                .familyId(familyId)
                .todayEarned(todayEarned != null ? todayEarned : 0)
                .todaySpent(todaySpent != null ? todaySpent : 0)
                .expiringPoints(0)
                .expiringDate(null)
                .pointTransferEnabled(transfer.enabled())
                .pointTransferMinYears(transfer.minYears())
                .pointTransferEligible(transfer.eligible())
                .pointTransferBlockedReason(transfer.blockedReason())
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
        // v8.23：家庭内转赠历史流水 source=transfer；兼容前端旧参数 family_transfer，不改历史数据
        String scopedSource = "family_transfer".equals(source) ? "transfer" : source;
        validatePointSource(scopedSource);

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
                        scopedSource,
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
        Resident fromSnapshot = getActiveResident(fromMemberId);
        if (!request.getFamilyId().equals(fromSnapshot.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员");
        }
        if (fromMemberId.equals(request.getToMemberId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "不能向自己转移积分");
        }

        // 锁顺序固定：家庭 -> 两个住户（按ID升序），避免与其它家庭操作互相死锁。
        Family family = familyRepository.findByIdForUpdate(request.getFamilyId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
        String firstId = fromMemberId.compareTo(request.getToMemberId()) < 0
                ? fromMemberId : request.getToMemberId();
        String secondId = firstId.equals(fromMemberId) ? request.getToMemberId() : fromMemberId;
        Resident first = residentRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        Resident second = residentRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        Resident fromMember = firstId.equals(fromMemberId) ? first : second;
        Resident toMember = firstId.equals(request.getToMemberId()) ? first : second;
        if (!request.getFamilyId().equals(fromMember.getFamilyId())
                || !request.getFamilyId().equals(toMember.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非家庭成员");
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

        int familyBalance = defaultFamilyPointBalance(family);
        PointRecord record = createRecord(
                fromMember,
                family.getId(),
                PointType.transfer.name(),
                -request.getAmount(),
                fromMember.getPointBalance(),
                0,
                familyBalance,
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
                0,
                familyBalance,
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
        }
        // 统一走 spendPoints：行锁 + 家庭池/个人池扣减 + 流水快照一次完成，避免与订单支付并发双花。
        PointRecord record = spendPoints(resident, request.getPoints(), "fee_deduct", fee.getId(),
                "积分抵扣物业费", null, useFamilyPool);

        fee.setPointDeducted(defaultPointDeducted(fee) + request.getPoints());
        fee.setPaidAmount(defaultPaidAmount(fee).add(deductedAmount));
        fee.setPaymentStatus(resolvePaymentStatus(fee.getAmount(), fee.getPaidAmount(), fee.getDueDate()));
        if ("paid".equals(fee.getPaymentStatus())) {
            fee.setPaidAt(LocalDateTime.now());
        }
        propertyFeeRepository.save(fee);

        return DeductFeeResponse.builder()
                .propertyFeeId(fee.getId())
                .pointsUsed(request.getPoints())
                .deductedAmount(deductedAmount)
                .remainingAmount(fee.getAmount().subtract(fee.getPaidAmount()))
                .paymentStatus(fee.getPaymentStatus())
                .residentBalance(record.getBalance())
                .familyBalance(record.getFamilyBalance())
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
        PointRecord record = spendPoints(resident, request.getPoints(), "manual", null,
                "积分兑换", null, useFamilyPool);

        return RedeemPointResponse.builder()
                .pointsUsed(request.getPoints())
                .redeemedAmount(redeemedAmount)
                .balanceAfter(record.getBalance())
                .familyBalanceAfter(record.getFamilyBalance())
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
     * 供其他模块调用的积分获取。
     *
     * <p>记账边界：住户有家庭时，积分只进入<b>家庭池</b>，个人余额不变；无家庭时才进入个人余额。
     * 家庭池与个人余额是两个独立账户，不能同时入账，否则同一笔积分会被花两次。</p>
     */
    @Transactional
    public PointRecord earnPoints(Resident resident,
                                  int amount,
                                  String source,
                                  String sourceId,
                                  String description,
                                  String operatorId) {
        if (amount <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "积分变动数量必须大于0");
        }
        LockedFamily locked = lockResidentAndFamily(resident);
        Resident current = locked.resident();

        if (locked.family() != null) {
            int newFamilyBalance = defaultFamilyPointBalance(locked.family()) + amount;
            locked.family().setPointBalance(newFamilyBalance);
            familyRepository.save(locked.family());
            return createRecord(
                    current,
                    locked.familyId(),
                    PointType.earn.name(),
                    amount,
                    defaultPointBalance(current),
                    amount,
                    newFamilyBalance,
                    source,
                    sourceId,
                    description,
                    operatorId);
        }

        int newBalance = defaultPointBalance(current) + amount;
        current.setPointBalance(newBalance);
        residentRepository.save(current);
        return createRecord(
                current,
                null,
                PointType.earn.name(),
                amount,
                newBalance,
                0,
                null,
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

        // v8.21：积分转物业需物业公司开启，且入住满配置年限（默认 5 年）；未开启时一律不可转
        PropertyCompany company = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        PointTransferPolicy.Decision transfer = PointTransferPolicy.evaluate(company, resident);
        if (!transfer.eligible()) {
            throw new BizException(ErrorCode.PARAM_INVALID, transfer.blockedReason());
        }

        List<PropertyFee> payableFees = propertyFeeRepository
                .findByResidentIdAndPaymentStatusInOrderByDueDateAsc(
                        resident.getId(), List.of("unpaid", "partial", "overdue"));
        if (payableFees.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "暂无待缴物业费，积分暂无可抵扣账单");
        }

        PointRecord record = spendPoints(resident, amount, SOURCE_TRANSFER_TO_PROPERTY, resident.getId(),
                request.getRemark() != null ? request.getRemark() : "住户积分转赠物业", null, false);

        BigDecimal rate = resolvePointToFeeRate(resident.getPropertyCompanyId());
        BigDecimal appliedAmount = applyPointsToFees(payableFees, amount, rate);

        return TransferToPropertyPointResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .pointAmount(amount)
                .balanceAfter(record.getBalance())
                .transferredAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .appliedFeeAmount(appliedAmount)
                .build();
    }

    /**
     * 按账期从早到晚冲抵物业费账单，返回实际抵扣金额。
     * 与 deductFee 保持同一回写口径：paidAmount/pointDeducted/paymentStatus/paidAt。
     */
    private BigDecimal applyPointsToFees(List<PropertyFee> fees, int points, BigDecimal rate) {
        int remainingPoints = points;
        BigDecimal applied = BigDecimal.ZERO;
        for (PropertyFee fee : fees) {
            if (remainingPoints <= 0) {
                break;
            }
            BigDecimal paid = defaultPaidAmount(fee);
            BigDecimal billRemaining = fee.getAmount().subtract(paid);
            if (billRemaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            int need = billRemaining.divide(rate, 0, RoundingMode.CEILING).intValue();
            int use = Math.min(remainingPoints, Math.max(need, 1));
            BigDecimal amount = rate.multiply(BigDecimal.valueOf(use)).min(billRemaining)
                    .setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            fee.setPaidAmount(paid.add(amount));
            fee.setPointDeducted(defaultPointDeducted(fee) + use);
            fee.setPaymentStatus(resolvePaymentStatus(fee.getAmount(), fee.getPaidAmount(), fee.getDueDate()));
            if ("paid".equals(fee.getPaymentStatus())) {
                fee.setPaidAt(LocalDateTime.now());
            }
            propertyFeeRepository.save(fee);
            remainingPoints -= use;
            applied = applied.add(amount);
        }
        return applied.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 供其他模块调用的积分消费。
     *
     * <p>useFamilyPool=true 时先扣家庭池，不足部分再扣个人余额；false 时只扣个人余额。
     * 家庭池与个人余额分别加行锁，避免并发读改写导致双花。</p>
     */
    @Transactional
    public PointRecord spendPoints(Resident resident,
                                   int amount,
                                   String source,
                                   String sourceId,
                                   String description,
                                   String operatorId,
                                   boolean useFamilyPool) {
        if (amount <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "积分变动数量必须大于0");
        }
        String familyId = useFamilyPool ? resident.getFamilyId() : null;
        Family family = null;
        Resident current;
        if (StringUtils.hasText(familyId)) {
            family = familyRepository.findByIdForUpdate(familyId)
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
            if (!familyId.equals(current.getFamilyId())) {
                throw new BizException(ErrorCode.ORDER_CONFLICT, "家庭状态已变更，请重试");
            }
        } else {
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        }

        int remaining = amount;
        int fromFamily = 0;
        Integer familyBalanceAfter = null;
        if (family != null) {
            int familyBalance = defaultFamilyPointBalance(family);
            fromFamily = Math.min(remaining, familyBalance);
            if (fromFamily > 0) {
                familyBalanceAfter = familyBalance - fromFamily;
                family.setPointBalance(familyBalanceAfter);
                familyRepository.save(family);
                remaining -= fromFamily;
            } else {
                familyBalanceAfter = familyBalance;
            }
        }

        if (remaining > 0) {
            int personalBalance = defaultPointBalance(current);
            if (personalBalance < remaining) {
                throw new BizException(ErrorCode.POINT_INSUFFICIENT);
            }
            current.setPointBalance(personalBalance - remaining);
            residentRepository.save(current);
        }

        return createRecord(
                current,
                family != null ? familyId : current.getFamilyId(),
                PointType.spend.name(),
                -amount,
                defaultPointBalance(current),
                -fromFamily,
                familyBalanceAfter,
                source,
                sourceId,
                description,
                operatorId);
    }

    /**
     * 订单取消/退款时退回积分：按原支付流水的家庭/个人扣减比例原路退回。
     * 旧数据没有 family_amount 时，若订单有 familyId 则回家庭池，否则回个人池。
     */
    @Transactional
    public void refundOrderPoints(Resident resident, String orderId, int amount, String orderFamilyId) {
        if (amount <= 0) {
            return;
        }
        PointRecord spend = pointRecordRepository
                .findFirstBySourceIdAndSourceAndPointTypeOrderByCreatedAtDesc(orderId, "order", "spend")
                .orElse(null);
        int fromFamily = 0;
        if (spend != null && spend.getFamilyAmount() != null && spend.getFamilyAmount() < 0) {
            fromFamily = Math.min(amount, -spend.getFamilyAmount());
        } else if (spend == null
                && (StringUtils.hasText(orderFamilyId) || StringUtils.hasText(resident.getFamilyId()))) {
            fromFamily = amount;
        }

        String familyId = StringUtils.hasText(orderFamilyId) ? orderFamilyId : resident.getFamilyId();
        Family family = null;
        Resident current;
        if (fromFamily > 0 && StringUtils.hasText(familyId)) {
            family = familyRepository.findByIdForUpdate(familyId).orElse(null);
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
            if (family == null) {
                fromFamily = 0;
            }
        } else {
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        }

        int toPersonal = amount - fromFamily;
        Integer familyBalanceAfter = null;
        if (family != null && fromFamily > 0) {
            familyBalanceAfter = defaultFamilyPointBalance(family) + fromFamily;
            family.setPointBalance(familyBalanceAfter);
            familyRepository.save(family);
        }
        if (toPersonal > 0) {
            current.setPointBalance(defaultPointBalance(current) + toPersonal);
            residentRepository.save(current);
        }

        createRecord(current, family != null ? familyId : current.getFamilyId(),
                PointType.earn.name(), amount, defaultPointBalance(current),
                fromFamily, familyBalanceAfter, "order", orderId, "订单取消退回积分", null);
    }

    /**
     * 订单取消/退款时扣回已返还的积分：按原返积分流水的家庭/个人入账比例原路扣回。
     */
    @Transactional
    public void reverseOrderEarnedPoints(Resident resident, String orderId, int amount) {
        if (amount <= 0) {
            return;
        }
        PointRecord earn = pointRecordRepository
                .findFirstBySourceIdAndSourceAndPointTypeOrderByCreatedAtDesc(orderId, "order", "earn")
                .orElse(null);
        int familyPortion = 0;
        if (earn != null && earn.getFamilyAmount() != null && earn.getFamilyAmount() > 0) {
            familyPortion = Math.min(amount, earn.getFamilyAmount());
        } else if (earn == null && StringUtils.hasText(resident.getFamilyId())) {
            familyPortion = amount;
        }

        String familyId = resident.getFamilyId();
        Family family = null;
        Resident current;
        if (familyPortion > 0 && StringUtils.hasText(familyId)) {
            family = familyRepository.findByIdForUpdate(familyId).orElse(null);
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        } else {
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        }

        int remaining = amount;
        int fromFamily = 0;
        Integer familyBalanceAfter = null;
        if (family != null && familyPortion > 0) {
            int familyBalance = defaultFamilyPointBalance(family);
            fromFamily = Math.min(remaining, Math.min(familyPortion, familyBalance));
            if (fromFamily > 0) {
                familyBalanceAfter = familyBalance - fromFamily;
                family.setPointBalance(familyBalanceAfter);
                familyRepository.save(family);
                remaining -= fromFamily;
            } else {
                familyBalanceAfter = familyBalance;
            }
        }
        if (remaining > 0) {
            int personalBalance = defaultPointBalance(current);
            if (personalBalance < remaining) {
                throw new BizException(ErrorCode.POINT_INSUFFICIENT);
            }
            current.setPointBalance(personalBalance - remaining);
            residentRepository.save(current);
        }

        createRecord(current, family != null ? familyId : current.getFamilyId(),
                PointType.spend.name(), -amount, defaultPointBalance(current),
                -fromFamily, familyBalanceAfter, "order", orderId, "订单取消扣回返积分", null);
    }

    /**
     * 家庭场景统一锁顺序：先锁家庭、再锁住户。无家庭时只锁住户。
     * 若读取家庭ID后被并发修改，抛 ORDER_CONFLICT 让上层重试，避免锁顺序反转导致死锁。
     */
    private LockedFamily lockResidentAndFamily(Resident resident) {
        String familyId = resident.getFamilyId();
        if (!StringUtils.hasText(familyId)) {
            Resident locked = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
            if (StringUtils.hasText(locked.getFamilyId())) {
                throw new BizException(ErrorCode.ORDER_CONFLICT, "家庭状态已变更，请重试");
            }
            return new LockedFamily(null, locked, null);
        }
        Family family = familyRepository.findByIdForUpdate(familyId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
        Resident locked = residentRepository.findByIdForUpdate(resident.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!familyId.equals(locked.getFamilyId())) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "家庭状态已变更，请重试");
        }
        return new LockedFamily(family, locked, familyId);
    }

    private record LockedFamily(Family family, Resident resident, String familyId) {
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
        return createRecord(resident, familyId, pointType, amount, balance, null, null,
                source, sourceId, description, operatorId);
    }

    private PointRecord createRecord(Resident resident,
                                     String familyId,
                                     String pointType,
                                     int amount,
                                     int balance,
                                     Integer familyAmount,
                                     Integer familyBalance,
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
        record.setFamilyAmount(familyAmount);
        record.setFamilyBalance(familyBalance);
        record.setSource(source);
        record.setSourceId(sourceId);
        record.setDescription(description);
        record.setOperatorId(operatorId);
        return pointRecordRepository.save(record);
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
