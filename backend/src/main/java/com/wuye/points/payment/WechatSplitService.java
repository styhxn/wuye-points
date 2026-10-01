package com.wuye.points.payment;

import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.MerchantWechatAccount;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.WechatSplitReceiverAccount;
import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.service.MerchantTransferService;
import com.wuye.points.service.SplitRecoveryService;
import com.wuye.points.service.WalletFlowService;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 微信分账业务服务（两级式分账状态机编排）。
 *
 * <p>支持两套资金模型，由 {@code wechat-ecommerce.enabled} 切换：</p>
 * <ul>
 *   <li><b>普通分账</b>（/{@code v3/profitsharing/*}，默认）：支付资金归集到平台二级商户，
 *       按订单分成（DistributionRecord）逐收款方冻结 → 售后窗口到期完结划账；</li>
 *   <li><b>电商收付通</b>（/{@code v3/ecommerce/profitsharing/*}）：用户付款直进「商家二级商户号」，
 *       平台主动发起分账——佣金/配送费划给平台商户号与快递员（PERSONAL_OPENID），货款留存商家，
 *       完结时解冻。资金不经平台归集，避免二清。</li>
 * </ul>
 *
 * <p>① 支付成功后 {@link #freezeOnPaymentSuccess} 按订单分成冻结 / 发起电商分账；
 * ② 售后窗口到期 {@link #finishAfterWindow} 完结划账；退款 / 取消走 {@link #retryReverse} 退回。</p>
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
    private final WechatPayProperties wechatPayProperties;
    private final WechatPayService wechatPayService;
    private final DistributionRecordRepository distributionRecordRepository;
    private final MerchantWechatAccountRepository merchantWechatAccountRepository;
    private final DeliveryRepository deliveryRepository;
    private final com.wuye.points.repository.PaymentRecordRepository paymentRecordRepository;
    private final MerchantTransferService merchantTransferService;
    private final SplitRecoveryService recoveryService;
    private final SnowflakeIdGenerator idGenerator;

    /** 业务总开关（普通分账是否启用）。 */
    public boolean isEnabled() {
        return wechatSplitProperties.isEnabled();
    }

    /**
     * 支付成功后分账（幂等：已存在同单分账记录则跳过）。
     * 仅订单类业务参与分账；其余（物业费/充值等）走平台内部钱包，不在此冻结。
     * 电商收付通模式下：按商家二级商户号 + 平台/快递员接收方发起一次请求分账。
     */
    @Transactional
    public void freezeOnPaymentSuccess(PaymentRecord record) {
        if (!PaymentBizType.ORDER.equals(record.getBizType())) {
            return;
        }
        if (splitRecordRepository.existsByOrderId(record.getBizId())) {
            log.info("[分账] 订单 {} 已存在分账记录，跳过", record.getBizId());
            return;
        }
        DistributionRecord dist = distributionRecordRepository.findFirstByOrderId(record.getBizId()).orElse(null);
        if (dist == null) {
            log.info("[分账] 订单 {} 无分成记录，跳过分账", record.getBizId());
            return;
        }
        if (ecommerceProps.isEnabled()) {
            ecommerceFreeze(record, dist);
            return;
        }
        // ===== 普通分账（资金归集平台后逐收款方冻结） =====
        String payer = StringUtils.hasText(wechatPayProperties.getPlatformSubMchId())
                ? wechatPayProperties.getPlatformSubMchId() : wechatPayProperties.getMchId();
        // 微信分账 transaction_id 必须是「微信支付订单号」；回调丢失、主动查单确认支付时未落库，先补全
        String transactionId = record.getChannelTradeNo();
        if (!StringUtils.hasText(transactionId)) {
            transactionId = wechatPayService.queryTransactionId(record);
            if (StringUtils.hasText(transactionId)) {
                record.setChannelTradeNo(transactionId);
                paymentRecordRepository.save(record);
            } else {
                log.warn("[分账] 订单 {} 缺微信支付订单号(transaction_id)，分账冻结跳过，待对账补全", record.getBizId());
                return;
            }
        }
        String groupNo = idGenerator.generateId("grp_");
        for (SplitPlan plan : buildPlans(dist)) {
            String splitNo = idGenerator.generateId("sp_");
            WechatSplitRecord sr = new WechatSplitRecord();
            sr.setId(idGenerator.generateId("wsr_"));
            sr.setPaymentRecordId(record.getId());
            sr.setOutTradeNo(record.getOutTradeNo());
            sr.setWechatOutTradeNo(transactionId);
            sr.setBizType(record.getBizType());
            sr.setOrderId(record.getBizId());
            sr.setSplitNo(splitNo);
            sr.setPayerAccount(payer);
            sr.setOwnerType(plan.ownerType);
            sr.setOwnerId(plan.ownerId);
            sr.setFreezeAmount(plan.amount);
            sr.setGroupNo(groupNo);
            sr.setCurrency("CNY");
            WechatSplitReceiverAccount acc = receiverAccountService.resolveAccount(plan.ownerType, plan.ownerId);
            if (acc == null || !StringUtils.hasText(acc.getAccountNo())) {
                sr.setStatus("skipped");
                sr.setRemark("收款账户未注册/未校验");
                splitRecordRepository.save(sr);
                continue;
            }
            sr.setPayeeAccount(acc.getAccountNo());
            try {
                // 个人 openid 接收方必须传加密姓名（平台证书 OAEP），否则微信冻结报姓名缺失
                String subTradeNo = wechatSplitApiService.freezeAllocate(splitNo, transactionId,
                        payer, acc.getAccountNo(), plan.amount, "物业积分分账-" + plan.ownerType,
                        receiverNameOf(acc.getAccountNo(), acc.getAccountName()));
                sr.setWechatSubTradeNo(subTradeNo);
                sr.setStatus("frozen");
            } catch (Exception e) {
                sr.setStatus("freeze_failed");
                sr.setRemark(truncate(e.getMessage()));
                log.error("[分账] 冻结失败 splitNo={} ownerType={}", splitNo, plan.ownerType, e);
            }
            splitRecordRepository.save(sr);
        }
    }

    /**
     * 电商收付通模式：发起一笔请求分账（receivers 含平台 MERCHANT_ID + 快递员 PERSONAL_OPENID）。
     * 分账金额 = 佣金总额(distributableAmount) + 配送结算基数(deliveryFee − courierEarning 归平台)；
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

        // 配送信息：配送费直分快递员
        Delivery delivery = deliveryRepository.findByOrderId(record.getBizId()).orElse(null);
        BigDecimal courierEarning = delivery != null ? nz(delivery.getCourierEarning()) : BigDecimal.ZERO;
        BigDecimal deliveryFee = delivery != null ? nz(delivery.getFee()) : BigDecimal.ZERO;
        // 平台应收 = 佣金 + 配送结算基数中归平台部分（基数 ≈ 用户实付配送费，减免差额由钱包补贴流水体现）
        BigDecimal platformAmount = money(nz(dist.getDistributableAmount())
                .add(deliveryFee).subtract(courierEarning));
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

    /** 电商收付通模式：配送完成后补分（配送费直分快递员）。供配送完成流程 / 定时任务调用，幂等。 */
    @Transactional
    public void freezeCourierDeliveryEarning(Delivery delivery) {
        if (!ecommerceProps.isEnabled() || delivery == null) {
            return;
        }
        BigDecimal courierEarning = nz(delivery.getCourierEarning());
        if (courierEarning.compareTo(BigDecimal.ZERO) <= 0 || !StringUtils.hasText(delivery.getCourierId())) {
            return;
        }
        String orderId = delivery.getOrderId();
        if (splitRecordRepository.existsByOrderIdAndOwnerType(orderId, WechatSplitOwnerTypes.COURIER.name())) {
            return;
        }
        DistributionRecord dist = distributionRecordRepository.findFirstByOrderId(orderId).orElse(null);
        if (dist == null) {
            return;
        }
        PaymentRecord record = paymentRecordFor(orderId);
        if (record == null || !StringUtils.hasText(record.getChannelTradeNo())) {
            log.warn("[电商分账] 订单 {} 支付流水/交易单号缺失，配送费补分跳过", orderId);
            return;
        }
        MerchantWechatAccount mwa = merchantWechatAccountRepository
                .findByMerchantIdAndStatus(dist.getMerchantId(), "active").orElse(null);
        if (mwa == null) {
            log.warn("[电商分账] 商家 {} 未进件，配送费补分跳过", dist.getMerchantId());
            return;
        }
        String courierOpenid = receiverAccountService.resolveAccountNo(
                WechatSplitOwnerTypes.COURIER.name(), delivery.getCourierId());
        if (!StringUtils.hasText(courierOpenid)) {
            log.info("[电商分账] 快递员 {} 微信账户未注册，配送费待补分", delivery.getCourierId());
            return;
        }
        String outOrderNo = idGenerator.generateId("grp_");
        try {
            String wxOrderId = ecommerceSplitApiService.requestSplit(mwa.getSubMchid(),
                    record.getChannelTradeNo(), outOrderNo,
                    EcommerceSplitApiService.receiversOf(EcommerceSplitApiService.receiver(
                            "PERSONAL_OPENID", courierOpenid, courierEarning, "订单配送费")));
            saveEcommerceSplit(record, dist, outOrderNo, mwa.getSubMchid(), record.getChannelTradeNo(), wxOrderId,
                    courierOpenid, WechatSplitOwnerTypes.COURIER.name(), delivery.getCourierId(),
                    courierEarning, "frozen", null);
        } catch (Exception e) {
            log.error("[电商分账] 配送费补分失败 orderId={}", orderId, e);
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

    private PaymentRecord paymentRecordFor(String orderId) {
        return paymentRecordRepository.findByBizId(orderId).orElse(null);
    }

    /** 售后窗口到期完结划账（解冻剩余资金给商家）。 */
    @Transactional
    public void finishAfterWindow(WechatSplitRecord split) {
        if (!"frozen".equals(split.getStatus())) {
            return;
        }
        if (ecommerceProps.isEnabled()) {
            ecommerceFinish(split);
            return;
        }
        try {
            wechatSplitApiService.finishOrder(split.getSplitNo(), split.getWechatOutTradeNo(), split.getWechatSubTradeNo());
            split.setStatus("finished");
        } catch (Exception e) {
            split.setStatus("finish_failed");
            split.setRemark(truncate(e.getMessage()));
            log.error("[分账] 完结失败 splitNo={}", split.getSplitNo(), e);
        }
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
     * 完结后尝试自动提现（Path B 占位）。
     * 普通分账下收款方在自身微信商户平台提现，平台侧无代提动作；标记 withdraw_status=withdrawn 让状态机推进。
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

    /** 缺户补分：重新解析账户并重冻（普通分账路径）。 */
    @Transactional
    public void retrySkippedSplitRecords() {
        if (ecommerceProps.isEnabled()) {
            return;
        }
        List<WechatSplitRecord> skipped = splitRecordRepository.findByStatus("skipped");
        for (WechatSplitRecord split : skipped) {
            String account = receiverAccountService.resolveAccountNo(split.getOwnerType(), split.getOwnerId());
            if (!StringUtils.hasText(account)) {
                continue;
            }
            retryFreeze(split, account);
            splitRecordRepository.save(split);
        }
    }

    /** 失败态补偿重试（freeze_failed / finish_failed / cancel_failed）。 */
    @Transactional
    public void retryFailed(WechatSplitRecord split) {
        if (ecommerceProps.isEnabled()) {
            ecommerceRetryFailed(split);
            return;
        }
        switch (split.getStatus()) {
            case "freeze_failed" -> {
                String account = receiverAccountService.resolveAccountNo(split.getOwnerType(), split.getOwnerId());
                retryFreeze(split, account);
            }
            case "finish_failed" -> {
                try {
                    wechatSplitApiService.finishOrder(split.getSplitNo(), split.getWechatOutTradeNo(), split.getWechatSubTradeNo());
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
     * 幂等：分账退回 out_return_no 确定性生成（普通分账）/ 每笔重试幂等；失败落 reverse_failed 由调度补偿。
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
    }

    private void doReverse(WechatSplitRecord split) {
        if (ecommerceProps.isEnabled()) {
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
                String outReturnNo = idGenerator.generateId("ret_");
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
        try {
            wechatSplitApiService.reverseRefund(split.getSplitNo(), split.getWechatOutTradeNo(),
                    split.getWechatSubTradeNo(), split.getPayeeAccount(), split.getFreezeAmount());
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
        WechatSplitReceiverAccount acc = receiverAccountService.resolveAccount(split.getOwnerType(), split.getOwnerId());
        split.setPayeeAccount(account);
        try {
            String subTradeNo = wechatSplitApiService.freezeAllocate(split.getSplitNo(), split.getWechatOutTradeNo(),
                    split.getPayerAccount(), account, split.getFreezeAmount(), "物业积分分账-" + split.getOwnerType(),
                    acc == null ? null : receiverNameOf(acc.getAccountNo(), acc.getAccountName()));
            split.setWechatSubTradeNo(subTradeNo);
            split.setStatus("frozen");
            split.setRemark(null);
        } catch (Exception e) {
            split.setStatus("freeze_failed");
            split.setRemark(truncate(e.getMessage()));
        }
    }

    /** 个人 openid 接收方（非纯数字商户号）冻结时必须传加密姓名；商户号接收方不需要。 */
    private static String receiverNameOf(String accountNo, String accountName) {
        if (StringUtils.hasText(accountNo) && accountNo.trim().matches("\\d{8,20}")) {
            return null; // MERCHANT_ID 无需姓名
        }
        return accountName;
    }

    private List<SplitPlan> buildPlans(DistributionRecord dist) {
        List<SplitPlan> plans = new ArrayList<>();
        addIfPositive(plans, WechatSplitOwnerTypes.PROPERTY.name(), dist.getPropertyCompanyId(), dist.getPropertyShare());
        addIfPositive(plans, WechatSplitOwnerTypes.COORDINATOR.name(), dist.getCoordinatorId(), dist.getCoordinatorShare());
        addIfPositive(plans, WechatSplitOwnerTypes.SECTOR_LEADER.name(), dist.getSectorLeaderId(), dist.getSectorLeaderShare());
        addIfPositive(plans, WechatSplitOwnerTypes.INDIVIDUAL_LEADER.name(), dist.getIndividualLeaderId(), dist.getIndividualLeaderShare());
        return plans;
    }

    private void addIfPositive(List<SplitPlan> plans, String ownerType, String ownerId, BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0 && StringUtils.hasText(ownerId)) {
            plans.add(new SplitPlan(ownerType, ownerId, amount));
        }
    }

    private static boolean isPositive(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
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
