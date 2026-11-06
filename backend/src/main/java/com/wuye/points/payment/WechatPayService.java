package com.wuye.points.payment;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * 微信官方支付服务，实现 {@link PayChannel}；按流水固化的通道路由到两个独立商户主体。
 *
 * <ul>
 *   <li><b>直连商户</b>（{@code wechat.direct}）：平台自营业务（物业费/充值/食堂/广告/服务预约等），
 *       走 {@code /v3/pay/transactions/jsapi}，资金进平台直连商户号，不做微信分账；</li>
 *   <li><b>服务商</b>（{@code wechat.pay}，sp_mchid）：商城订单，走
 *       {@code /v3/pay/partner/transactions/jsapi}，资金进商家特约商户号，下单即标记
 *       {@code settle_info.profit_sharing=true}，订单完成后走官方分账；</li>
 *   <li><b>电商收付通</b>：同为 partner 下单，但分账/退款走 {@code /v3/ecommerce/*}（需 ICP/EDI，二期）。</li>
 * </ul>
 *
 * <p><b>路由原则</b>：预下单时把 {@code pay_mode} 与 {@code mch_id} 写入流水，之后查单/退款/关单
 * 一律按流水通道选择对应的 HTTP 客户端与接口，禁止按“当前配置”猜测——运营调整配置不会影响历史流水。</p>
 */
@Slf4j
@Service
public class WechatPayService implements PayChannel {

    private final WechatHttpClient partnerClient;
    private final WechatHttpClient directClient;
    private final WechatPayProperties partnerProps;
    private final WechatDirectProperties directProps;
    private final EcommerceSplitProperties ecommerceProps;
    private final PaymentRecordRepository paymentRecordRepository;
    private final SnowflakeIdGenerator idGenerator;

    public WechatPayService(@Qualifier("wechatHttpClient") WechatHttpClient partnerClient,
                            @Qualifier("directWechatHttpClient") WechatHttpClient directClient,
                            WechatPayProperties partnerProps,
                            WechatDirectProperties directProps,
                            EcommerceSplitProperties ecommerceProps,
                            PaymentRecordRepository paymentRecordRepository,
                            SnowflakeIdGenerator idGenerator) {
        this.partnerClient = partnerClient;
        this.directClient = directClient;
        this.partnerProps = partnerProps;
        this.directProps = directProps;
        this.ecommerceProps = ecommerceProps;
        this.paymentRecordRepository = paymentRecordRepository;
        this.idGenerator = idGenerator;
    }

    /** 任一通道就绪即视为在线支付可用（具体业务在下单时校验对应通道）。 */
    @Override
    public boolean isEnabled() {
        return isDirectEnabled() || isPartnerEnabled();
    }

    /** 直连商户通道是否就绪（平台自营业务使用）。 */
    @Override
    public boolean isDirectEnabled() {
        return directProps.isConfigured();
    }

    /** 服务商通道是否就绪（商城订单 + 官方分账使用）。 */
    public boolean isPartnerEnabled() {
        return partnerProps.isConfigured();
    }

    @Override
    public void validate() {
        if (!isEnabled()) {
            throw new WechatPayException("WECHAT_PAY_NOT_CONFIGURED",
                    "微信支付未配置（服务商 wechat.pay / 直连 wechat.direct 均未就绪）", null);
        }
    }

    @Override
    public PaymentRecord createIntent(String bizType, String bizId, String residentId,
                                      String propertyCompanyId, String paymentMethod, BigDecimal amount,
                                      int pointUsed, BigDecimal coinUsed, BigDecimal cashAmount) {
        PaymentRecord record = new PaymentRecord();
        record.setId(idGenerator.generateId("pr_"));
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setResidentId(residentId);
        record.setPropertyCompanyId(propertyCompanyId);
        record.setPaymentMethod(paymentMethod);
        record.setAmount(amount);
        record.setPointUsed(pointUsed);
        record.setCoinUsed(coinUsed);
        // payment_records.directed_balance_used 为 NOT NULL：必须显式落 0，
        // 否则 Hibernate 静态 INSERT 写 NULL 触发「Column ... cannot be null」约束冲突。
        record.setDirectedBalanceUsed(BigDecimal.ZERO);
        record.setCashAmount(cashAmount);
        record.setStatus("pending");
        record.setOutTradeNo(idGenerator.generateId("wxo_"));
        record.setTraceNo(record.getId());
        try {
            return paymentRecordRepository.save(record);
        } catch (DataIntegrityViolationException e) {
            // 唯一约束 (biz_type, biz_id, active_flag) 兜底：并发重复发起支付时，仅第一个请求能建 pending 流水，
            // 其余请求在此拦截，避免同订单生成多条待支付流水（跨实例同样生效，无需分布式锁）。
            log.warn("[支付流水] 重复发起支付被唯一约束拦截 bizType={} bizId={}: {}",
                    bizType, bizId, e.getMessage());
            throw new BizException(ErrorCode.ORDER_CONFLICT, "订单支付处理中，请勿重复提交");
        }
    }

    /**
     * 直连商户下单（平台自营业务）：POST /v3/pay/transactions/jsapi。
     * 资金直接进平台直连商户号，无二级商户号、不做微信分账。
     */
    @Override
    public WechatJsPayParams miniPay(PaymentRecord record, String openId, String orderBody) {
        requireDirectConfigured();
        record.setPayMode(WechatPayModes.DIRECT);
        record.setMchId(directProps.getMchId());
        paymentRecordRepository.save(record);

        LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
        amount.put("total", Integer.parseInt(fen(record.getCashAmount())));
        amount.put("currency", "CNY");

        LinkedHashMap<String, Object> payer = new LinkedHashMap<>();
        payer.put("openid", openId);

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("appid", directProps.getAppId());
        body.put("mchid", directProps.getMchId());
        body.put("description", orderBody);
        body.put("out_trade_no", record.getOutTradeNo());
        body.put("notify_url", directProps.getNotifyUrl());
        body.put("amount", amount);
        body.put("payer", payer);

        String resp = directClient.post(directProps.getGateway() + "/v3/pay/transactions/jsapi", body);
        String prepayId = parseField(resp, "prepay_id");
        if (prepayId == null) {
            throw new WechatPayException("WECHAT_JSAPI_NO_PREPAY", "微信 JSAPI 下单未返回 prepay_id", resp);
        }
        return buildJsPayParams(prepayId, record.getOutTradeNo(), directClient, directProps.getAppId());
    }

    /**
     * 服务商下单（商城订单）：POST /v3/pay/partner/transactions/jsapi，payer 用 sp_openid，
     * 用户付款直进「订单商家特约商户号」。本平台为统一小程序、商家无独立 sub_appid，
     * 故 sp_openid 直接用居民在该小程序下的 openid。
     *
     * <p>{@code settle_info.profit_sharing} 必须为 true：缺失时微信将该笔订单标记为「非分账订单」，
     * 后续 {@code /v3/profitsharing/orders} 会以「非分账订单不支持分账」拒绝。凡走服务商通道的
     * 订单都必然需要分账，故此处强制开启，不依赖配置开关。</p>
     */
    @Override
    public WechatJsPayParams miniPay(PaymentRecord record, String openId, String orderBody, String subMchid) {
        if (!StringUtils.hasText(subMchid)) {
            throw new WechatPayException("WECHAT_SUB_MCHID_MISSING",
                    "服务商模式缺少商家特约商户号(sub_mchid)，禁止回退服务商号", null);
        }
        requirePartnerConfigured();
        boolean ecommerce = ecommerceProps.isEnabled();
        record.setSubMchid(subMchid);
        record.setPayMode(ecommerce ? WechatPayModes.ECOMMERCE : WechatPayModes.PARTNER);
        record.setMchId(partnerProps.getMchId());
        paymentRecordRepository.save(record);

        LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
        amount.put("total", Integer.parseInt(fen(record.getCashAmount())));
        amount.put("currency", "CNY");

        LinkedHashMap<String, Object> payer = new LinkedHashMap<>();
        payer.put("sp_openid", openId);

        LinkedHashMap<String, Object> settleInfo = new LinkedHashMap<>();
        settleInfo.put("profit_sharing", true);

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("sp_appid", partnerProps.getAppId());
        body.put("sp_mchid", partnerProps.getMchId());
        body.put("sub_mchid", subMchid);
        body.put("description", orderBody);
        body.put("out_trade_no", record.getOutTradeNo());
        body.put("notify_url", partnerProps.getNotifyUrl());
        body.put("amount", amount);
        body.put("payer", payer);
        body.put("settle_info", settleInfo);

        String resp = partnerClient.post(partnerProps.getGateway() + "/v3/pay/partner/transactions/jsapi", body);
        String prepayId = parseField(resp, "prepay_id");
        if (prepayId == null) {
            throw new WechatPayException("WECHAT_JSAPI_NO_PREPAY", "微信服务商 JSAPI 下单未返回 prepay_id", resp);
        }
        return buildJsPayParams(prepayId, record.getOutTradeNo(), partnerClient, partnerProps.getAppId());
    }

    @Override
    public String query(PaymentRecord record) {
        Channel ch = channelOf(record);
        String url;
        if (ch.partner()) {
            url = ch.props().getGateway() + "/v3/pay/partner/transactions/out-trade-no/" + record.getOutTradeNo()
                    + "?sp_mchid=" + ch.props().getMchId()
                    + "&sub_mchid=" + requireSubMchid(record, ch.mode());
        } else {
            url = ch.props().getGateway() + "/v3/pay/transactions/out-trade-no/" + record.getOutTradeNo()
                    + "?mchid=" + ch.props().getMchId();
        }
        return parseField(ch.client().get(url), "trade_state");
    }

    /**
     * 查询微信支付订单号（transaction_id），用于回调丢失、主动查单确认支付后补全分账所需字段。
     * 查单响应体携带 transaction_id；查不到返回 null。
     */
    public String queryTransactionId(PaymentRecord record) {
        if (!StringUtils.hasText(record.getOutTradeNo())) {
            return null;
        }
        Channel ch = channelOf(record);
        String url;
        if (ch.partner()) {
            url = ch.props().getGateway() + "/v3/pay/partner/transactions/out-trade-no/" + record.getOutTradeNo()
                    + "?sp_mchid=" + ch.props().getMchId()
                    + "&sub_mchid=" + requireSubMchid(record, ch.mode());
        } else {
            url = ch.props().getGateway() + "/v3/pay/transactions/out-trade-no/" + record.getOutTradeNo()
                    + "?mchid=" + ch.props().getMchId();
        }
        return parseField(ch.client().get(url), "transaction_id");
    }

    @Override
    public String refund(PaymentRecord record, BigDecimal refundAmountYuan, String reason) {
        Channel ch = channelOf(record);
        String outRefundNo = idGenerator.generateId("rf_");
        String resp = WechatPayModes.ECOMMERCE.equals(ch.mode())
                ? ecommerceRefund(record, ch, refundAmountYuan, reason, outRefundNo)
                : domesticRefund(record, ch, refundAmountYuan, reason, outRefundNo);
        String returned = parseField(resp, "out_refund_no");
        if (!StringUtils.hasText(returned)) {
            throw new WechatPayException("WECHAT_REFUND_FAIL", "微信退款未返回 out_refund_no", resp);
        }
        // 落 out_refund_no：RefundQueryScheduler 依赖它轮询退款终态，缺失会导致流水卡在 refunding
        record.setOutRefundNo(outRefundNo);
        paymentRecordRepository.save(record);
        return outRefundNo;
    }

    /**
     * 直连 / 服务商普通退款：POST /v3/refund/domestic/refunds。
     * 服务商模式必须携带 sub_mchid（退款从特约商户号扣减），签名主体为 sp_mchid。
     */
    private String domesticRefund(PaymentRecord record, Channel ch, BigDecimal refundAmountYuan,
                                  String reason, String outRefundNo) {
        LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
        amount.put("refund", Integer.parseInt(fen(refundAmountYuan)));
        amount.put("total", Integer.parseInt(fen(record.getCashAmount())));
        amount.put("currency", "CNY");

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        if (ch.partner()) {
            body.put("sub_mchid", requireSubMchid(record, ch.mode()));
        }
        body.put("out_trade_no", record.getOutTradeNo());
        body.put("out_refund_no", outRefundNo);
        body.put("reason", reason);
        body.put("notify_url", notifyUrlOf(ch));
        body.put("amount", amount);

        return ch.client().post(ch.props().getGateway() + "/v3/refund/domestic/refunds", body);
    }

    /**
     * 电商收付通退款：POST /v3/ecommerce/refunds/apply（退款从商家二级商户号余额扣减）。
     * 若该笔已发起过分账，退款前需先走分账回退（个人 openid 不可回退，业务侧另行处理）。
     */
    private String ecommerceRefund(PaymentRecord record, Channel ch, BigDecimal refundAmountYuan,
                                   String reason, String outRefundNo) {
        LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
        amount.put("refund", Integer.parseInt(fen(refundAmountYuan)));
        amount.put("total", Integer.parseInt(fen(record.getCashAmount())));
        amount.put("currency", "CNY");

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("appid", ch.props().getAppId());
        body.put("sub_mchid", requireSubMchid(record, ch.mode()));
        body.put("out_trade_no", record.getOutTradeNo());
        body.put("out_refund_no", outRefundNo);
        body.put("reason", reason);
        body.put("amount", amount);
        body.put("notify_url", notifyUrlOf(ch));

        return ch.client().post(ch.props().getGateway() + "/v3/ecommerce/refunds/apply", body);
    }

    @Override
    public String queryRefund(PaymentRecord record) {
        if (!StringUtils.hasText(record.getOutRefundNo())) {
            return null;
        }
        Channel ch = channelOf(record);
        String url;
        if (WechatPayModes.ECOMMERCE.equals(ch.mode())) {
            url = ch.props().getGateway() + "/v3/ecommerce/refunds/out-refund-no/" + record.getOutRefundNo()
                    + "?sub_mchid=" + requireSubMchid(record, ch.mode());
        } else if (WechatPayModes.PARTNER.equals(ch.mode())) {
            url = ch.props().getGateway() + "/v3/refund/domestic/refunds/out-refund-no/" + record.getOutRefundNo()
                    + "?sub_mchid=" + requireSubMchid(record, ch.mode());
        } else {
            url = ch.props().getGateway() + "/v3/refund/domestic/refunds/out-refund-no/" + record.getOutRefundNo();
        }
        return parseField(ch.client().get(url), "status");
    }

    @Override
    public void close(PaymentRecord record) {
        Channel ch = channelOf(record);
        if (ch.partner()) {
            // 服务商/电商：按 sp_mchid + sub_mchid 关单
            LinkedHashMap<String, Object> body = new LinkedHashMap<>();
            body.put("sp_mchid", ch.props().getMchId());
            body.put("sub_mchid", requireSubMchid(record, ch.mode()));
            ch.client().post(ch.props().getGateway() + "/v3/pay/partner/transactions/out-trade-no/"
                    + record.getOutTradeNo() + "/close", body);
        } else {
            // 直连商户：按 mchid 关单
            LinkedHashMap<String, Object> body = new LinkedHashMap<>();
            body.put("mchid", ch.props().getMchId());
            ch.client().post(ch.props().getGateway() + "/v3/pay/transactions/out-trade-no/"
                    + record.getOutTradeNo() + "/close", body);
        }
        record.setStatus("closed");
        paymentRecordRepository.save(record);
    }

    @Override
    public void closeForRetry(PaymentRecord record, String bizLabel, String alreadyPaidMessage) {
        try {
            if ("SUCCESS".equals(query(record))) {
                throw new BizException(ErrorCode.ORDER_CONFLICT, alreadyPaidMessage);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[{}] 旧流水查单失败，继续尝试关单 recordId={}", bizLabel, record.getId(), e);
        }
        try {
            close(record);
            log.info("[{}] 关闭遗留待支付流水 recordId={} outTradeNo={}",
                    bizLabel, record.getId(), record.getOutTradeNo());
        } catch (Exception e) {
            log.warn("[{}] 旧流水微信关单失败，本地置 closed recordId={}", bizLabel, record.getId(), e);
            record.setStatus("closed");
            record.setErrorMsg("重新发起支付，旧流水关闭");
            paymentRecordRepository.save(record);
        }
        // 必须显式下刷：Hibernate 刷写顺序是 INSERT 早于 UPDATE，同一事务里紧随其后的 createIntent
        // 若先落库，会撞线上唯一键 uk_payment_records_biz_pending(biz_type, biz_id, active_flag)
        // （active_flag 是生成列 if(status='pending','1',NULL)），二次支付必然 409 Duplicate entry
        // ——2026-11-06 物业币充值已在线上复现。该键只存在于生产库，H2 用例拦不住，故留此注释。
        paymentRecordRepository.flush();
    }

    @Override
    public void markPaidFromQuery(PaymentRecord record) {
        record.setStatus("paid");
        record.setPaidAt(LocalDateTime.now());
        paymentRecordRepository.save(record);
    }

    private WechatJsPayParams buildJsPayParams(String prepayId, String outTradeNo,
                                               WechatHttpClient client, String appId) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String message = appId + "\n" + timeStamp + "\n" + nonceStr + "\nprepay_id=" + prepayId + "\n";
        String paySign = WechatCrypto.sign(client.getPrivateKey(), message);
        WechatJsPayParams params = new WechatJsPayParams();
        params.setAppId(appId);
        params.setTimeStamp(timeStamp);
        params.setNonceStr(nonceStr);
        params.setPkg("prepay_id=" + prepayId);
        params.setSignType("RSA");
        params.setPaySign(paySign);
        params.setOutTradeNo(outTradeNo);
        return params;
    }

    /** 按流水固化的通道解析出「客户端 + 配置 + 是否 partner 系」。 */
    private Channel channelOf(PaymentRecord record) {
        String mode = WechatPayModes.resolve(record.getPayMode(), record.getSubMchid(), ecommerceProps.isEnabled());
        if (WechatPayModes.ECOMMERCE.equals(mode) || WechatPayModes.PARTNER.equals(mode)) {
            return new Channel(mode, partnerClient, partnerProps, true);
        }
        return new Channel(WechatPayModes.DIRECT, directClient, directProps, false);
    }

    private String requireSubMchid(PaymentRecord record, String mode) {
        if (record == null || !StringUtils.hasText(record.getSubMchid())) {
            throw new WechatPayException("WECHAT_SUB_MCHID_MISSING",
                    "该笔支付为 " + mode + " 通道但缺少 sub_mchid，无法调用微信接口 recordId="
                            + (record == null ? null : record.getId()), null);
        }
        return record.getSubMchid();
    }

    private String notifyUrlOf(Channel ch) {
        return StringUtils.hasText(ch.props().getRefundNotifyUrl())
                ? ch.props().getRefundNotifyUrl() : ch.props().getNotifyUrl();
    }

    private void requireDirectConfigured() {
        if (!isDirectEnabled()) {
            throw new WechatPayException("WECHAT_DIRECT_NOT_CONFIGURED",
                    "直连商户通道未配置（wechat.direct.enabled/mch-id/私钥/APIv3 密钥/证书序列号），"
                            + "平台自营业务暂不可在线支付", null);
        }
    }

    private void requirePartnerConfigured() {
        if (!isPartnerEnabled()) {
            throw new WechatPayException("WECHAT_PARTNER_NOT_CONFIGURED",
                    "服务商通道未配置（wechat.pay.enabled/mch-id/私钥/APIv3 密钥/证书序列号），"
                            + "商城订单暂不可在线支付", null);
        }
    }

    private static String fen(BigDecimal yuan) {
        if (yuan == null) {
            return "0";
        }
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private static String parseField(String resp, String key) {
        try {
            JSONObject obj = JSONUtil.parseObj(resp);
            return obj.getStr(key);
        } catch (Exception e) {
            return null;
        }
    }

    /** 通道路由结果：模式 + HTTP 客户端 + 该通道配置。 */
    private record Channel(String mode, WechatHttpClient client, WechatMerchantProperties props, boolean partner) {
    }
}
