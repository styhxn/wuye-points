package com.wuye.points.payment;

import cn.hutool.json.JSONObject;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.MerchantWechatAccount;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyManager;
import com.wuye.points.entity.WechatSplitReceiverAccount;
import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.repository.PropertyManagerRepository;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.service.MerchantTransferService;
import com.wuye.points.service.PlatformSettingService;
import com.wuye.points.service.SplitRecoveryService;
import com.wuye.points.service.WalletFlowService;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 微信分账业务服务（两级式分账状态机编排）。
 *
 * <p>只处理<b>服务商通道</b>的商城订单（{@code pay_mode=partner}）与电商收付通订单
 * （{@code pay_mode=ecommerce}）。平台直连商户收款的业务（物业费/充值等）<b>不参与微信分账</b>：
 * 那类资金直接进平台直连商户号，没有商家特约商户号可分。</p>
 *
 * <ul>
 *   <li><b>服务商分账</b>（/{@code v3/profitsharing/*}，默认）：用户付款资金冻结在<b>商家特约商户号</b>，
 *       服务商以 sub_mchid 指定分账出资方，按订单分成（DistributionRecord）逐收款方请求分账
 *       （平台佣金 + 配送链 + 物业/协调员/负责人），售后窗口到期解冻剩余资金给商家；
 *       资金不经平台归集，满足「不走内部钱包、全链路真实划转」的业务约束；</li>
 *   <li><b>电商收付通</b>（/{@code v3/ecommerce/profitsharing/*}）：需 ICP/EDI 资质，
 *       作为二期备选（切换开关即用，业务侧无感）。</li>
 * </ul>
 *
 * <p>分账接口的选择按<b>支付流水固化的 pay_mode</b> 决定，而不是当前全局开关——
 * 运营切换模式不会把历史订单发到错误的分账接口。</p>
 *
 * <p>① 用户确认收货（或超期自动确认）后 {@link #freezeOnOrderCompleted} 按订单分成发起分账；
 * ② 售后窗口到期 {@link #finishAfterWindow} 解冻剩余资金划账；退款 / 取消走 {@link #retryReverse} 退回。</p>
 *
 * <p><b>触发时机说明（P0-0 修复）</b>：分账必须在「订单完成」时点触发——分成记录
 * （{@code DistributionRecord}）正是在订单完成时由 {@code OrderCompletionService} 生成。
 * 历史上曾在支付成功时触发，导致每个订单都找不到分成记录而静默跳过，微信分账实际零执行。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatSplitService {

    private final WechatSplitApiService wechatSplitApiService;
    private final EcommerceSplitApiService ecommerceSplitApiService;
    private final EcommerceSplitProperties ecommerceProps;
    private final WechatSplitRecordRepository splitRecordRepository;
    private final WechatSplitReceiverAccountService receiverAccountService;
    private final WechatSplitProperties wechatSplitProperties;
    private final PlatformSettingService platformSettingService;
    private final WechatPayProperties wechatPayProperties;
    private final WechatPayService wechatPayService;
    private final DistributionRecordRepository distributionRecordRepository;
    private final MerchantWechatAccountRepository merchantWechatAccountRepository;
    private final DeliveryRepository deliveryRepository;
    private final com.wuye.points.repository.PaymentRecordRepository paymentRecordRepository;
    private final MerchantTransferService merchantTransferService;
    private final SplitRecoveryService recoveryService;
    private final PropertyManagerRepository propertyManagerRepository;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 业务总开关（普通分账是否启用）：配置总开关（{@code wechat-split.enabled}，fail-closed）
     * 与运营运行时开关（{@code settlement.wechatSplitEnabled}，管理端 {@code PUT /admin/settlement-config}）取与。
     * <p>两个开关都必须为 true 才启用，故管理端只能收紧、无法突破配置总开关，保持 fail-closed。</p>
     */
    public boolean isEnabled() {
        return wechatSplitProperties.isEnabled() && platformSettingService.isWechatSplitEnabled();
    }

    /**
     * 该物业公司是否启用分账：在 {@link #isEnabled()} 基础上叠加灰度白名单
     * （白名单非空时仅名单内启用）。
     */
    public boolean isEnabledForProperty(String propertyCompanyId) {
        return isEnabled() && wechatSplitProperties.isEnabledForProperty(propertyCompanyId);
    }

    /**
     * 订单确认收货后分账（两级式分账第 ① 步）。
     *
     * <p><b>为什么在订单完成时触发（P0-0 修复）</b>：分成记录 {@code DistributionRecord}
     * 由 {@code OrderCompletionService.settleOnCompleted} 在订单 completed 时生成；此前在
     * 支付成功时触发，那一刻分成记录必然不存在，每个订单都走「无分成记录 → 跳过」分支，
     * 微信分账实际零执行。现在触发点与数据生成点对齐。</p>
     *
     * <p><b>业务规则</b>：确认收货前允许全额原路退款（此时无分账记录，退款无资金障碍）；
     * 确认收货后按业务规则不再退货，分账资金按状态机推进至完结。</p>
     *
     * <p>门禁 fail-closed：{@link WechatSplitApiService#isEnabled()}（微信配置就绪）
     * + {@link #isEnabledForProperty}（配置总开关 × 运营开关 × 物业灰度白名单）。</p>
     *
     * <p><b>幂等语义（上线前加固）</b>：逐接收方判重（{@code existsByOrderIdAndOwnerType}），
     * 分账单号按「订单 + 主体」确定性生成——微信侧同分账单号多次请求等同一次，
     * 因此冻结重跑 / 缺户补分 / {@code SplitRepairScheduler} 修复补跑均不会重复出钱。
     * 本方法<b>不持有事务</b>：每个接收方的微信调用结果立即落库，单个接收方失败不影响
     * 同订单其余接收方，也不会因事务回滚丢失「微信已成功、本地无记录」的分账。</p>
     */
    public void freezeOnOrderCompleted(String orderId) {
        if (!StringUtils.hasText(orderId)) {
            return;
        }
        if (!wechatSplitApiService.isEnabled()) {
            log.debug("[分账] 微信支付未配置就绪，跳过 orderId={}", orderId);
            return;
        }
        DistributionRecord dist = distributionRecordRepository.findFirstByOrderId(orderId).orElse(null);
        if (dist == null) {
            log.info("[分账] 订单 {} 无分成记录，跳过分账", orderId);
            return;
        }
        if (!isEnabledForProperty(dist.getPropertyCompanyId())) {
            log.info("[分账] 订单 {} 所属物业未启用分账（总开关/灰度白名单），跳过", orderId);
            return;
        }
        // 同一订单可能因关单/重付存在多条流水：取最近一条 paid，避免 findByBizId 多结果异常
        PaymentRecord record = paymentRecordRepository
                .findFirstByBizIdAndStatusOrderByCreatedAtDesc(orderId, "paid")
                .orElse(null);
        if (record == null) {
            log.warn("[分账] 订单 {} 无 paid 支付流水，跳过分账（待对账补分）", orderId);
            return;
        }
        String payMode = WechatPayModes.resolve(record.getPayMode(), record.getSubMchid(), ecommerceProps.isEnabled());
        if (WechatPayModes.ECOMMERCE.equals(payMode)) {
            ecommerceFreeze(record, dist);
            return;
        }
        if (!WechatPayModes.PARTNER.equals(payMode)) {
            // 直连商户收款（平台自营代收）没有商家特约商户号，微信侧无从发起分账；
            // 修复调度会批量扫到这类订单，用 debug 避免日志噪声。
            log.debug("[分账] 订单 {} 支付通道为 {}（非服务商分账），跳过微信分账", orderId, payMode);
            return;
        }
        // ===== 服务商分账（资金冻结在商家特约商户号内，平台按订单分成逐收款方分出） =====
        // sub_mchid 既是商家入账主体也是分账出资方；缺失即分账不可执行（fail-closed，禁止回退服务商号）
        String subMchid = record.getSubMchid();
        if (!StringUtils.hasText(subMchid)) {
            log.error("[分账] 订单 {} 为服务商支付但流水缺 sub_mchid，无法分账（recordId={}）", orderId, record.getId());
            return;
        }
        // 微信分账 transaction_id 必须是「微信支付订单号」；回调丢失、主动查单确认支付时未落库，先补全
        String transactionId = record.getChannelTradeNo();
        if (!StringUtils.hasText(transactionId)) {
            transactionId = wechatPayService.queryTransactionId(record);
            if (StringUtils.hasText(transactionId)) {
                record.setChannelTradeNo(transactionId);
                paymentRecordRepository.save(record);
            } else {
                log.warn("[分账] 订单 {} 缺微信支付订单号(transaction_id)，分账冻结跳过，待对账补全", orderId);
                return;
            }
        }
        // 配送链路分成（平台配送分成归平台、配送报酬归快递员）与商品佣金链同笔分出
        Delivery delivery = deliveryRepository.findByOrderId(orderId).orElse(null);
        List<SplitPlan> plans = buildPlans(dist, delivery);
        if (plans.isEmpty()) {
            log.info("[分账] 订单 {} 无可分金额，跳过", orderId);
            return;
        }
        // 微信服务商分账默认上限：分出总额不得超过订单金额的 maxSplitRatio（默认 30%，须与商户平台
        // 「最大分账比例」一致）。超限时微信会逐笔拒绝并把部分接收方留在未分状态，故先整体拦截。
        BigDecimal totalPlan = plans.stream().map(p -> p.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ratio = wechatSplitProperties.getMaxSplitRatio() == null
                ? new BigDecimal("0.30") : wechatSplitProperties.getMaxSplitRatio();
        BigDecimal maxSplit = nz(record.getCashAmount())
                .multiply(ratio)
                .setScale(2, java.math.RoundingMode.DOWN);
        if (totalPlan.compareTo(maxSplit) > 0) {
            log.error("[分账] 订单 {} 分账总额 {} 元超过订单金额 {} 元的 {}%（上限 {} 元），本次全部分账拦截。"
                            + "请下调分成比例，或在微信商户平台申请更高的最大分账比例后调整 wechat-split.max-split-ratio",
                    orderId, totalPlan, record.getCashAmount(),
                    ratio.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString(),
                    maxSplit);
            return;
        }
        // 官方文档：分账总额不能超过「订单金额减去手续费的净额」，请求分账前应先查剩余待分金额。
        // 30% 比例以订单金额为基数，实际可用资金以 net 为上限，二者取小才是安全上限。
        Long unsplitFen = wechatSplitApiService.queryUnsplitAmount(transactionId);
        if (unsplitFen != null) {
            long planFen = Long.parseLong(WechatSplitApiService.fen(totalPlan));
            long effectiveFen = Math.min(Long.parseLong(WechatSplitApiService.fen(maxSplit)), unsplitFen);
            if (planFen > effectiveFen) {
                log.error("[分账] 订单 {} 分账总额 {} 分超过可用上限 {} 分（比例上限 {} 分 / 微信剩余待分 {} 分），"
                                + "本次全部分账拦截", orderId, planFen, effectiveFen,
                        Long.parseLong(WechatSplitApiService.fen(maxSplit)), unsplitFen);
                return;
            }
        }
        // 同一订单的所有接收方共享一个 groupNo（修复补跑时复用已有分组，完结按组一次解冻）
        String groupNo = splitRecordRepository.findFirstByOrderId(orderId)
                .map(WechatSplitRecord::getGroupNo)
                .orElseGet(() -> idGenerator.generateId("grp_"));
        for (SplitPlan plan : plans) {
            // 逐接收方幂等：已存在同订单同主体记录则跳过（冻结/补分/修复重跑均安全）。
            // splitNo 按 (订单, 主体) 确定性生成——微信侧同分账单号多次请求等同一次，
            // 保证「微信已分账成功但本地记录未落库」时重跑不会重复出钱。
            if (splitRecordRepository.existsByOrderIdAndOwnerType(orderId, plan.ownerType)) {
                continue;
            }
            String splitNo = splitNoOf(orderId, plan.ownerType);
            Receiver receiver = resolveReceiver(plan.ownerType, plan.ownerId);
            WechatSplitRecord sr = new WechatSplitRecord();
            sr.setId(idGenerator.generateId("wsr_"));
            sr.setPaymentRecordId(record.getId());
            sr.setOutTradeNo(record.getOutTradeNo());
            sr.setWechatOutTradeNo(transactionId);
            sr.setBizType(record.getBizType());
            sr.setOrderId(orderId);
            sr.setSplitNo(splitNo);
            sr.setPayerAccount(subMchid);
            sr.setOwnerType(plan.ownerType);
            sr.setOwnerId(plan.ownerId);
            sr.setFreezeAmount(plan.amount);
            sr.setGroupNo(groupNo);
            sr.setCurrency("CNY");
            if (receiver == null || !StringUtils.hasText(receiver.accountNo())) {
                sr.setStatus("skipped");
                sr.setRemark("收款账户未注册/未校验");
                splitRecordRepository.save(sr);
                continue;
            }
            sr.setPayeeAccount(receiver.accountNo());
            if (WechatSplitOwnerTypes.PLATFORM.name().equals(plan.ownerType)
                    && !StringUtils.hasText(receiver.accountName())) {
                log.warn("[分账] 平台收款商户名称未配置（wechat.pay.platform-receiver-name），"
                        + "添加 MERCHANT_ID 接收方可能被微信拒绝 orderId={}", orderId);
            }
            try {
                // 分账前置：把接收方加入该子商户的分账接收方列表（幂等；缺失时随后的冻结会以
                // 「关系不存在」失败并落 freeze_failed，由补偿调度带着本步骤一起重试）
                String relationType = relationTypeOf(plan.ownerType);
                wechatSplitApiService.ensureReceiverAdded(subMchid, receiver.accountNo(),
                        receiver.accountName(), relationType, customRelationOf(relationType));
                String wxOrderId = wechatSplitApiService.freezeAllocate(splitNo, transactionId,
                        subMchid, receiver.accountNo(), plan.amount, "物业积分分账-" + plan.ownerType,
                        receiver.accountName());
                sr.setWechatSubTradeNo(wxOrderId);
                sr.setStatus("frozen");
            } catch (Exception e) {
                sr.setStatus("freeze_failed");
                sr.setRemark(truncate(e.getMessage()));
                log.error("[分账] 冻结失败 splitNo={} ownerType={}", splitNo, plan.ownerType, e);
            }
            // 逐笔立即落库（本方法不持有事务）：微信调用结果与本地记录一一对应，
            // 单个接收方失败/异常不影响同订单其余接收方，也不会因事务回滚丢失已成功的分账记录
            splitRecordRepository.save(sr);
        }
    }

    /**
     * 分账单号（out_order_no）确定性生成：同一订单同一主体永远同一单号。
     * 微信对同一分账单号的多次请求按一次处理（幂等），这是「冻结重跑 / 补分 / 崩溃恢复」
     * 不重复出钱的关键约束。长度 3+32+1+17=53，在微信 64 字符上限内。
     */
    static String splitNoOf(String orderId, String ownerType) {
        return "sp_" + orderId + "_" + ownerType;
    }

    /**
     * 电商收付通模式：发起一笔请求分账（receivers 含平台 MERCHANT_ID + 快递员 PERSONAL_OPENID）。
     * 分账金额 = 佣金总额(distributableAmount) + 平台配送分成(delivery.platformEarning)；
     * 快递员配送报酬(delivery.courierEarning) 单独作为一笔 PERSONAL_OPENID 分账；
     * 商家货款不参与分账，完结时解冻给商家。
     */
    private void ecommerceFreeze(PaymentRecord record, DistributionRecord dist) {
        String outOrderNo = idGenerator.generateId("grp_");
        MerchantWechatAccount mwa = merchantWechatAccountRepository
                .findByMerchantIdAndStatus(dist.getMerchantId(), "active").orElse(null);
        if (mwa == null) {
            log.warn("[电商分账] 商家 {} 未进件/未激活，订单 {} 暂不分账（等进件后补分）",
                    dist.getMerchantId(), record.getBizId());
            return;
        }
        String subMchid = mwa.getSubMchid();
        String transactionId = record.getChannelTradeNo();
        if (!StringUtils.hasText(transactionId)) {
            log.warn("[电商分账] 订单 {} 缺微信支付单号(transaction_id)，暂不分账", record.getBizId());
            return;
        }

        // 配送信息：平台配送分成归平台，配送报酬直分快递员（deliveries 为配送侧唯一事实源）
        Delivery delivery = deliveryRepository.findByOrderId(record.getBizId()).orElse(null);
        BigDecimal courierEarning = delivery != null ? nz(delivery.getCourierEarning()) : BigDecimal.ZERO;
        BigDecimal platformDeliveryShare = delivery != null ? nz(delivery.getPlatformEarning()) : BigDecimal.ZERO;
        // 平台应收 = 商品佣金总额 + 平台配送分成
        BigDecimal platformAmount = money(nz(dist.getDistributableAmount()).add(platformDeliveryShare));
        String platformMchId = ecommerceProps.resolvePlatformReceiverMchId(wechatPayProperties);

        // 快递员 openid（未注册则本次跳过，配送费留待补分/对账）
        String courierOpenid = null;
        boolean courierEligible = delivery != null
                && courierEarning.compareTo(BigDecimal.ZERO) > 0
                && StringUtils.hasText(delivery.getCourierId());
        if (courierEligible) {
            courierOpenid = receiverAccountService.resolveAccountNo(
                    WechatSplitOwnerTypes.COURIER.name(), delivery.getCourierId());
        }

        List<Map<String, Object>> receivers = new ArrayList<>();
        if (platformAmount.compareTo(BigDecimal.ZERO) > 0) {
            receivers.add(EcommerceSplitApiService.receiver(
                    "MERCHANT_ID", platformMchId, platformAmount, "物业积分佣金分成"));
        }
        if (courierEligible && StringUtils.hasText(courierOpenid)) {
            receivers.add(EcommerceSplitApiService.receiver(
                    "PERSONAL_OPENID", courierOpenid, courierEarning, "订单配送费"));
        }
        if (receivers.isEmpty()) {
            log.info("[电商分账] 订单 {} 无可分金额，跳过", record.getBizId());
            return;
        }

        try {
            String orderId = ecommerceSplitApiService.requestSplit(subMchid, transactionId, outOrderNo, receivers);
            if (platformAmount.compareTo(BigDecimal.ZERO) > 0) {
                saveEcommerceSplit(record, dist, outOrderNo, subMchid, transactionId, orderId,
                        platformMchId, WechatSplitOwnerTypes.PLATFORM.name(), WalletFlowService.PLATFORM_OWNER_ID,
                        platformAmount, "frozen", null);
            }
            if (courierEligible) {
                if (StringUtils.hasText(courierOpenid)) {
                    saveEcommerceSplit(record, dist, outOrderNo, subMchid, transactionId, orderId,
                            courierOpenid, WechatSplitOwnerTypes.COURIER.name(), delivery.getCourierId(),
                            courierEarning, "frozen", null);
                } else {
                    saveEcommerceSplit(record, dist, outOrderNo, subMchid, transactionId, orderId,
                            null, WechatSplitOwnerTypes.COURIER.name(), delivery.getCourierId(),
                            courierEarning, "skipped", "快递员微信账户未注册/未校验，待补分");
                }
            }
        } catch (Exception e) {
            log.error("[电商分账] 请求分账失败 订单={} subMchid={} outOrderNo={}", record.getBizId(), subMchid, outOrderNo, e);
            if (platformAmount.compareTo(BigDecimal.ZERO) > 0) {
                saveEcommerceSplit(record, dist, outOrderNo, subMchid, transactionId, null,
                        platformMchId, WechatSplitOwnerTypes.PLATFORM.name(), WalletFlowService.PLATFORM_OWNER_ID,
                        platformAmount, "freeze_failed", truncate(e.getMessage()));
            }
            if (courierEligible) {
                saveEcommerceSplit(record, dist, outOrderNo, subMchid, transactionId, null,
                        courierOpenid, WechatSplitOwnerTypes.COURIER.name(), delivery.getCourierId(),
                        courierEarning, "freeze_failed", truncate(e.getMessage()));
            }
        }
    }

    private void saveEcommerceSplit(PaymentRecord record, DistributionRecord dist, String outOrderNo,
                                    String subMchid, String transactionId, String wxOrderId,
                                    String payeeAccount, String ownerType, String ownerId,
                                    BigDecimal amount, String status, String remark) {
        WechatSplitRecord sr = new WechatSplitRecord();
        sr.setId(idGenerator.generateId("wsr_"));
        sr.setPaymentRecordId(record.getId());
        sr.setOutTradeNo(record.getOutTradeNo());
        sr.setBizType(record.getBizType());
        sr.setOrderId(record.getBizId());
        sr.setSplitNo(idGenerator.generateId("sp_"));
        sr.setPayerAccount(subMchid);
        sr.setPayeeAccount(payeeAccount);
        sr.setFreezeAmount(amount);
        sr.setGroupNo(outOrderNo);
        sr.setWechatOutTradeNo(transactionId);
        sr.setWechatSubTradeNo(wxOrderId);
        sr.setOwnerType(ownerType);
        sr.setOwnerId(ownerId);
        sr.setCurrency("CNY");
        sr.setStatus(status);
        sr.setRemark(remark);
        splitRecordRepository.save(sr);
    }

    /**
     * 售后窗口到期完结划账（解冻剩余资金给分账方）。
     *
     * <p><b>订单级一次解冻（上线前加固）</b>：微信「解冻剩余资金」是<b>交易级</b>操作——同一订单
     * 的 N 个接收方记录只需（且只能）成功解冻一次，剩余资金即解冻回分账方。若逐记录解冻，
     * 首条成功后其余 N−1 条会收到 {@code NOT_ENOUGH}（分账已完成，无需再解冻）而落
     * {@code finish_failed}，被补偿调度无限重试并污染每日对账。因此这里按 groupNo
     * <b>一次解冻、整组标记完结</b>；{@code NOT_ENOUGH} 按幂等成功处理
     * （调度重入 / 同组其他记录已先解冻）。</p>
     */
    @Transactional
    public void finishAfterWindow(WechatSplitRecord split) {
        if (!"frozen".equals(split.getStatus())) {
            return;
        }
        if (isEcommerceSplit(split)) {
            ecommerceFinish(split);
            return;
        }
        try {
            wechatSplitApiService.finishOrder(split.getSplitNo(), split.getWechatOutTradeNo(), resolveSubMchid(split));
        } catch (WechatPayException e) {
            if (!"NOT_ENOUGH".equals(e.getCode())) {
                markFinishFailed(split, e);
                return;
            }
            log.info("[分账] 解冻剩余资金返回 NOT_ENOUGH（该订单已解冻），按成功处理 splitNo={}", split.getSplitNo());
        } catch (Exception e) {
            markFinishFailed(split, e);
            return;
        }
        // 解冻成功（或已解冻）：同组 frozen 记录一并置完结
        for (WechatSplitRecord s : splitRecordRepository.findByGroupNo(split.getGroupNo())) {
            if ("frozen".equals(s.getStatus())) {
                s.setStatus("finished");
                splitRecordRepository.save(s);
            }
        }
    }

    private void markFinishFailed(WechatSplitRecord split, Exception e) {
        split.setStatus("finish_failed");
        split.setRemark(truncate(e.getMessage()));
        log.error("[分账] 完结失败 splitNo={}", split.getSplitNo(), e);
        splitRecordRepository.save(split);
    }

    /** 电商收付通：完结是订单级操作，一次完结后同 groupNo 的 frozen 记录全部置 finished。 */
    private void ecommerceFinish(WechatSplitRecord split) {
        String outOrderNo = split.getGroupNo();
        String subMchid = split.getPayerAccount();
        String transactionId = split.getWechatOutTradeNo();
        if (!StringUtils.hasText(outOrderNo) || !StringUtils.hasText(subMchid) || !StringUtils.hasText(transactionId)) {
            split.setStatus("finish_failed");
            split.setRemark("电商完结参数缺失");
            splitRecordRepository.save(split);
            return;
        }
        try {
            ecommerceSplitApiService.finishSplit(subMchid, transactionId, outOrderNo);
            for (WechatSplitRecord s : splitRecordRepository.findByGroupNo(outOrderNo)) {
                if ("frozen".equals(s.getStatus())) {
                    s.setStatus("finished");
                    splitRecordRepository.save(s);
                }
            }
            // 平台佣金到账后，触发四层树内部结算转账计划生成（幂等，见 MerchantTransferService）
            merchantTransferService.planForFinishedOrder(split.getOrderId());
        } catch (Exception e) {
            split.setStatus("finish_failed");
            split.setRemark(truncate(e.getMessage()));
            log.error("[电商分账] 完结失败 outOrderNo={}", outOrderNo, e);
            splitRecordRepository.save(split);
        }
    }

    /**
     * 完结后尝试自动提现（Path B）。
     * 普通分账下收款方在自身微信商户平台提现，平台侧无代提动作；标记 withdraw_status=withdrawn 让状态机推进。
     * 电商收付通下平台佣金已到账，此处立即驱动四层树内部结算出款（{@code MerchantTransferService}，
     * 幂等；失败由 {@code TransferScheduler} 定时兜底重试）。
     */
    @Transactional
    public void maybeAutoWithdraw(WechatSplitRecord split) {
        if (!"finished".equals(split.getStatus())) {
            return;
        }
        if (!wechatSplitProperties.isAutoWithdrawEnabled()) {
            return;
        }
        split.setWithdrawStatus("withdrawn");
        splitRecordRepository.save(split);
        if (ecommerceProps.isEnabled()) {
            try {
                merchantTransferService.processTransfers();
            } catch (Exception e) {
                log.warn("[分账] 完结后触发内部结算出款失败（由转账调度兜底） splitNo={}: {}",
                        split.getSplitNo(), e.getMessage());
            }
        }
    }

    /** 重试提现失败记录。 */
    @Transactional
    public void retryWithdraw(WechatSplitRecord split) {
        if (!"withdraw_failed".equals(split.getWithdrawStatus())) {
            return;
        }
        split.setWithdrawStatus("withdrawn");
        splitRecordRepository.save(split);
    }

    /** 缺户补分：重新解析账户并重冻（普通分账路径 / 电商补分路径）。 */
    @Transactional
    public void retrySkippedSplitRecords() {
        List<WechatSplitRecord> skipped = splitRecordRepository.findByStatus("skipped");
        for (WechatSplitRecord split : skipped) {
            if (noLongerSplittable(split)) {
                split.setStatus("expired");
                split.setRemark("售后窗口已完结/订单已退款，剩余资金已解冻给商家，不再补分");
                splitRecordRepository.save(split);
                continue;
            }
            if (isEcommerceSplit(split)) {
                retrySkippedEcommerce(split);
            } else {
                String account = resolveAccountNoAny(split.getOwnerType(), split.getOwnerId());
                if (!StringUtils.hasText(account)) {
                    continue;
                }
                retryFreeze(split, account);
            }
            splitRecordRepository.save(split);
        }
    }

    /**
     * 电商收付通：skipped 记录补分（当前覆盖快递员配送费场景）。
     *
     * <p>快递员开户/授权完成后，以<b>新的 out_order_no</b> 对同一笔支付发起请求分账
     * （微信允许同一交易多次请求分账），原分账单的完结不受影响；补分账单独立走完结窗口。
     * 若账户仍未注册则保持 skipped，等待下一轮。</p>
     */
    private void retrySkippedEcommerce(WechatSplitRecord split) {
        if (!WechatSplitOwnerTypes.COURIER.name().equals(split.getOwnerType())) {
            return; // 平台佣金为 MERCHANT_ID 接收方，不存在缺户场景
        }
        if (!isPositive(split.getFreezeAmount())
                || !StringUtils.hasText(split.getWechatOutTradeNo())
                || !StringUtils.hasText(split.getPayerAccount())) {
            return;
        }
        String openid = receiverAccountService.resolveAccountNo(
                WechatSplitOwnerTypes.COURIER.name(), split.getOwnerId());
        if (!StringUtils.hasText(openid)) {
            return;
        }
        String outOrderNo = idGenerator.generateId("grp_");
        try {
            String wxOrderId = ecommerceSplitApiService.requestSplit(split.getPayerAccount(),
                    split.getWechatOutTradeNo(), outOrderNo,
                    EcommerceSplitApiService.receiversOf(EcommerceSplitApiService.receiver(
                            "PERSONAL_OPENID", openid, split.getFreezeAmount(), "订单配送费补分")));
            split.setPayeeAccount(openid);
            split.setGroupNo(outOrderNo);
            split.setWechatSubTradeNo(wxOrderId);
            split.setStatus("frozen");
            split.setRemark(null);
            log.info("[电商分账] 快递员补分成功 splitNo={} orderId={} amount={}",
                    split.getSplitNo(), split.getOrderId(), split.getFreezeAmount());
        } catch (Exception e) {
            split.setRemark("补分失败:" + truncate(e.getMessage()));
            log.warn("[电商分账] 快递员补分失败 splitNo={}: {}", split.getSplitNo(), e.getMessage());
        }
    }

    /** 失败态补偿重试（freeze_failed / finish_failed / cancel_failed）。 */
    @Transactional
    public void retryFailed(WechatSplitRecord split) {
        if (isEcommerceSplit(split)) {
            ecommerceRetryFailed(split);
            return;
        }
        switch (split.getStatus()) {
            case "freeze_failed" -> {
                if (noLongerSplittable(split)) {
                    split.setStatus("expired");
                    split.setRemark("售后窗口已完结/订单已退款，剩余资金已解冻给商家，不再补分");
                    break;
                }
                String account = resolveAccountNoAny(split.getOwnerType(), split.getOwnerId());
                retryFreeze(split, account);
            }
            case "finish_failed" -> {
                try {
                    wechatSplitApiService.finishOrder(split.getSplitNo(), split.getWechatOutTradeNo(), resolveSubMchid(split));
                    split.setStatus("finished");
                } catch (Exception e) {
                    split.setStatus("finish_failed");
                    split.setRemark(truncate(e.getMessage()));
                }
            }
            case "cancel_failed" -> retryReverse(split);
            default -> { /* 非失败态，跳过 */ }
        }
        splitRecordRepository.save(split);
    }

    /** 电商收付通：freeze_failed → 重放请求分账；finish_failed → 重放完结。 */
    private void ecommerceRetryFailed(WechatSplitRecord split) {
        switch (split.getStatus()) {
            case "freeze_failed" -> replayEcommerceFreeze(split);
            case "finish_failed" -> ecommerceFinish(split);
            case "reverse_failed" -> retryReverse(split);
            default -> { /* 非失败态，跳过 */ }
        }
    }

    /** 电商收付通：按同 groupNo 记录重建接收方列表，重新发起请求分账。 */
    private void replayEcommerceFreeze(WechatSplitRecord split) {
        String outOrderNo = split.getGroupNo();
        String subMchid = split.getPayerAccount();
        String transactionId = split.getWechatOutTradeNo();
        if (!StringUtils.hasText(outOrderNo) || !StringUtils.hasText(subMchid) || !StringUtils.hasText(transactionId)) {
            return;
        }
        List<Map<String, Object>> receivers = new ArrayList<>();
        for (WechatSplitRecord s : splitRecordRepository.findByGroupNo(outOrderNo)) {
            if (!StringUtils.hasText(s.getPayeeAccount()) || !isPositive(s.getFreezeAmount())) {
                continue;
            }
            receivers.add(EcommerceSplitApiService.receiver(
                    resolveReceiverType(s.getPayeeAccount()), s.getPayeeAccount(), s.getFreezeAmount(), "物业积分分账重试"));
        }
        if (receivers.isEmpty()) {
            return;
        }
        try {
            String orderId = ecommerceSplitApiService.requestSplit(subMchid, transactionId, outOrderNo, receivers);
            for (WechatSplitRecord s : splitRecordRepository.findByGroupNo(outOrderNo)) {
                s.setWechatSubTradeNo(orderId);
                s.setStatus("frozen");
                s.setRemark(null);
                splitRecordRepository.save(s);
            }
        } catch (Exception e) {
            log.error("[电商分账] 重放分账失败 outOrderNo={}", outOrderNo, e);
        }
    }

    /** 分账退回补偿（订单取消 / 退款，平台分成退回）。兼容首次退回（frozen/finished）与 reverse_failed 重试。 */
    @Transactional
    public void retryReverse(WechatSplitRecord split) {
        if ("frozen".equals(split.getStatus()) || "finished".equals(split.getStatus())) {
            // 首次发起退回（订单取消 / 退款路径）
        } else if (!"reverse_failed".equals(split.getStatus())) {
            return;
        }
        doReverse(split);
    }

    /**
     * 订单取消 / 退款：退回该订单全部已冻结（或已完结）的分账，资金回笼后原路退款才有余额。
     * 幂等：分账退回 out_return_no 确定性生成（普通分账 {@code splitNo_RET} / 电商 {@code ret_}{splitNo}），
     * 重试不会产生重复回退；失败落 reverse_failed 由调度补偿。
     *
     * <p><b>fail-closed（P0-3 修复）</b>：处理完后仍存在未回退（frozen/finished/reverse_failed）记录时
     * 抛出 {@link BizException}，由调用方回滚整个取消 / 退款事务，杜绝「部分回退 + 已发起退款」的
     * 资金中间态。本地状态随事务回滚，重试时微信侧按确定性 out_return_no 幂等处理。</p>
     */
    @Transactional
    public void reverseForOrder(String orderId) {
        List<WechatSplitRecord> splits = splitRecordRepository.findByOrderIdAndStatusIn(
                orderId, List.of("frozen", "finished"));
        for (WechatSplitRecord split : splits) {
            try {
                retryReverse(split);
            } catch (Exception e) {
                log.error("[分账退回] 订单 {} 分账退回异常 splitNo={}", orderId, split.getSplitNo(), e);
            }
        }
        List<WechatSplitRecord> remaining = splitRecordRepository.findByOrderIdAndStatusIn(
                orderId, List.of("frozen", "finished", "reverse_failed"));
        if (!remaining.isEmpty()) {
            throw new BizException(ErrorCode.SPLIT_REVERSE_INCOMPLETE,
                    "订单 " + orderId + " 有 " + remaining.size() + " 笔分账未回退完成，暂不能发起退款");
        }
    }

    private void doReverse(WechatSplitRecord split) {
        if (isEcommerceSplit(split)) {
            // 电商收付通：仅 MERCHANT_ID 接收方可在微信侧回退；个人 openid 不支持回退，
            // 平台先行垫付并登记待追回台账（线下追回后核销）
            String payeeType = resolveReceiverType(split.getPayeeAccount());
            if (!"MERCHANT_ID".equals(payeeType)) {
                recoveryService.recordPersonalReverse(split);
                split.setStatus("reverted");
                split.setRemark("个人类型接收方不支持微信回退，平台垫付待追回（台账已登记）");
                splitRecordRepository.save(split);
                return;
            }
            try {
                // 确定性 out_return_no：崩溃/重试时同一 splitNo 生成同一单号，微信侧幂等，杜绝重复回退
                String outReturnNo = returnNoOf(split.getSplitNo());
                ecommerceSplitApiService.returnSplit(split.getPayerAccount(), split.getWechatSubTradeNo(),
                        split.getGroupNo(), outReturnNo, split.getPayeeAccount(), split.getFreezeAmount());
                split.setStatus("reverted");
                split.setRemark("电商回退成功:" + outReturnNo);
            } catch (Exception e) {
                split.setStatus("reverse_failed");
                split.setRemark(truncate(e.getMessage()));
                log.error("[电商分账] 回退重试失败 splitNo={}", split.getSplitNo(), e);
            }
            splitRecordRepository.save(split);
            return;
        }
        // 服务商/普通分账：仅 MERCHANT_ID 接收方可在微信侧回退；个人 openid 不支持回退，
        // 平台先行垫付并登记待追回台账（与电商收付通模式同一策略）
        String payeeType = resolveReceiverType(split.getPayeeAccount());
        if (!"MERCHANT_ID".equals(payeeType)) {
            recoveryService.recordPersonalReverse(split);
            split.setStatus("reverted");
            split.setRemark("个人类型接收方不支持微信回退，平台垫付待追回（台账已登记）");
            splitRecordRepository.save(split);
            return;
        }
        try {
            wechatSplitApiService.reverseRefund(split.getSplitNo(), split.getWechatOutTradeNo(),
                    resolveSubMchid(split), split.getWechatSubTradeNo(), split.getPayeeAccount(),
                    split.getFreezeAmount());
            split.setStatus("reverted");
        } catch (Exception e) {
            split.setStatus("reverse_failed");
            split.setRemark(truncate(e.getMessage()));
            log.error("[分账退回] 重试失败 splitNo={}", split.getSplitNo(), e);
        }
        splitRecordRepository.save(split);
    }

    private void retryFreeze(WechatSplitRecord split, String account) {
        if (!StringUtils.hasText(account)) {
            split.setStatus("skipped");
            split.setRemark("收款账户未注册/未校验");
            return;
        }
        String subMchid = resolveSubMchid(split);
        if (!StringUtils.hasText(subMchid)) {
            split.setStatus("freeze_failed");
            split.setRemark("支付流水缺 sub_mchid（非服务商通道），无法分账");
            return;
        }
        Receiver receiver = resolveReceiver(split.getOwnerType(), split.getOwnerId());
        split.setPayeeAccount(account);
        try {
            String relationType = relationTypeOf(split.getOwnerType());
            wechatSplitApiService.ensureReceiverAdded(subMchid, account,
                    receiver == null ? null : receiver.accountName(), relationType, customRelationOf(relationType));
            String wxOrderId = wechatSplitApiService.freezeAllocate(split.getSplitNo(), split.getWechatOutTradeNo(),
                    subMchid, account, split.getFreezeAmount(), "物业积分分账-" + split.getOwnerType(),
                    receiver == null ? null : receiver.accountName());
            split.setWechatSubTradeNo(wxOrderId);
            split.setStatus("frozen");
            split.setRemark(null);
        } catch (Exception e) {
            split.setStatus("freeze_failed");
            split.setRemark(truncate(e.getMessage()));
        }
    }

    /**
     * 判断该分账记录所属支付是否为电商收付通通道：以<b>支付流水固化的 pay_mode</b> 为准，
     * 避免切换全局收付通开关后把老的服务商分账记录发到电商接口（或反之）。
     * 历史记录无 pay_mode 时回退全局开关。
     */
    private boolean isEcommerceSplit(WechatSplitRecord split) {
        if (split == null || !StringUtils.hasText(split.getPaymentRecordId())) {
            return ecommerceProps.isEnabled();
        }
        return paymentRecordRepository.findById(split.getPaymentRecordId())
                .map(pr -> WechatPayModes.ECOMMERCE.equals(
                        WechatPayModes.resolve(pr.getPayMode(), pr.getSubMchid(), ecommerceProps.isEnabled())))
                .orElse(ecommerceProps.isEnabled());
    }

    /**
     * 解析收款方账号（平台特判：平台佣金/配送分成落平台自身商户号，无需运营录入账户）。
     */
    private String resolveAccountNoAny(String ownerType, String ownerId) {
        Receiver receiver = resolveReceiver(ownerType, ownerId);
        return receiver == null ? null : receiver.accountNo();
    }

    /**
     * 解析收款方完整信息（账号 + 名称）。平台主体特判落<b>服务商自身商户号</b>（sp_mchid）；
     * 其余主体走 {@code wechat_split_receiver_accounts}（仅 verified 账户可用）。
     *
     * <p>服务商分账模式下，未分出到平台的佣金会随「解冻剩余资金」退回商家特约商户号，
     * 因此平台抽成必须作为 MERCHANT_ID 接收方显式分出（relation_type=SERVICE_PROVIDER）。</p>
     */
    private Receiver resolveReceiver(String ownerType, String ownerId) {
        if (WechatSplitOwnerTypes.PLATFORM.name().equals(ownerType)) {
            String platformMchId = wechatPayProperties.getMchId();
            if (!StringUtils.hasText(platformMchId)) {
                return null;
            }
            return new Receiver(platformMchId, wechatPayProperties.getPlatformReceiverName());
        }
        WechatSplitReceiverAccount acc = receiverAccountService.resolveAccount(ownerType, ownerId);
        if (acc == null || !StringUtils.hasText(acc.getAccountNo())) {
            return null;
        }
        return new Receiver(acc.getAccountNo(), acc.getAccountName());
    }

    /**
     * 添加分账接收方的关系类型：平台自身为 SERVICE_PROVIDER（服务商抽成），
     * 其余主体（物业/协调员/负责人/快递员等）取配置值，缺省 PARTNER（合作伙伴）。
     * 官方可选取值：SERVICE_PROVIDER / STORE / STAFF / STORE_OWNER / PARTNER /
     * HEADQUARTER / BRAND / DISTRIBUTOR / USER / SUPPLIER / CUSTOM。
     */
    private String relationTypeOf(String ownerType) {
        if (WechatSplitOwnerTypes.PLATFORM.name().equals(ownerType)) {
            return "SERVICE_PROVIDER";
        }
        String configured = wechatPayProperties.getReceiverRelationType();
        return StringUtils.hasText(configured) ? configured : "PARTNER";
    }

    /** 官方约束：relation_type=CUSTOM 时 custom_relation 必填（≤10 字），其余取值不得填写。 */
    private String customRelationOf(String relationType) {
        return "CUSTOM".equals(relationType) ? wechatPayProperties.getReceiverCustomRelation() : null;
    }

    /**
     * 解析分账记录对应支付的服务商子商户号（商家特约商户号 = 分账出资方）。
     * 服务商分账必须携带 sub_mchid；直连/历史流水无该字段时返回 null，由调用方 fail-closed。
     */
    private String resolveSubMchid(WechatSplitRecord split) {
        if (split == null || !StringUtils.hasText(split.getPaymentRecordId())) {
            return null;
        }
        return paymentRecordRepository.findById(split.getPaymentRecordId())
                .map(pr -> StringUtils.hasText(pr.getSubMchid()) ? pr.getSubMchid() : null)
                .orElse(null);
    }

    /** 查单（服务商/普通分账）：自动附带该笔分账的 sub_mchid。 */
    public JSONObject querySplitResult(WechatSplitRecord split) {
        return wechatSplitApiService.querySplitResult(split.getSplitNo(), split.getWechatOutTradeNo(),
                resolveSubMchid(split));
    }

    /**
     * 该记录是否已错过可分账时机：同组已完结/回退，或距支付/建单已超过微信 30 天冻结上限。
     * 命中后补冻必然失败（微信已无冻结资金），应置终态 {@code expired}，避免补偿调度无限重试。
     */
    private boolean noLongerSplittable(WechatSplitRecord split) {
        if (split == null) {
            return false;
        }
        if (StringUtils.hasText(split.getGroupNo())) {
            boolean groupClosed = splitRecordRepository.findByGroupNo(split.getGroupNo()).stream()
                    .anyMatch(s -> "finished".equals(s.getStatus())
                            || "withdrawn".equals(s.getStatus())
                            || "reverted".equals(s.getStatus()));
            if (groupClosed) {
                return true;
            }
        }
        LocalDateTime base = split.getCreatedAt();
        if (base == null && StringUtils.hasText(split.getPaymentRecordId())) {
            base = paymentRecordRepository.findById(split.getPaymentRecordId())
                    .map(PaymentRecord::getCreatedAt).orElse(null);
        }
        return base != null && base.plusDays(30).isBefore(LocalDateTime.now());
    }

    /** 该分账记录是否属于电商收付通通道（普通分账的对账调度需跳过，避免调错接口）。 */
    public boolean isEcommerceSplitRecord(WechatSplitRecord split) {
        return isEcommerceSplit(split);
    }

    /** 收款方（账号 + 名称）解析结果。 */
    private record Receiver(String accountNo, String accountName) {
    }

    /**
     * 构建分账计划（服务商/普通分账）：平台抽成 + 四层佣金分成 + 楼长 + 物业经理 + 快递员配送报酬。
     *
     * <p><b>关键口径（对应「不走内部钱包」决策）</b>：除商家货款（留在商家特约商户号、完结时解冻）外，
     * 分账池内<b>每一分钱的归属主体都必须作为接收方分出</b>——服务商分账模式下未分出的资金
     * 将随完结解冻退回商家（资金错配）。
     * 落点：平台佣金/配送分成→平台商户号；快递员/楼长/经理/协调员/负责人→个人零钱；
     * 物业公司→其微信商户号；均按运营录入的微信账户（{@code wechat_split_receiver_accounts}）解析。</p>
     *
     * <p><b>楼长与经理的顺序关系</b>：楼长落库的是「扣除经理抽成后的净额」
     * （{@code buildingLeaderShare}），经理抽成金额 2026-10-18 起落库
     * {@code managerShare}；两者分别作为 BUILDING_LEADER / PROPERTY_MANAGER 接收方分出，
     * 合计等于楼长毛额，与钱包流水（经理流水）口径一致。</p>
     *
     * @param delivery 配送单（含平台配送分成与快递员配送报酬）；无配送单（无配送费场景）传 null
     */
    private List<SplitPlan> buildPlans(DistributionRecord dist, Delivery delivery) {
        List<SplitPlan> plans = new ArrayList<>();
        // 平台：商品佣金分成 + 配送费平台分成（服务商抽成，落平台自身商户号）
        BigDecimal platformAmount = nz(dist.getPlatformShare());
        if (delivery != null) {
            platformAmount = platformAmount.add(nz(delivery.getPlatformEarning()));
        }
        addIfPositive(plans, WechatSplitOwnerTypes.PLATFORM.name(), WalletFlowService.PLATFORM_OWNER_ID, platformAmount);
        addIfPositive(plans, WechatSplitOwnerTypes.PROPERTY.name(), dist.getPropertyCompanyId(), dist.getPropertyShare());
        addIfPositive(plans, WechatSplitOwnerTypes.COORDINATOR.name(), dist.getCoordinatorId(), dist.getCoordinatorShare());
        addIfPositive(plans, WechatSplitOwnerTypes.SECTOR_LEADER.name(), dist.getSectorLeaderId(), dist.getSectorLeaderShare());
        addIfPositive(plans, WechatSplitOwnerTypes.INDIVIDUAL_LEADER.name(), dist.getIndividualLeaderId(), dist.getIndividualLeaderShare());
        // 单元楼长：从物业分成中定向扣除的净额（经理抽成已在毛额中扣除）
        addIfPositive(plans, WechatSplitOwnerTypes.BUILDING_LEADER.name(), dist.getBuildingLeaderId(),
                dist.getBuildingLeaderShare());
        // 物业经理：从楼长毛额中抽取的抽成（主体键用经理 residentId，与钱包流水一致；
        // 历史订单 managerShare 为 null 或经理已停用时该笔不分账，资金留在池内按原内部口径结算）
        if (isPositive(dist.getManagerShare())) {
            String managerResidentId = resolveManagerResidentId(dist.getManagerId());
            if (managerResidentId != null) {
                addIfPositive(plans, WechatSplitOwnerTypes.PROPERTY_MANAGER.name(), managerResidentId,
                        dist.getManagerShare());
            } else {
                log.warn("[分账] 订单 {} 物业经理抽成 {} 元本次不分账：managerId={} 不存在或未激活",
                        dist.getOrderId(), dist.getManagerShare(), dist.getManagerId());
            }
        }
        // 快递员：配送报酬（个人 openid 接收方，落微信零钱）
        if (delivery != null) {
            addIfPositive(plans, WechatSplitOwnerTypes.COURIER.name(), delivery.getCourierId(),
                    nz(delivery.getCourierEarning()));
        }
        return plans;
    }

    /**
     * 物业经理的 residentId（分账记录与钱包流水的统一主体键）。
     * 经理不存在或未激活时返回 null——该笔抽成保持原内部结算口径，不作为分账接收方。
     */
    private String resolveManagerResidentId(String managerId) {
        if (!StringUtils.hasText(managerId)) {
            return null;
        }
        return propertyManagerRepository.findById(managerId)
                .filter(m -> "active".equals(m.getStatus()))
                .map(PropertyManager::getResidentId)
                .orElse(null);
    }

    private void addIfPositive(List<SplitPlan> plans, String ownerType, String ownerId, BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0 && StringUtils.hasText(ownerId)) {
            plans.add(new SplitPlan(ownerType, ownerId, amount));
        }
    }

    private static boolean isPositive(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 电商分账回退单号（确定性）：同一 splitNo 永远生成同一 out_return_no，
     * 崩溃重试 / 补偿调度重试时微信侧按幂等处理，不会重复回退。
     */
    static String returnNoOf(String splitNo) {
        return "ret_" + splitNo;
    }

    private static String resolveReceiverType(String account) {
        if (account == null) {
            return "PERSONAL_OPENID";
        }
        return account.trim().matches("\\d{8,20}") ? "MERCHANT_ID" : "PERSONAL_OPENID";
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String truncate(String s) {
        return s == null ? null : (s.length() > 500 ? s.substring(0, 500) : s);
    }

    /** 单笔分账计划（收款方 + 金额）。 */
    private static final class SplitPlan {
        final String ownerType;
        final String ownerId;
        final BigDecimal amount;

        SplitPlan(String ownerType, String ownerId, BigDecimal amount) {
            this.ownerType = ownerType;
            this.ownerId = ownerId;
            this.amount = amount;
        }
    }
}
