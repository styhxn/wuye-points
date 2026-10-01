package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.canteen.DirectedBalanceResponse;
import com.wuye.points.dto.canteen.DirectedFlowResponse;
import com.wuye.points.dto.canteen.DirectedRechargeRequest;
import com.wuye.points.dto.canteen.DirectedRechargeResponse;
import com.wuye.points.dto.canteen.MerchantQuotaBalanceResponse;
import com.wuye.points.dto.canteen.QuotaPurchaseRequest;
import com.wuye.points.dto.canteen.QuotaPurchaseResponse;
import com.wuye.points.dto.canteen.QuotaQuoteRequest;
import com.wuye.points.dto.canteen.QuotaQuoteResponse;
import com.wuye.points.dto.canteen.QuotaRecordResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.CanteenDirectedBalance;
import com.wuye.points.entity.CanteenDirectedFlow;
import com.wuye.points.entity.CanteenDirectedRecharge;
import com.wuye.points.entity.CanteenMerchantBinding;
import com.wuye.points.entity.CanteenQuotaPurchase;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.repository.CanteenDirectedBalanceRepository;
import com.wuye.points.repository.CanteenDirectedFlowRepository;
import com.wuye.points.repository.CanteenDirectedRechargeRepository;
import com.wuye.points.repository.CanteenMerchantBindingRepository;
import com.wuye.points.repository.CanteenQuotaPurchaseRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.MerchantSupport;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 社区食堂核心账务服务：额度购买/到账、定向充值、消费扣减、退款退回。
 *
 * <p>账务原则：
 * <ul>
 *   <li>额度购买走微信线上支付两段式（复用物业币充值模式），回调幂等到账；仅显式开启模拟支付（开发环境）才直接到账</li>
 *   <li>手续费内扣：actual_quota = quota_amount - fee_amount；手续费经 wallet_flows 落平台收入账</li>
 *   <li>定向余额账户 {@link CanteenDirectedBalance} 乐观锁防并发覆盖</li>
 *   <li>额度按购买记录 remaining_quota 逐笔扣减（复用商家积分模式）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CanteenService {

    public static final String BINDING_STATUS_ACTIVE = "active";

    private final RoleGuard roleGuard;
    private final MerchantSupport merchantSupport;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final OrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final CanteenMerchantBindingRepository bindingRepository;
    private final CanteenQuotaPurchaseRepository quotaPurchaseRepository;
    private final CanteenDirectedBalanceRepository directedBalanceRepository;
    private final CanteenDirectedRechargeRepository directedRechargeRepository;
    private final CanteenDirectedFlowRepository directedFlowRepository;
    private final PlatformSettingService platformSettingService;
    private final WalletFlowService walletFlowService;
    private final PayChannel wechatPayService;
    private final SnowflakeIdGenerator idGenerator;

    /** 模拟支付开关：仅开发环境可开启；生产必须关闭，否则商家可零成本获得食堂额度。 */
    @Value("${app.payment.mock-enabled:true}")
    private boolean mockPayEnabled;

    // ==================== 购买额度 ====================

    /** 购买试算：返回手续费、到账额度（手续费内扣口径） */
    public QuotaQuoteResponse quoteQuota(QuotaQuoteRequest request) {
        merchantSupport.requireCurrentMerchant();
        BigDecimal quotaAmount = request.getAmount();
        BigDecimal feeRate = platformSettingService.getCanteenQuotaFeeRate();
        BigDecimal feeAmount = computeFee(quotaAmount, feeRate);
        return QuotaQuoteResponse.builder()
                .quotaAmount(quotaAmount)
                .feeRate(feeRate)
                .feeAmount(feeAmount)
                .actualQuota(quotaAmount.subtract(feeAmount))
                .build();
    }

    /** 发起购买额度：微信预下单（线上支付自动到账）；微信未配置时仅模拟支付开关开启（开发环境）才直接到账，否则拒绝 */
    @Transactional
    public QuotaPurchaseResponse purchaseQuota(QuotaPurchaseRequest request) {
        Merchant merchant = requireMainMerchant(merchantSupport.requireCurrentMerchant());
        BigDecimal quotaAmount = request.getAmount();
        BigDecimal feeRate = platformSettingService.getCanteenQuotaFeeRate();
        BigDecimal feeAmount = computeFee(quotaAmount, feeRate);
        BigDecimal actualQuota = quotaAmount.subtract(feeAmount);

        CanteenQuotaPurchase purchase = new CanteenQuotaPurchase();
        purchase.setId(idGenerator.generateId("cqp"));
        purchase.setMerchantId(merchant.getId());
        purchase.setPropertyCompanyId(merchant.getPropertyCompanyId());
        purchase.setQuotaAmount(quotaAmount);
        purchase.setFeeRate(feeRate);
        purchase.setFeeAmount(feeAmount);
        purchase.setActualQuota(actualQuota);
        purchase.setPayAmount(quotaAmount);
        purchase.setRemainingQuota(BigDecimal.ZERO);

        if (wechatPayService.isEnabled()) {
            // 防重复提交：该主商家已有 pending 购买时拒绝（按店主 resident 维度）
            if (!paymentRecordRepository.findByBizTypeAndResidentIdAndStatus(
                    PaymentBizType.QUOTA_PURCHASE, resolveOperatorId(merchant), "pending").isEmpty()) {
                throw new BizException(ErrorCode.CANTEEN_PURCHASE_PENDING);
            }
            String openId = resolveOpenId(request.getOpenid(), merchant);
            purchase.setStatus("pending");
            purchase = quotaPurchaseRepository.save(purchase);

            PaymentRecord record = wechatPayService.createIntent(
                    PaymentBizType.QUOTA_PURCHASE, purchase.getId(), resolveOperatorId(merchant),
                    merchant.getPropertyCompanyId(), "wechat", quotaAmount,
                    0, BigDecimal.ZERO, quotaAmount);
            purchase.setPaymentRecordId(record.getId());
            quotaPurchaseRepository.save(purchase);

            WechatJsPayParams payParams = wechatPayService.miniPay(record, openId, "社区食堂充值额度");
            return QuotaPurchaseResponse.builder()
                    .purchaseId(purchase.getId())
                    .quotaAmount(quotaAmount)
                    .feeAmount(feeAmount)
                    .actualQuota(actualQuota)
                    .payAmount(quotaAmount)
                    .paymentRecordId(record.getId())
                    .status("pending")
                    .payParams(payParams)
                    .build();
        }

        // 微信支付未配置：仅显式开启模拟支付（开发环境）才允许直接到账；生产必须拒绝，杜绝免支付到账资损
        if (!mockPayEnabled) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "在线支付未开通，请联系物业线下缴纳后由管理端确认到账");
        }

        // 开发模式（模拟支付开关开启）：直接到账
        purchase.setStatus("paid");
        purchase.setRemainingQuota(actualQuota);
        purchase.setCompletedAt(LocalDateTime.now());
        purchase = quotaPurchaseRepository.save(purchase);
        log.info("[社区食堂] 开发模式额度直接到账 merchantId={} purchaseId={} quota={}",
                merchant.getId(), purchase.getId(), actualQuota);
        return QuotaPurchaseResponse.builder()
                .purchaseId(purchase.getId())
                .quotaAmount(quotaAmount)
                .feeAmount(feeAmount)
                .actualQuota(actualQuota)
                .payAmount(quotaAmount)
                .status("paid")
                .build();
    }

    /** 微信回调确认后额度到账（幂等）。由 PaymentBizDispatcher 调用，无登录上下文。 */
    @Transactional
    public void completeQuotaPurchase(PaymentRecord record) {
        CanteenQuotaPurchase purchase = quotaPurchaseRepository.findByPaymentRecordId(record.getId())
                .orElseThrow(() -> new BizException(ErrorCode.CANTEEN_QUOTA_PURCHASE_NOT_FOUND,
                        "额度购买记录不存在: " + record.getId()));
        // 幂等：已到账直接返回
        if ("paid".equals(purchase.getStatus())) {
            return;
        }
        purchase.setStatus("paid");
        purchase.setRemainingQuota(purchase.getActualQuota());
        purchase.setCompletedAt(LocalDateTime.now());
        quotaPurchaseRepository.save(purchase);

        // 平台手续费落账（wallet_flows，幂等键 canteen:quota_fee:{purchaseId}）
        if (purchase.getFeeAmount() != null && purchase.getFeeAmount().compareTo(BigDecimal.ZERO) > 0) {
            walletFlowService.recordCanteenQuotaFee(
                    purchase.getMerchantId(), purchase.getId(), purchase.getFeeAmount());
        }
        log.info("[社区食堂] 额度购买到账 merchantId={} purchaseId={} actualQuota={} fee={}",
                purchase.getMerchantId(), purchase.getId(), purchase.getActualQuota(), purchase.getFeeAmount());
    }

    /** 主商家额度余额查询 */
    @Transactional(readOnly = true)
    public MerchantQuotaBalanceResponse getQuotaBalance() {
        Merchant merchant = requireMainMerchant(merchantSupport.requireCurrentMerchant());
        BigDecimal available = defaultDecimal(quotaPurchaseRepository.sumRemainingQuota(merchant.getId()));
        BigDecimal purchased = quotaPurchaseRepository.findByMerchantIdAndStatus(merchant.getId(), "paid")
                .stream()
                .map(CanteenQuotaPurchase::getActualQuota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal recharged = directedRechargeRepository.findByMainMerchantId(merchant.getId())
                .stream()
                .map(CanteenDirectedRecharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MerchantQuotaBalanceResponse.builder()
                .merchantId(merchant.getId())
                .availableQuota(available)
                .totalPurchasedQuota(purchased)
                .totalRecharged(recharged)
                .build();
    }

    /** 主商家额度流水：购买记录 + 充值扣减（分页） */
    @Transactional(readOnly = true)
    public PageResponse<QuotaRecordResponse> listQuotaRecords(int page, int pageSize) {
        Merchant merchant = merchantSupport.requireCurrentMerchant();
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 购买记录（按当前商家过滤）
        Page<CanteenQuotaPurchase> purchasePage = quotaPurchaseRepository
                .findByMerchantId(merchant.getId(), pageable);
        List<QuotaRecordResponse> list = new ArrayList<>();
        purchasePage.getContent().forEach(p -> list.add(QuotaRecordResponse.builder()
                .id(p.getId())
                .type("purchase")
                .amount(p.getPayAmount())
                .feeAmount(p.getFeeAmount())
                .actualQuota(p.getActualQuota())
                .status(p.getStatus())
                .remark("购买充值额度")
                .createdAt(DateTimeUtil.toIsoUtc(p.getCreatedAt()))
                .build()));

        // 充值扣减（按当前商家过滤）
        Page<CanteenDirectedRecharge> rechargePage = directedRechargeRepository
                .findByMainMerchantId(merchant.getId(), pageable);
        rechargePage.getContent().forEach(r -> list.add(QuotaRecordResponse.builder()
                .id(r.getId())
                .type("recharge")
                .amount(r.getAmount().negate())
                .targetResidentId(r.getResidentId())
                .remark(StringUtils.hasText(r.getRemark()) ? r.getRemark() : "定向充值")
                .createdAt(DateTimeUtil.toIsoUtc(r.getCreatedAt()))
                .build()));

        list.sort(Comparator.comparing(QuotaRecordResponse::getCreatedAt).reversed());
        return PageResponse.of(list, page, pageSize,
                purchasePage.getTotalElements() + rechargePage.getTotalElements());
    }

    // ==================== 定向充值 ====================

    /** 主商家给用户充定向余额（从自己额度中扣减） */
    @Transactional
    public DirectedRechargeResponse recharge(DirectedRechargeRequest request) {
        Merchant merchant = requireMainMerchant(merchantSupport.requireCurrentMerchant());
        Resident resident = locateResident(request.getPhone(), request.getResidentId(), merchant.getPropertyCompanyId());

        BigDecimal amount = request.getAmount();
        BigDecimal available = defaultDecimal(quotaPurchaseRepository.sumRemainingQuota(merchant.getId()));
        if (available.compareTo(amount) < 0) {
            throw new BizException(ErrorCode.CANTEEN_QUOTA_INSUFFICIENT);
        }

        // 按到账时间先后逐笔扣减购买记录剩余额度（复用商家积分模式，防负值）
        List<CanteenQuotaPurchase> spendable = quotaPurchaseRepository.findSpendableByMerchantId(merchant.getId());
        BigDecimal toDeduct = amount;
        for (CanteenQuotaPurchase purchase : spendable) {
            if (toDeduct.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal deduct = purchase.getRemainingQuota().min(toDeduct);
            purchase.setRemainingQuota(purchase.getRemainingQuota().subtract(deduct));
            quotaPurchaseRepository.save(purchase);
            toDeduct = toDeduct.subtract(deduct);
        }

        // 用户定向余额账户入账（行锁；并发充值串行化，避免乐观锁冲突导致的入账丢失）
        CanteenDirectedBalance account = directedBalanceRepository
                .findByResidentIdAndMainMerchantIdForUpdate(resident.getId(), merchant.getId())
                .orElseGet(() -> {
                    CanteenDirectedBalance created = new CanteenDirectedBalance();
                    created.setId(idGenerator.generateId("cdb"));
                    created.setResidentId(resident.getId());
                    created.setMainMerchantId(merchant.getId());
                    created.setPropertyCompanyId(merchant.getPropertyCompanyId());
                    created.setBalance(BigDecimal.ZERO);
                    return created;
                });
        BigDecimal newBalance = defaultDecimal(account.getBalance()).add(amount);
        account.setBalance(newBalance);
        try {
            directedBalanceRepository.save(account);
        } catch (ObjectOptimisticLockingFailureException ex) {
            // 已加行锁，理论上不会走到这里；保留兜底以便在锁被绕过时仍能明确暴露问题
            log.error("[社区食堂] 定向充值并发冲突 merchantId={} residentId={}",
                    merchant.getId(), resident.getId(), ex);
            throw new BizException(ErrorCode.ORDER_CONFLICT, "并发冲突，请重试");
        }

        CanteenDirectedRecharge recharge = new CanteenDirectedRecharge();
        recharge.setId(idGenerator.generateId("cdr"));
        recharge.setMainMerchantId(merchant.getId());
        recharge.setResidentId(resident.getId());
        recharge.setPropertyCompanyId(merchant.getPropertyCompanyId());
        recharge.setAmount(amount);
        recharge.setBalanceAfter(newBalance);
        recharge.setOperatorId(UserContext.currentUserId());
        recharge.setRemark(request.getRemark());
        directedRechargeRepository.save(recharge);

        log.info("[社区食堂] 定向充值 merchantId={} residentId={} amount={} balanceAfter={}",
                merchant.getId(), resident.getId(), amount, newBalance);
        return DirectedRechargeResponse.builder()
                .rechargeId(recharge.getId())
                .mainMerchantId(merchant.getId())
                .residentId(resident.getId())
                .residentName(resident.getName())
                .amount(amount)
                .balanceAfter(newBalance)
                .build();
    }

    // ==================== 用户端查询 ====================

    /** 主商家定向充值记录（分页） */
    @Transactional(readOnly = true)
    public PageResponse<DirectedRechargeResponse> listRechargeRecords(int page, int pageSize) {
        Merchant merchant = merchantSupport.requireCurrentMerchant();
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<CanteenDirectedRecharge> rechargePage = directedRechargeRepository
                .findByMainMerchantId(merchant.getId(), pageable);
        Map<String, Resident> residentMap = residentRepository.findAllById(
                        rechargePage.getContent().stream().map(CanteenDirectedRecharge::getResidentId)
                                .distinct().toList())
                .stream().collect(Collectors.toMap(Resident::getId, Function.identity()));
        List<DirectedRechargeResponse> list = rechargePage.getContent().stream()
                .map(r -> DirectedRechargeResponse.builder()
                        .rechargeId(r.getId())
                        .mainMerchantId(r.getMainMerchantId())
                        .residentId(r.getResidentId())
                        .residentName(residentMap.containsKey(r.getResidentId())
                                ? residentMap.get(r.getResidentId()).getName() : null)
                        .amount(r.getAmount())
                        .balanceAfter(r.getBalanceAfter())
                        .build())
                .toList();
        return PageResponse.of(list, page, pageSize, rechargePage.getTotalElements());
    }

    /** 我的定向余额列表（多主商家体系） */
    @Transactional(readOnly = true)
    public List<DirectedBalanceResponse> getMyDirectedBalances() {
        String residentId = UserContext.currentUserId();
        List<CanteenDirectedBalance> accounts = directedBalanceRepository.findByResidentId(residentId);
        if (accounts.isEmpty()) {
            return List.of();
        }
        Map<String, Merchant> merchantMap = merchantRepository.findAllById(
                        accounts.stream().map(CanteenDirectedBalance::getMainMerchantId).distinct().toList())
                .stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));
        return accounts.stream()
                .map(a -> DirectedBalanceResponse.builder()
                        .mainMerchantId(a.getMainMerchantId())
                        .mainMerchantName(merchantMap.containsKey(a.getMainMerchantId())
                                ? merchantMap.get(a.getMainMerchantId()).getName() : null)
                        .balance(defaultDecimal(a.getBalance()))
                        .build())
                .toList();
    }

    /** 我的定向余额流水（充值/消费/退款） */
    @Transactional(readOnly = true)
    public PageResponse<DirectedFlowResponse> getMyDirectedFlows(int page, int pageSize) {
        String residentId = UserContext.currentUserId();
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<CanteenDirectedFlow> flowPage = directedFlowRepository.findByResidentId(residentId, pageable);
        return toFlowPageResponse(flowPage, page);
    }

    // ==================== 订单消费抵扣 / 退款退回（被 OrderService 调用） ====================

    /**
     * 订单支付试算：返回订单商家所属主商家体系中，用户可用（余额&gt;0）的定向余额列表。
     * 供 previewOrder 展示/前端选体系。
     */
    @Transactional(readOnly = true)
    public List<DirectedBalanceResponse> listUsableBalances(String merchantId, String residentId) {
        Set<String> mainMerchantIds = resolveMainMerchantIds(merchantId, null);
        if (mainMerchantIds.isEmpty()) {
            return List.of();
        }
        List<CanteenDirectedBalance> accounts = directedBalanceRepository
                .findByResidentIdAndMainMerchantIdIn(residentId, new ArrayList<>(mainMerchantIds));
        if (accounts.isEmpty()) {
            return List.of();
        }
        Map<String, Merchant> merchantMap = merchantRepository.findAllById(
                        accounts.stream().map(CanteenDirectedBalance::getMainMerchantId).distinct().toList())
                .stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));
        return accounts.stream()
                .filter(a -> defaultDecimal(a.getBalance()).compareTo(BigDecimal.ZERO) > 0)
                .map(a -> DirectedBalanceResponse.builder()
                        .mainMerchantId(a.getMainMerchantId())
                        .mainMerchantName(merchantMap.containsKey(a.getMainMerchantId())
                                ? merchantMap.get(a.getMainMerchantId()).getName() : null)
                        .balance(defaultDecimal(a.getBalance()))
                        .build())
                .toList();
    }

    /**
     * 订单支付试算：校验所选主商家体系与订单商家匹配、余额足够，返回可抵扣金额（= requested）。
     * 由 OrderService.resolvePaymentBreakdown 调用。
     */
    @Transactional(readOnly = true)
    public BigDecimal resolveDirectedUsable(String residentId, String merchantId,
                                            String mainMerchantId, BigDecimal requested) {
        if (requested == null || requested.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        Set<String> valid = resolveMainMerchantIds(merchantId, null);
        if (!valid.contains(mainMerchantId)) {
            throw new BizException(ErrorCode.CANTEEN_MERCHANT_NOT_IN_SYSTEM);
        }
        CanteenDirectedBalance account = directedBalanceRepository
                .findByResidentIdAndMainMerchantId(residentId, mainMerchantId)
                .orElseThrow(() -> new BizException(ErrorCode.CANTEEN_BALANCE_INSUFFICIENT));
        BigDecimal balance = defaultDecimal(account.getBalance());
        if (balance.compareTo(requested) < 0) {
            throw new BizException(ErrorCode.CANTEEN_BALANCE_INSUFFICIENT);
        }
        return requested;
    }

    /**
     * 订单支付时扣减定向余额（与 executePayment 同事务）。校验所选体系与订单商家匹配。
     * 幂等由调用方订单支付完成路径保证（支付回调只会触发一次 executePayment）。
     */
    @Transactional
    public void consumeForOrder(String residentId, String mainMerchantId, String merchantId,
                                String orderId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // 强校验：商家必须在所选主商家体系内（主商家自己 或 绑定为子商家）
        Set<String> validMainMerchants = resolveMainMerchantIds(merchantId, null);
        if (!validMainMerchants.contains(mainMerchantId)) {
            throw new BizException(ErrorCode.CANTEEN_MERCHANT_NOT_IN_SYSTEM);
        }
        // 行锁：支付已成功的前提下，并发扣减必须串行。
        // 若此处用乐观锁并在冲突时抛"请重试"，会造成「付款成功但定向余额未扣」的可白嫖漏洞。
        CanteenDirectedBalance account = directedBalanceRepository
                .findByResidentIdAndMainMerchantIdForUpdate(residentId, mainMerchantId)
                .orElseThrow(() -> new BizException(ErrorCode.CANTEEN_BALANCE_INSUFFICIENT));
        BigDecimal balance = defaultDecimal(account.getBalance());
        if (balance.compareTo(amount) < 0) {
            throw new BizException(ErrorCode.CANTEEN_BALANCE_INSUFFICIENT);
        }
        BigDecimal newBalance = balance.subtract(amount);
        account.setBalance(newBalance);
        try {
            directedBalanceRepository.save(account);
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.error("[社区食堂] 定向余额扣减并发冲突 residentId={} mainMerchantId={} orderId={}",
                    residentId, mainMerchantId, orderId, ex);
            throw new BizException(ErrorCode.ORDER_CONFLICT, "并发冲突，请重试");
        }
        writeFlow(residentId, mainMerchantId, merchantId, orderId,
                CanteenDirectedFlow.FLOW_CONSUME_DEDUCT, amount, newBalance);
    }

    /**
     * 订单退款时定向余额原路退回。以订单快照 directed_main_merchant_id 为准，不受解绑影响。
     */
    @Transactional
    public void refundForOrder(Order order, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || !StringUtils.hasText(order.getDirectedMainMerchantId())) {
            return;
        }
        CanteenDirectedBalance account = directedBalanceRepository
                .findByResidentIdAndMainMerchantIdForUpdate(order.getResidentId(), order.getDirectedMainMerchantId())
                .orElseGet(() -> {
                    CanteenDirectedBalance created = new CanteenDirectedBalance();
                    created.setId(idGenerator.generateId("cdb"));
                    created.setResidentId(order.getResidentId());
                    created.setMainMerchantId(order.getDirectedMainMerchantId());
                    created.setPropertyCompanyId(order.getPropertyCompanyId());
                    created.setBalance(BigDecimal.ZERO);
                    return created;
                });
        BigDecimal newBalance = defaultDecimal(account.getBalance()).add(amount);
        account.setBalance(newBalance);
        try {
            directedBalanceRepository.save(account);
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.error("[社区食堂] 定向余额退款并发冲突 residentId={} orderId={}",
                    order.getResidentId(), order.getId(), ex);
            throw new BizException(ErrorCode.ORDER_CONFLICT, "并发冲突，请重试");
        }
        writeFlow(order.getResidentId(), order.getDirectedMainMerchantId(), order.getMerchantId(),
                order.getId(), CanteenDirectedFlow.FLOW_REFUND_BACK, amount, newBalance);
    }

    // ==================== 主商家判定 ====================

    /** 主商家判定：merchants.is_canteen_main_merchant=1 */
    public boolean isMainMerchant(Merchant merchant) {
        return merchant != null
                && "canteen".equals(merchant.getMerchantType())
                && Boolean.TRUE.equals(merchant.getIsCanteenMainMerchant());
    }

    public Merchant requireMainMerchant(Merchant merchant) {
        if (!isMainMerchant(merchant)) {
            throw new BizException(ErrorCode.CANTEEN_NOT_MAIN_MERCHANT);
        }
        return merchant;
    }

    /**
     * 解析订单商家可用的主商家体系ID集合：
     * - 商家本身是主商家 → 自己
     * - 商家被绑定的 active 主商家 → 全部
     */
    public Set<String> resolveMainMerchantIds(String merchantId, String propertyCompanyId) {
        Set<String> result = new LinkedHashSet<>();
        Merchant merchant = merchantId != null
                ? merchantRepository.findById(merchantId).orElse(null)
                : null;
        if (merchant == null) {
            return result;
        }
        // 订单商家自身是社区食堂主商家
        if (isMainMerchant(merchant)) {
            result.add(merchant.getId());
        }
        // 订单商家作为社区食堂子商家，其绑定的社区食堂主商家（仅保留 canteen 类型，隔离普通商家）
        List<String> candidateMainIds = bindingRepository
                .findBySubMerchantIdAndStatus(merchant.getId(), BINDING_STATUS_ACTIVE)
                .stream()
                .map(CanteenMerchantBinding::getMainMerchantId)
                .distinct()
                .toList();
        if (!candidateMainIds.isEmpty()) {
            merchantRepository.findAllById(candidateMainIds).stream()
                    .filter(m -> "canteen".equals(m.getMerchantType())
                            && Boolean.TRUE.equals(m.getIsCanteenMainMerchant()))
                    .map(Merchant::getId)
                    .forEach(result::add);
        }
        return result;
    }

    // ==================== 内部辅助 ====================

    private void writeFlow(String residentId, String mainMerchantId, String merchantId,
                           String orderId, String flowType, BigDecimal amount, BigDecimal balanceAfter) {
        CanteenDirectedFlow flow = new CanteenDirectedFlow();
        flow.setId(idGenerator.generateId("cdf"));
        flow.setResidentId(residentId);
        flow.setMainMerchantId(mainMerchantId);
        flow.setMerchantId(merchantId);
        flow.setOrderId(orderId);
        flow.setFlowType(flowType);
        flow.setAmount(amount);
        flow.setBalanceAfter(balanceAfter);
        directedFlowRepository.save(flow);
    }

    private PageResponse<DirectedFlowResponse> toFlowPageResponse(Page<CanteenDirectedFlow> flowPage, int page) {
        List<CanteenDirectedFlow> flows = flowPage.getContent();
        if (flows.isEmpty()) {
            return PageResponse.of(List.of(), page, flowPage.getSize(), flowPage.getTotalElements());
        }
        Map<String, Merchant> merchantMap = merchantRepository.findAllById(
                        flows.stream().map(CanteenDirectedFlow::getMerchantId).distinct().toList())
                .stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));
        Map<String, Merchant> mainMap = merchantRepository.findAllById(
                        flows.stream().map(CanteenDirectedFlow::getMainMerchantId).distinct().toList())
                .stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));
        Map<String, String> orderNoMap = orderRepository.findAllById(
                        flows.stream().map(CanteenDirectedFlow::getOrderId).filter(StringUtils::hasText).distinct().toList())
                .stream().collect(Collectors.toMap(Order::getId, Order::getOrderNo));
        List<DirectedFlowResponse> list = flows.stream().map(f -> DirectedFlowResponse.builder()
                .id(f.getId())
                .flowType(f.getFlowType())
                .mainMerchantName(mainMap.containsKey(f.getMainMerchantId())
                        ? mainMap.get(f.getMainMerchantId()).getName() : null)
                .amount(defaultDecimal(f.getAmount()))
                .balanceAfter(defaultDecimal(f.getBalanceAfter()))
                .orderNo(f.getOrderId() != null ? orderNoMap.get(f.getOrderId()) : null)
                .merchantName(merchantMap.containsKey(f.getMerchantId())
                        ? merchantMap.get(f.getMerchantId()).getName() : null)
                .createdAt(DateTimeUtil.toIsoUtc(f.getCreatedAt()))
                .build()).toList();
        return PageResponse.of(list, page, flowPage.getSize(), flowPage.getTotalElements());
    }

    private Resident locateResident(String phone, String residentId, String propertyCompanyId) {
        Resident resident;
        if (StringUtils.hasText(phone)) {
            resident = residentRepository.findByPhoneAndPropertyCompanyId(phone, propertyCompanyId)
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND,
                            "该物业下未找到对应手机号的住户"));
        } else {
            if (!StringUtils.hasText(residentId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "residentId与phone至少传一个");
            }
            if (!residentId.startsWith("res_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
            }
            resident = residentRepository.findById(residentId)
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        }
        if (!propertyCompanyId.equals(resident.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "住户与商家不属于同一物业公司");
        }
        return resident;
    }

    private String resolveOperatorId(Merchant merchant) {
        if (StringUtils.hasText(merchant.getResidentId())) {
            return merchant.getResidentId();
        }
        return UserContext.currentUserId();
    }

    private String resolveOpenId(String requestOpenId, Merchant merchant) {
        if (StringUtils.hasText(requestOpenId)) {
            return requestOpenId;
        }
        String operatorId = resolveOperatorId(merchant);
        return residentRepository.findById(operatorId)
                .map(Resident::getOpenid)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_MISSING,
                        "openid不能为空（微信小程序支付需要）"));
    }

    private BigDecimal computeFee(BigDecimal amount, BigDecimal feeRate) {
        return amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
