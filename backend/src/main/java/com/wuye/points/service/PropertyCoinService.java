package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.coinfreeze.CoinFreezeRecordItemResponse;
import com.wuye.points.dto.coin.CoinBalanceResponse;
import com.wuye.points.dto.coin.CoinRecordItemResponse;
import com.wuye.points.dto.coin.CoinUseConditionResponse;
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
import com.wuye.points.dto.coin.RechargeStatusResponse;
import com.wuye.points.dto.coin.SpendCoinRequest;
import com.wuye.points.dto.coin.SpendCoinResponse;
import com.wuye.points.dto.coin.TransferCoinToWalletRequest;
import com.wuye.points.dto.coin.TransferCoinToWalletResponse;
import com.wuye.points.dto.coin.UnfreezeCoinRequest;
import com.wuye.points.dto.coin.UnfreezeCoinResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.TransferToPropertyCoinRequest;
import com.wuye.points.dto.resident.TransferToPropertyCoinResponse;
import com.wuye.points.entity.CoinFreezeRecord;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.CoinType;
import com.wuye.points.enums.CoinUseCondition;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.repository.CoinFreezeRecordRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PropertyCoinRecordSpecifications;
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

    /** 住户主动转给物业的流水来源标识，须与管理端对账查询（AdminTransferToPropertyService）保持一致。 */
    public static final String SOURCE_TRANSFER_TO_PROPERTY = "transfer_to_property";

    /** 奖励金转钱包的流水来源与钱包入账类型 */
    public static final String SOURCE_TRANSFER_TO_WALLET = "transfer_to_wallet";
    private static final String ENTRY_COIN_TRANSFER_TO_WALLET = "coin_transfer_to_wallet";

    private final PropertyCoinRecordRepository coinRecordRepository;
    private final CoinFreezeRecordRepository coinFreezeRecordRepository;
    private final ResidentRepository residentRepository;
    private final FamilyRepository familyRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PropertyFeeRepository propertyFeeRepository;
    private final PropertyCoinConverter coinConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final FeatureSwitchGuard featureSwitchGuard;
    private final PayChannel wechatPayService;
    private final PaymentRecordRepository paymentRecordRepository;
    private final WalletFlowWriter walletFlowWriter;
    private final WalletFlowRepository walletFlowRepository;
    private final PropertyCoinFundService propertyCoinFundService;

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

        List<Resident> members = residentRepository.findByFamilyId(family.getId());
        List<FamilyCoinBalanceResponse.Member> memberItems = members.stream()
                .filter(m -> ResidentStatus.active.name().equalsIgnoreCase(m.getStatus()))
                .map(m -> FamilyCoinBalanceResponse.Member.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .coinBalance(m.getCoinBalance() != null ? m.getCoinBalance() : BigDecimal.ZERO)
                        .relation(m.getFamilyRelation())
                        .build())
                .toList();

        return FamilyCoinBalanceResponse.builder()
                .familyId(family.getId())
                .familyName(family.getName())
                .coinBalance(defaultFamilyCoinBalance(family))
                .memberCount(memberItems.size())
                .members(memberItems)
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
        Resident resident = getActiveResident(UserContext.currentUserId());

        int coinExpiryDays = 365;
        if (StringUtils.hasText(resident.getPropertyCompanyId())) {
            PropertyCompany company = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                    .orElse(null);
            if (company != null && company.getCoinExpiryDays() != null && company.getCoinExpiryDays() > 0) {
                coinExpiryDays = company.getCoinExpiryDays();
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startWindow = now.minusDays(coinExpiryDays);
        LocalDateTime endWindow = now.plusDays(30).minusDays(coinExpiryDays);

        BigDecimal expiringSoon = coinRecordRepository.sumCoinsExpiringBetween(
                resident.getId(), startWindow, endWindow);

        if (expiringSoon == null) {
            expiringSoon = BigDecimal.ZERO;
        }

        BigDecimal balance = defaultCoinBalance(resident);
        if (expiringSoon.compareTo(balance) > 0) {
            expiringSoon = balance;
        }

        if (expiringSoon.compareTo(BigDecimal.ZERO) <= 0) {
            return ExpiringCoinResponse.builder()
                    .expiringSoon(BigDecimal.ZERO)
                    .expiryDate(null)
                    .build();
        }

        LocalDateTime expiryDate = endWindow.plusDays(coinExpiryDays);
        return ExpiringCoinResponse.builder()
                .expiringSoon(expiringSoon)
                .expiryDate(DateTimeUtil.toIsoUtc(expiryDate))
                .build();
    }

    /**
     * 物业币充值预下单。
     *
     * <p>充值不再直接加余额：先创建支付流水并调微信 JSAPI 预下单，返给前端 {@code payParams}；
     * 用户支付成功后由微信回调（{@code PaymentBizDispatcher}）调用 {@link #completeChannelPayment} 到账。
     * 前端用 {@code GET /property-coins/recharge/{paymentRecordId}} 轮询到账状态。</p>
     */
    @Transactional
    public RechargeCoinResponse recharge(RechargeCoinRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        assertCoinNotFrozen(resident);
        assertCoinDisplayEnabled(resident);

        if (!wechatPayService.isDirectEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "在线支付未开通，暂时无法充值物业币");
        }
        if (!StringUtils.hasText(resident.getOpenid())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "openid不能为空（微信小程序支付需要）");
        }
        if (!paymentRecordRepository.findByBizTypeAndResidentIdAndStatus(
                PaymentBizType.COIN_RECHARGE, resident.getId(), "pending").isEmpty()) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "充值支付处理中，请勿重复提交");
        }

        BigDecimal amount = request.getAmount();
        PaymentRecord record = wechatPayService.createIntent(
                PaymentBizType.COIN_RECHARGE,
                resident.getId(),
                resident.getId(),
                resident.getPropertyCompanyId(),
                "wechat",
                amount,
                0,
                BigDecimal.ZERO,
                amount);
        WechatJsPayParams payParams = wechatPayService.miniPay(record, resident.getOpenid(), "物业币充值");

        return RechargeCoinResponse.builder()
                .paymentRecordId(record.getId())
                .outTradeNo(record.getOutTradeNo())
                .status("pending")
                .payParams(payParams)
                .amount(amount)
                .residentId(resident.getId())
                .build();
    }

    /**
     * 查询充值支付状态。前端拉起微信支付后轮询本接口，只有 status=paid 且 rechargedAt 非空才视为到账。
     */
    @Transactional(readOnly = true)
    public RechargeStatusResponse getRechargeStatus(String paymentRecordId) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        PaymentRecord record = paymentRecordRepository.findById(paymentRecordId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "充值流水不存在"));
        if (!PaymentBizType.COIN_RECHARGE.equals(record.getBizType())
                || !resident.getId().equals(record.getResidentId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }

        PropertyCoinRecord coinRecord = coinRecordRepository
                .findFirstByResidentIdAndSourceIdAndCoinTypeOrderByCreatedAtDesc(
                        resident.getId(), record.getId(), CoinType.recharge.name())
                .orElse(null);
        BigDecimal familyBalance = resolveFamilyBalanceAfter(resident);

        return RechargeStatusResponse.builder()
                .paymentRecordId(record.getId())
                .outTradeNo(record.getOutTradeNo())
                .status(coinRecord != null ? "paid" : record.getStatus())
                .amount(record.getAmount())
                .balanceAfter(coinRecord != null ? coinRecord.getBalance() : null)
                .familyBalanceAfter(coinRecord != null ? familyBalance : null)
                .rechargedAt(coinRecord != null ? DateTimeUtil.toIsoUtc(coinRecord.getCreatedAt()) : null)
                .failReason(record.getErrorMsg())
                .build();
    }

    /**
     * 微信支付回调确认后物业币充值到账（幂等）。由 PaymentBizDispatcher 调用，无登录上下文。
     * 以源支付流水ID作为幂等键：同一笔支付重复投递只入账一次。
     */
    @Transactional
    public void completeChannelPayment(PaymentRecord record) {
        if (record == null || !StringUtils.hasText(record.getResidentId())) {
            return;
        }
        BigDecimal amount = record.getAmount() != null ? record.getAmount() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (coinRecordRepository.existsByResidentIdAndSourceIdAndCoinType(
                record.getResidentId(), record.getId(), CoinType.recharge.name())) {
            return;
        }
        Resident resident = residentRepository.findByIdForUpdate(record.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        // 充值到账不受 coin_frozen 限制：用户已付款，冻结只限制消费，不能阻止入账。
        creditCoins(resident, amount, CoinType.recharge.name(), "manual",
                record.getId(), "物业币充值", null);
        // 充值现金进入平台直连商户号，按物业公司登记代管资金，供商家物业币应收结算出账。
        propertyCoinFundService.recordRecharge(record);
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

    /**
     * 当前物业币使用条件（§79.1 GET /property-coins/use-condition）。
     *
     * <p>条件来自物业公司配置（§79.2 设置）：
     * none / point_threshold（积分门槛）/ property_fee_paid（物业费已缴清）/ manual_issue（仅物业配发物业币可用）。
     * 未配置时按 {@code none} 处理，保证前端可展示、不因配置缺失而报错。</p>
     */
    @Transactional(readOnly = true)
    public CoinUseConditionResponse getUseCondition() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        String conditionCode = CoinUseCondition.NONE.getCode();
        Integer pointThreshold = 0;
        if (StringUtils.hasText(resident.getPropertyCompanyId())) {
            PropertyCompany company = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                    .orElse(null);
            if (company != null && StringUtils.hasText(company.getCoinUseCondition())) {
                conditionCode = company.getCoinUseCondition();
                pointThreshold = company.getCoinPointThreshold() != null
                        ? company.getCoinPointThreshold() : 0;
            }
        }

        boolean satisfied = resolveUseConditionSatisfied(resident, conditionCode, pointThreshold);
        boolean frozen = Boolean.TRUE.equals(resident.getCoinFrozen());
        return CoinUseConditionResponse.builder()
                .condition(conditionCode)
                .pointThreshold(conditionCode.equals(CoinUseCondition.POINT_THRESHOLD.getCode())
                        ? pointThreshold : null)
                .satisfied(satisfied)
                .eligible(satisfied && !frozen)
                .build();
    }

    private boolean resolveUseConditionSatisfied(Resident resident, String conditionCode, int pointThreshold) {
        CoinUseCondition condition = CoinUseCondition.fromCode(conditionCode);
        return switch (condition) {
            case NONE -> true;
            case POINT_THRESHOLD -> {
                int balance = resident.getPointBalance() != null ? resident.getPointBalance() : 0;
                yield balance >= pointThreshold;
            }
            case PROPERTY_FEE_PAID -> propertyFeeRepository
                    .countByResidentIdAndPaymentStatusNotIn(resident.getId()) == 0;
            case MANUAL_ISSUE -> coinRecordRepository
                    .existsByResidentIdAndSourceAndCoinType(resident.getId(), "manual", CoinType.earn.name());
        };
    }

    @Transactional(readOnly = true)
    public PageResponse<CoinFreezeRecordItemResponse> listFreezeRecords(
            int page, int pageSize, String residentId, String action,
            String propertyCompanyId, String sort) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(residentId) && !residentId.startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }

        String scopedPropertyCompanyId = roleGuard.resolveListPropertyCompanyId(propertyCompanyId);
        String queryAction = StringUtils.hasText(action) ? action : "freeze";

        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, sortObj);

        Page<CoinFreezeRecord> recordPage;
        if (StringUtils.hasText(scopedPropertyCompanyId)) {
            recordPage = coinFreezeRecordRepository.findByPropertyCompanyIdAndAction(
                    scopedPropertyCompanyId, queryAction, pageable);
        } else {
            recordPage = coinFreezeRecordRepository.findByAction(queryAction, pageable);
        }

        List<CoinFreezeRecordItemResponse> list = recordPage.getContent().stream()
                .map(record -> {
                    Resident resident = residentRepository.findById(record.getResidentId()).orElse(null);
                    return CoinFreezeRecordItemResponse.builder()
                            .id(record.getId())
                            .residentId(record.getResidentId())
                            .residentName(resident != null ? resident.getName() : null)
                            .residentPhone(resident != null ? resident.getPhone() : null)
                            .residentBuilding(resident != null ? resident.getBuilding() : null)
                            .residentRoom(resident != null ? resident.getRoom() : null)
                            .action(record.getAction())
                            .amount(record.getAmount() != null ? record.getAmount().toPlainString() : null)
                            .reason(record.getReason())
                            .operatorId(record.getOperatorId())
                            .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                            .build();
                })
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
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
     * 供其他模块调用的物业币获取。
     *
     * <p>记账边界（2026-10-25 调整）：物业币发放统一进入<b>个人余额</b>，不再进入家庭池；
     * 家庭池仅保留历史余额，消费时仍可按 {@code useFamilyPool} 先扣家庭池、不足再扣个人余额。</p>
     */
    @Transactional
    public PropertyCoinRecord earnCoins(Resident resident,
                                        BigDecimal amount,
                                        String coinType,
                                        String source,
                                        String sourceId,
                                        String description,
                                        String operatorId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "物业币变动数量必须大于0");
        }
        Resident current = residentRepository.findByIdForUpdate(resident.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        assertCoinNotFrozen(current);
        return creditCoins(current, amount, coinType, source, sourceId, description, operatorId);
    }

    /**
     * 入账核心逻辑（调用方已完成加锁与幂等判断）：统一入个人余额。
     * 充值到账不经过冻结校验：冻结限制的是消费，不应阻止已付款的充值到账。
     */
    private PropertyCoinRecord creditCoins(Resident current,
                                           BigDecimal amount,
                                           String coinType,
                                           String source,
                                           String sourceId,
                                           String description,
                                           String operatorId) {
        BigDecimal newBalance = defaultCoinBalance(current).add(amount);
        current.setCoinBalance(newBalance);
        residentRepository.save(current);
        return createRecord(
                current,
                null,
                coinType,
                amount,
                newBalance,
                BigDecimal.ZERO,
                null,
                source,
                sourceId,
                description,
                operatorId);
    }

    /**
     * 住户主动将家庭物业币转赠给物业。
     *
     * <p>账户边界：仅<b>家庭物业币池</b>可转给物业公司；个人物业币不得转赠（个人名义持有、不从集体划转）。
     * 无家庭或家庭余额不足时将拒绝。扣减家庭余额，记 consume 流水。</p>
     */
    @Transactional
    public TransferToPropertyCoinResponse transferToProperty(TransferToPropertyCoinRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!ResidentStatus.active.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        featureSwitchGuard.assertCoinEnabled(resident.getPropertyCompanyId());
        assertCoinNotFrozen(resident);

        String familyId = resident.getFamilyId();
        if (!StringUtils.hasText(familyId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "您尚未加入家庭，无家庭物业币可转赠物业");
        }
        Family family = familyRepository.findByIdForUpdate(familyId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
        Resident current = residentRepository.findByIdForUpdate(resident.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!familyId.equals(current.getFamilyId())) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "家庭状态已变更，请重试");
        }
        assertCoinNotFrozen(current);

        BigDecimal amount = request.getCoinAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "转赠金额必须大于0");
        }
        BigDecimal familyBalance = defaultFamilyCoinBalance(family);
        if (familyBalance.compareTo(amount) < 0) {
            throw new BizException(ErrorCode.COIN_INSUFFICIENT, "家庭物业币余额不足，无法转赠物业");
        }

        BigDecimal newFamilyBalance = familyBalance.subtract(amount);
        family.setCoinBalance(newFamilyBalance);
        familyRepository.save(family);

        PropertyCoinRecord record = createRecord(
                current, familyId, CoinType.spend.name(), amount.negate(),
                defaultCoinBalance(current), amount.negate(), newFamilyBalance,
                SOURCE_TRANSFER_TO_PROPERTY, current.getId(),
                request.getRemark() != null ? request.getRemark() : "住户家庭物业币转赠物业", null);
        return TransferToPropertyCoinResponse.builder()
                .recordId(record.getId())
                .residentId(resident.getId())
                .coinAmount(amount)
                .balanceAfter(newFamilyBalance)
                .transferredAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    /**
     * 奖励金转钱包（v8.21）：仅从个人奖励金转出，不动家庭池；转出后奖励金减少、钱包增加，不可撤销。
     */
    @Transactional
    public TransferCoinToWalletResponse transferToWallet(TransferCoinToWalletRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        featureSwitchGuard.assertCoinEnabled(resident.getPropertyCompanyId());
        assertCoinNotFrozen(resident);

        BigDecimal amount = request.getCoinAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "转出金额必须大于0");
        }
        Resident current = residentRepository.findByIdForUpdate(resident.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        BigDecimal balance = defaultCoinBalance(current);
        if (balance.compareTo(amount) < 0) {
            throw new BizException(ErrorCode.COIN_INSUFFICIENT, "个人奖励金余额不足");
        }
        BigDecimal newBalance = balance.subtract(amount);
        current.setCoinBalance(newBalance);
        residentRepository.save(current);

        PropertyCoinRecord record = createRecord(
                current, null, CoinType.spend.name(), amount.negate(), newBalance,
                SOURCE_TRANSFER_TO_WALLET, current.getId(), "奖励金转钱包", current.getId());

        // 与奖励金扣减同事务：避免扣减回滚后钱包流水已独立提交，形成无对应扣款的余额。
        walletFlowWriter.writeInCurrentTransaction(null, null, null,
                "resident", current.getId(), ENTRY_COIN_TRANSFER_TO_WALLET,
                amount, "withdrawable",
                "coin:transfer_to_wallet:" + record.getId(), null);

        BigDecimal walletBalance = walletFlowRepository
                .sumAmountByOwnerIdAndBalanceType(current.getId(), "withdrawable");

        return TransferCoinToWalletResponse.builder()
                .recordId(record.getId())
                .coinBalance(newBalance)
                .walletBalance(walletBalance != null ? walletBalance : BigDecimal.ZERO)
                .build();
    }

    /**
     * 供其他模块调用的物业币消费（useFamilyPool=true 时先扣家庭池，不足再扣个人余额）。
     * 家庭池与个人余额分别加行锁，避免并发读改写导致双花。
     */
    @Transactional
    public PropertyCoinRecord spendCoins(Resident resident,
                                         BigDecimal amount,
                                         String source,
                                         String sourceId,
                                         String description,
                                         String operatorId,
                                         boolean useFamilyPool) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "物业币变动数量必须大于0");
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
        assertCoinNotFrozen(current);

        BigDecimal remaining = amount;
        BigDecimal fromFamily = BigDecimal.ZERO;
        BigDecimal familyBalanceAfter = null;
        if (family != null) {
            BigDecimal familyBalance = defaultFamilyCoinBalance(family);
            fromFamily = remaining.min(familyBalance);
            if (fromFamily.compareTo(BigDecimal.ZERO) > 0) {
                familyBalanceAfter = familyBalance.subtract(fromFamily);
                family.setCoinBalance(familyBalanceAfter);
                familyRepository.save(family);
                remaining = remaining.subtract(fromFamily);
            } else {
                familyBalanceAfter = familyBalance;
            }
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal personalBalance = defaultCoinBalance(current);
            if (personalBalance.compareTo(remaining) < 0) {
                throw new BizException(ErrorCode.COIN_INSUFFICIENT);
            }
            current.setCoinBalance(personalBalance.subtract(remaining));
            residentRepository.save(current);
        }

        return createRecord(
                current,
                family != null ? familyId : current.getFamilyId(),
                CoinType.spend.name(),
                amount.negate(),
                defaultCoinBalance(current),
                fromFamily.negate(),
                familyBalanceAfter,
                source,
                sourceId,
                description,
                operatorId);
    }

    /**
     * 订单取消/退款时退回物业币：按原支付流水的家庭/个人扣减比例原路退回。
     */
    @Transactional
    public void refundOrderCoins(Resident resident, String orderId, BigDecimal amount, String orderFamilyId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        PropertyCoinRecord spend = coinRecordRepository
                .findFirstBySourceIdAndSourceAndCoinTypeOrderByCreatedAtDesc(orderId, "order", CoinType.spend.name())
                .orElse(null);
        BigDecimal fromFamily = BigDecimal.ZERO;
        if (spend != null && spend.getFamilyAmount() != null
                && spend.getFamilyAmount().compareTo(BigDecimal.ZERO) < 0) {
            fromFamily = amount.min(spend.getFamilyAmount().negate());
        } else if (spend == null
                && (StringUtils.hasText(orderFamilyId) || StringUtils.hasText(resident.getFamilyId()))) {
            fromFamily = amount;
        }

        String familyId = StringUtils.hasText(orderFamilyId) ? orderFamilyId : resident.getFamilyId();
        Family family = null;
        Resident current;
        if (fromFamily.compareTo(BigDecimal.ZERO) > 0 && StringUtils.hasText(familyId)) {
            family = familyRepository.findByIdForUpdate(familyId).orElse(null);
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
            if (family == null) {
                fromFamily = BigDecimal.ZERO;
            }
        } else {
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        }

        BigDecimal toPersonal = amount.subtract(fromFamily);
        BigDecimal familyBalanceAfter = null;
        if (family != null && fromFamily.compareTo(BigDecimal.ZERO) > 0) {
            familyBalanceAfter = defaultFamilyCoinBalance(family).add(fromFamily);
            family.setCoinBalance(familyBalanceAfter);
            familyRepository.save(family);
        }
        if (toPersonal.compareTo(BigDecimal.ZERO) > 0) {
            current.setCoinBalance(defaultCoinBalance(current).add(toPersonal));
            residentRepository.save(current);
        }

        createRecord(current, family != null ? familyId : current.getFamilyId(),
                CoinType.earn.name(), amount, defaultCoinBalance(current),
                fromFamily, familyBalanceAfter, "order", orderId, "订单取消退回物业币", null);
    }

    /**
     * 订单取消/退款时扣回已返还的物业币：按原返币流水的家庭/个人入账比例原路扣回。
     */
    @Transactional
    public void reverseOrderEarnedCoins(Resident resident, String orderId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        PropertyCoinRecord earn = coinRecordRepository
                .findFirstBySourceIdAndSourceAndCoinTypeOrderByCreatedAtDesc(orderId, "order", CoinType.earn.name())
                .orElse(null);
        BigDecimal familyPortion = BigDecimal.ZERO;
        if (earn != null && earn.getFamilyAmount() != null
                && earn.getFamilyAmount().compareTo(BigDecimal.ZERO) > 0) {
            familyPortion = amount.min(earn.getFamilyAmount());
        } else if (earn == null && StringUtils.hasText(resident.getFamilyId())) {
            familyPortion = amount;
        }

        String familyId = resident.getFamilyId();
        Family family = null;
        Resident current;
        if (familyPortion.compareTo(BigDecimal.ZERO) > 0 && StringUtils.hasText(familyId)) {
            family = familyRepository.findByIdForUpdate(familyId).orElse(null);
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        } else {
            current = residentRepository.findByIdForUpdate(resident.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        }

        BigDecimal remaining = amount;
        BigDecimal fromFamily = BigDecimal.ZERO;
        BigDecimal familyBalanceAfter = null;
        if (family != null && familyPortion.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal familyBalance = defaultFamilyCoinBalance(family);
            fromFamily = remaining.min(familyPortion.min(familyBalance));
            if (fromFamily.compareTo(BigDecimal.ZERO) > 0) {
                familyBalanceAfter = familyBalance.subtract(fromFamily);
                family.setCoinBalance(familyBalanceAfter);
                familyRepository.save(family);
                remaining = remaining.subtract(fromFamily);
            } else {
                familyBalanceAfter = familyBalance;
            }
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal personalBalance = defaultCoinBalance(current);
            if (personalBalance.compareTo(remaining) < 0) {
                throw new BizException(ErrorCode.COIN_INSUFFICIENT);
            }
            current.setCoinBalance(personalBalance.subtract(remaining));
            residentRepository.save(current);
        }

        createRecord(current, family != null ? familyId : current.getFamilyId(),
                CoinType.spend.name(), amount.negate(), defaultCoinBalance(current),
                fromFamily.negate(), familyBalanceAfter, "order", orderId, "订单取消扣回物业币返现", null);
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
        return createRecord(resident, familyId, coinType, amount, balance, null, null,
                source, sourceId, description, operatorId);
    }

    private PropertyCoinRecord createRecord(Resident resident,
                                            String familyId,
                                            String coinType,
                                            BigDecimal amount,
                                            BigDecimal balance,
                                            BigDecimal familyAmount,
                                            BigDecimal familyBalance,
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
        record.setFamilyAmount(familyAmount);
        record.setFamilyBalance(familyBalance);
        record.setExpiredAmount(BigDecimal.ZERO);
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
