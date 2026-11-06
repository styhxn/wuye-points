package com.wuye.points.service;

import com.wuye.points.dto.scanpay.ScanPayCreateRequest;
import com.wuye.points.dto.scanpay.ScanPayCreateResponse;
import com.wuye.points.dto.scanpay.ScanPayStatusResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.MerchantWechatAccount;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.EcommerceSplitProperties;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.payment.WechatPayService;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.support.RateDefaults;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import com.wuye.points.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 商家扫码付款（v8.40）：顾客扫商家小程序码 → 极简收银台输金额 → 一次拿到
 * 微信小程序支付 payParams → 微信原生收银台付款。
 *
 * <p><b>为什么落成一条真实订单</b>：商家可提现余额由 orders(completed) +
 * distribution_records 计算，分成记录对 orders.id 有外键。若扫码付款不落订单，
 * 商家将收不到这笔钱。因此这里创建一条无商品的订单（source_type=scan_pay、
 * requires_delivery=false），并复用 {@link OrderCompletionService#settleOnCompleted}
 * 完成分成记录、微信分账和结算口径，资金链路与商城订单完全一致。</p>
 *
 * <p>支付通道也沿用商城订单判断：分账启用走服务商 + sub_mchid（钱进商家特约商户号，
 * profit_sharing=true），否则走平台直连代收；回调经 PaymentBizDispatcher 分发到
 * {@link #completeChannelPayment}，幂等发放积分并把订单推进到 completed。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScanPayService {

    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");
    private static final String SOURCE_SCAN_PAY = "scan_pay";

    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final MerchantWechatAccountRepository merchantWechatAccountRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final WechatPayService wechatPayService;
    private final WechatSplitService wechatSplitService;
    private final EcommerceSplitProperties ecommerceSplitProperties;
    private final PointService pointService;
    private final OrderCompletionService orderCompletionService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /** 创建扫码付款订单并预下单，一次返回 payParams（前端原样 wx.requestPayment）。 */
    @Transactional
    public ScanPayCreateResponse create(ScanPayCreateRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(resident.getOpenid())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "openid不能为空（微信小程序支付需要）");
        }
        if (!wechatPayService.isEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "在线支付未开通，请稍后再试");
        }
        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        assertMerchantAvailable(merchant);
        if (!paymentRecordRepository.findByBizTypeAndResidentIdAndStatus(
                PaymentBizType.SCAN_PAY, resident.getId(), "pending").isEmpty()) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "您有一笔扫码付款待完成，请先完成支付或稍后重试");
        }

        boolean partnerChannel = ecommerceSplitProperties.isEnabled()
                || wechatSplitService.isEnabledForProperty(resident.getPropertyCompanyId());
        String subMchid = null;
        if (partnerChannel) {
            if (!wechatPayService.isPartnerEnabled()) {
                throw new BizException(ErrorCode.ORDER_PAY_UNAVAILABLE,
                        "服务商支付通道未配置，扫码付款暂不可用");
            }
            MerchantWechatAccount account = merchantWechatAccountRepository
                    .findByMerchantIdAndStatus(merchant.getId(), "active").orElse(null);
            if (account == null || !StringUtils.hasText(account.getSubMchid())) {
                throw new BizException(ErrorCode.ORDER_PAY_UNAVAILABLE,
                        "该商家未完成微信进件，暂不能扫码付款");
            }
            subMchid = account.getSubMchid();
        } else if (!wechatPayService.isDirectEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "在线支付未开通，请稍后再试");
        }

        Order order = new Order();
        order.setId(idGenerator.generateId("ord"));
        order.setOrderNo(order.getId());
        order.setResidentId(resident.getId());
        order.setMerchantId(merchant.getId());
        order.setPropertyCompanyId(resident.getPropertyCompanyId());
        order.setFamilyId(resident.getFamilyId());
        order.setOrderStatus("pending");
        order.setTotalAmount(amount);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setOriginalDeliveryFee(BigDecimal.ZERO);
        order.setDeliverySubsidySponsor("none");
        order.setDeliverySubsidyAmount(BigDecimal.ZERO);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        order.setPointEarned(0);
        order.setCoinEarned(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setContactPhone(resident.getPhone());
        order.setRemark("扫码付款");
        order.setSourceType(SOURCE_SCAN_PAY);
        order.setRequiresDelivery(false);
        order.setFulfillmentMode("none");
        orderRepository.save(order);

        PaymentRecord record = wechatPayService.createIntent(
                PaymentBizType.SCAN_PAY, order.getId(), resident.getId(),
                resident.getPropertyCompanyId(), "wechat", amount, 0, BigDecimal.ZERO, amount);
        String body = "扫码付款-" + merchant.getName();
        WechatJsPayParams payParams = partnerChannel
                ? wechatPayService.miniPay(record, resident.getOpenid(), body, subMchid)
                : wechatPayService.miniPay(record, resident.getOpenid(), body);
        log.info("[扫码付款] 创建 merchantId={} residentId={} amount={} channel={} orderId={}",
                merchant.getId(), resident.getId(), amount, partnerChannel ? "partner" : "direct", order.getId());

        return ScanPayCreateResponse.builder()
                .scanPayId(order.getId())
                .merchantName(merchant.getName())
                .amount(amount)
                .paymentRecordId(record.getId())
                .outTradeNo(record.getOutTradeNo())
                .status("pending")
                .payParams(payParams)
                .build();
    }

    /** 前端拉起微信支付后轮询；只有 status=paid 且 paidAt 有值才算到账。 */
    @Transactional(readOnly = true)
    public ScanPayStatusResponse getStatus(String scanPayId) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        Order order = orderRepository.findById(scanPayId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "扫码付款单不存在"));
        if (!resident.getId().equals(order.getResidentId())
                || !SOURCE_SCAN_PAY.equals(order.getSourceType())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        Merchant merchant = merchantRepository.findById(order.getMerchantId()).orElse(null);
        PaymentRecord paid = paymentRecordRepository
                .findFirstByBizIdAndStatusOrderByCreatedAtDesc(order.getId(), "paid").orElse(null);
        boolean done = "completed".equals(order.getOrderStatus());
        return ScanPayStatusResponse.builder()
                .scanPayId(order.getId())
                .merchantName(merchant != null ? merchant.getName() : null)
                .amount(order.getTotalAmount())
                .status(done ? "paid" : order.getOrderStatus())
                .pointEarned(order.getPointEarned())
                .paidAt(done && order.getPaidAt() != null ? DateTimeUtil.toIsoUtc(order.getPaidAt()) : null)
                .paymentRecordId(paid != null ? paid.getId() : null)
                .outTradeNo(paid != null ? paid.getOutTradeNo() : null)
                .build();
    }

    /**
     * 微信支付回调确认后的业务落地（幂等）。由 PaymentBizDispatcher 调用，无登录上下文。
     * 订单号即幂等键：重复投递时订单已 completed，直接返回，积分不会重复发放。
     */
    @Transactional
    public void completeChannelPayment(PaymentRecord record) {
        if (record == null || !PaymentBizType.SCAN_PAY.equals(record.getBizType())
                || !StringUtils.hasText(record.getBizId())) {
            return;
        }
        Order order = orderRepository.findById(record.getBizId()).orElse(null);
        if (order == null || !SOURCE_SCAN_PAY.equals(order.getSourceType())
                || !"pending".equals(order.getOrderStatus())) {
            return;
        }
        Resident resident = residentRepository.findById(order.getResidentId()).orElse(null);
        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(order.getMerchantId(), order.getPropertyCompanyId())
                .filter(c -> "active".equals(c.getStatus()))
                .orElse(null);
        int points = resident != null ? calculatePoints(order, config) : 0;
        if (points > 0 && resident != null) {
            pointService.earnPoints(resident, points, "scan_pay", order.getId(), "扫码消费返积分", null);
        }
        order.setOrderStatus("completed");
        order.setPaymentMethod("wechat");
        order.setCashAmount(nz(record.getCashAmount()));
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setPointEarned(points);
        order.setCoinEarned(BigDecimal.ZERO);
        order.setPaidAt(LocalDateTime.now());
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 复用订单结算：生成分成记录（含商家应收）、写积分池，并在事务提交后触发微信分账/解冻。
        // 服务商通道 profit_sharing=true，不做分账会导致商家资金一直冻结。
        orderCompletionService.settleOnCompleted(order);
        log.info("[扫码付款] 支付完成 orderId={} amount={} points={} payMode={}",
                order.getId(), order.getTotalAmount(), points, record.getPayMode());
    }

    /** 积分与商品订单同公式：佣金基数 × 用户积分比例 × 积分兑换比，向下取整。 */
    private int calculatePoints(Order order, MerchantPropertyConfig config) {
        if (config == null || config.getCommissionRate() == null) {
            return 0;
        }
        BigDecimal commissionBase = nz(order.getTotalAmount())
                .subtract(nz(order.getCouponAmount())).max(BigDecimal.ZERO)
                .multiply(config.getCommissionRate()).setScale(2, RoundingMode.HALF_UP);
        PropertyCompany company = order.getPropertyCompanyId() != null
                ? propertyCompanyRepository.findById(order.getPropertyCompanyId()).orElse(null) : null;
        BigDecimal pointRate = company != null && company.getUserPointShareRate() != null
                ? company.getUserPointShareRate() : RateDefaults.USER_POINT_SHARE_RATE;
        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate() : DEFAULT_POINT_EXCHANGE_RATE;
        return commissionBase.multiply(pointRate).multiply(exchangeRate)
                .setScale(0, RoundingMode.DOWN).intValue();
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
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

    private void assertMerchantAvailable(Merchant merchant) {
        if (!"approved".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_AUDITING, "商家审核通过后才能扫码付款");
        }
        if ("inactive".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND, "商家已停用，无法扫码付款");
        }
    }
}
