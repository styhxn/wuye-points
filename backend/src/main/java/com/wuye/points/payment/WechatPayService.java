package com.wuye.points.payment;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信官方支付服务（平台微信支付分账 partner JSAPI），实现 {@link PayChannel}。
 *
 * <p>下单走「平台微信支付分账 partner 模式」(/{@code v3/pay/partner/transactions/jsapi})，
 * 与已废弃的 CBK / 扫呗（LCSW）聚合支付无关。返给前端的 {@link WechatJsPayParams} 由前端原样透传 wx.requestPayment。</p>
 */
@Slf4j
@Service
public class WechatPayService implements PayChannel {

    private final WechatHttpClient httpClient;
    private final WechatPayProperties props;
    private final WechatCertManager certManager;
    private final PaymentRecordRepository paymentRecordRepository;
    private final SnowflakeIdGenerator idGenerator;

    public WechatPayService(WechatHttpClient httpClient, WechatPayProperties props,
                            WechatCertManager certManager, PaymentRecordRepository paymentRecordRepository,
                            SnowflakeIdGenerator idGenerator) {
        this.httpClient = httpClient;
        this.props = props;
        this.certManager = certManager;
        this.paymentRecordRepository = paymentRecordRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public boolean isEnabled() {
        return props.isConfigured();
    }

    @Override
    public void validate() {
        if (!props.isConfigured()) {
            throw new WechatPayException("WECHAT_PAY_NOT_CONFIGURED", "微信支付未配置（mchId/apiV3Key/私钥缺失）", null);
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
        record.setCashAmount(cashAmount);
        record.setStatus("pending");
        record.setOutTradeNo(idGenerator.generateId("wxo_"));
        record.setTraceNo(record.getId());
        try {
            return paymentRecordRepository.save(record);
        } catch (DataIntegrityViolationException e) {
            // 唯一约束 (biz_type, biz_id, status) 兜底：并发重复发起支付时，仅第一个请求能建 pending 流水，
            // 其余请求在此拦截，避免同订单生成多条待支付流水（跨实例同样生效，无需分布式锁）。
            log.warn("[支付流水] 重复发起支付被唯一约束拦截 bizType={} bizId={}: {}",
                    bizType, bizId, e.getMessage());
            throw new BizException(ErrorCode.ORDER_CONFLICT, "订单支付处理中，请勿重复提交");
        }
    }

    @Override
    public WechatJsPayParams miniPay(PaymentRecord record, String openId, String orderBody) {
        // 电商收付通模式（流水有真实二级商户号）：走服务商 partner 接口，payer 用 sp_openid。
        // 直连商户模式（无二级商户号）：走 /v3/pay/transactions/jsapi，payer 用 openid。
        if (StringUtils.hasText(record.getSubMchid())) {
            return partnerMiniPay(record, openId, orderBody);
        }
        return directMiniPay(record, openId, orderBody);
    }

    /**
     * 直连商户下单：POST /v3/pay/transactions/jsapi，payer 用 openid，资金直接进商户号 mchid。
     * <p>微信硬规则：服务商不能把自己的 sp_mchid 设为 sub_mchid；普通分账 /v3/profitsharing
     * 只认直连商户号收款。故非收付通场景必须走直连，才能与分账/退款链路自洽。</p>
     */
    private WechatJsPayParams directMiniPay(PaymentRecord record, String openId, String orderBody) {
        LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
        amount.put("total", Integer.parseInt(fen(record.getCashAmount())));
        amount.put("currency", "CNY");

        LinkedHashMap<String, Object> payer = new LinkedHashMap<>();
        payer.put("openid", openId);

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("appid", props.getAppId());
        body.put("mchid", props.getMchId());
        body.put("description", orderBody);
        body.put("out_trade_no", record.getOutTradeNo());
        body.put("notify_url", props.getNotifyUrl());
        body.put("amount", amount);
        body.put("payer", payer);
        // 微信硬规则：需分账的订单下单时必须传 profit_sharing=true，否则后续 /v3/profitsharing 冻结会报
        // 「非分账订单不支持分账」，导致全部分账冻结失败。
        if (props.isProfitSharing()) {
            body.put("profit_sharing", true);
        }

        String resp = httpClient.post(props.getGateway() + "/v3/pay/transactions/jsapi", body);
        String prepayId = parseField(resp, "prepay_id");
        if (prepayId == null) {
            throw new WechatPayException("WECHAT_JSAPI_NO_PREPAY", "微信 JSAPI 下单未返回 prepay_id", resp);
        }
        return buildJsPayParams(prepayId);
    }

    /**
     * 电商收付通下单：POST /v3/pay/partner/transactions/jsapi，payer 用 sp_openid，
     * 用户付款直进「订单商家二级商户号」。本平台为统一单小程序、无商家独立 sub_appid，
     * 故 sp_openid 直接用居民在该小程序下的 openid。
     */
    private WechatJsPayParams partnerMiniPay(PaymentRecord record, String openId, String orderBody) {
        String subMchId = resolveSubMchid(record);
        LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
        amount.put("total", Integer.parseInt(fen(record.getCashAmount())));
        amount.put("currency", "CNY");

        LinkedHashMap<String, Object> payer = new LinkedHashMap<>();
        payer.put("sp_openid", openId);

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("sp_appid", props.getAppId());
        body.put("sp_mchid", props.getMchId());
        body.put("sub_mchid", subMchId);
        body.put("description", orderBody);
        body.put("out_trade_no", record.getOutTradeNo());
        body.put("notify_url", props.getNotifyUrl());
        body.put("amount", amount);
        body.put("payer", payer);

        String resp = httpClient.post(props.getGateway() + "/v3/pay/partner/transactions/jsapi", body);
        String prepayId = parseField(resp, "prepay_id");
        if (prepayId == null) {
            throw new WechatPayException("WECHAT_JSAPI_NO_PREPAY", "微信 JSAPI 下单未返回 prepay_id", resp);
        }
        return buildJsPayParams(prepayId);
    }

    /**
     * 指定入账二级商户号发起支付（电商收付通模式：用户付款直进「订单商家」的二级商户号）。
     * sub_mchid 同时回写支付流水，供后续查单 / 退款 / 电商分账按商家路由。
     */
    @Override
    public WechatJsPayParams miniPay(PaymentRecord record, String openId, String orderBody, String subMchid) {
        if (StringUtils.hasText(subMchid)) {
            record.setSubMchid(subMchid);
            paymentRecordRepository.save(record);
        }
        return miniPay(record, openId, orderBody);
    }

    /**
     * 解析该笔支付的入账二级商户号：仅流水回写值有效（fail-closed）。
     * <p>微信硬规则：服务商不能把 sp_mchid 设为 sub_mchid，缺真实二级商户号必须拒绝，
     * 禁止兜底回退服务商号，否则 partner 下单会被微信业务校验拒绝。</p>
     */
    private String resolveSubMchid(PaymentRecord record) {
        if (record != null && StringUtils.hasText(record.getSubMchid())) {
            return record.getSubMchid();
        }
        throw new WechatPayException("WECHAT_SUB_MCHID_MISSING",
                "电商收付通模式缺少真实二级商户号(sub_mchid)，禁止回退服务商号", null);
    }

    @Override
    public String query(PaymentRecord record) {
        String url;
        if (StringUtils.hasText(record.getSubMchid())) {
            // 电商收付通：partner 模式，按二级商户号查单
            url = props.getGateway() + "/v3/pay/partner/transactions/out-trade-no/" + record.getOutTradeNo()
                    + "?sub_mchid=" + resolveSubMchid(record);
        } else {
            // 直连商户：按 mchid 查单
            url = props.getGateway() + "/v3/pay/transactions/out-trade-no/" + record.getOutTradeNo()
                    + "?mchid=" + props.getMchId();
        }
        String resp = httpClient.get(url);
        return parseField(resp, "trade_state");
    }

    /**
     * 查询微信支付订单号（transaction_id），用于回调丢失、主动查单确认支付后补全分账所需字段。
     * 查单响应体携带 transaction_id；查不到返回 null。
     */
    public String queryTransactionId(PaymentRecord record) {
        if (!StringUtils.hasText(record.getOutTradeNo())) {
            return null;
        }
        String url;
        if (StringUtils.hasText(record.getSubMchid())) {
            url = props.getGateway() + "/v3/pay/partner/transactions/out-trade-no/" + record.getOutTradeNo()
                    + "?sub_mchid=" + resolveSubMchid(record);
        } else {
            url = props.getGateway() + "/v3/pay/transactions/out-trade-no/" + record.getOutTradeNo()
                    + "?mchid=" + props.getMchId();
        }
        String resp = httpClient.get(url);
        return parseField(resp, "transaction_id");
    }

    @Override
    public String refund(PaymentRecord record, BigDecimal refundAmountYuan, String reason) {
        String outRefundNo = idGenerator.generateId("rf_");
        // 电商收付通模式（资金在商家二级商户号）：走 /v3/ecommerce/refunds/apply
        if (StringUtils.hasText(record.getSubMchid())) {
            return ecommerceRefund(record, refundAmountYuan, reason, outRefundNo);
        }
        // 直连商户模式（非收付通）：资金在平台商户号，走通用退款接口 /v3/refund/domestic/refunds
        LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
        amount.put("refund", Integer.parseInt(fen(refundAmountYuan)));
        amount.put("total", Integer.parseInt(fen(record.getCashAmount())));
        amount.put("currency", "CNY");

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("out_trade_no", record.getOutTradeNo());
        body.put("out_refund_no", outRefundNo);
        body.put("reason", reason);
        body.put("notify_url", StringUtils.hasText(props.getRefundNotifyUrl()) ? props.getRefundNotifyUrl() : props.getNotifyUrl());
        body.put("amount", amount);

        String resp = httpClient.post(props.getGateway() + "/v3/refund/domestic/refunds", body);
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
     * 电商收付通退款：POST /v3/ecommerce/refunds/apply（退款从商家二级商户号余额扣减）。
     * 若该笔已发起过分账，退款前需先走分账回退（PERSONAL_OPENID 不可回退，业务侧另行处理）。
     */
    private String ecommerceRefund(PaymentRecord record, BigDecimal refundAmountYuan, String reason, String outRefundNo) {
        LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
        amount.put("refund", Integer.parseInt(fen(refundAmountYuan)));
        amount.put("total", Integer.parseInt(fen(record.getCashAmount())));
        amount.put("currency", "CNY");

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("appid", props.getAppId());
        body.put("sub_mchid", record.getSubMchid());
        body.put("out_trade_no", record.getOutTradeNo());
        body.put("out_refund_no", outRefundNo);
        body.put("reason", reason);
        body.put("amount", amount);
        body.put("notify_url", StringUtils.hasText(props.getRefundNotifyUrl()) ? props.getRefundNotifyUrl() : props.getNotifyUrl());

        String resp = httpClient.post(props.getGateway() + "/v3/ecommerce/refunds/apply", body);
        String returned = parseField(resp, "out_refund_no");
        if (!StringUtils.hasText(returned)) {
            throw new WechatPayException("WECHAT_ECOMMERCE_REFUND_FAIL", "电商收付通退款未返回 out_refund_no", resp);
        }
        record.setOutRefundNo(outRefundNo);
        paymentRecordRepository.save(record);
        return outRefundNo;
    }

    @Override
    public String queryRefund(PaymentRecord record) {
        if (!StringUtils.hasText(record.getOutRefundNo())) {
            return null;
        }
        String url;
        if (StringUtils.hasText(record.getSubMchid())) {
            url = props.getGateway() + "/v3/ecommerce/refunds/out-refund-no/" + record.getOutRefundNo()
                    + "?sub_mchid=" + record.getSubMchid();
        } else {
            url = props.getGateway() + "/v3/refund/domestic/refunds/out-refund-no/" + record.getOutRefundNo();
        }
        String resp = httpClient.get(url);
        return parseField(resp, "status");
    }

    @Override
    public void close(PaymentRecord record) {
        if (StringUtils.hasText(record.getSubMchid())) {
            // 电商收付通：服务商关单（按二级商户号）
            LinkedHashMap<String, Object> body = new LinkedHashMap<>();
            body.put("sp_mchid", props.getMchId());
            body.put("sub_mchid", resolveSubMchid(record));
            httpClient.post(props.getGateway() + "/v3/pay/partner/transactions/out-trade-no/" + record.getOutTradeNo() + "/close", body);
        } else {
            // 直连商户：普通关单（按 mchid）
            LinkedHashMap<String, Object> body = new LinkedHashMap<>();
            body.put("mchid", props.getMchId());
            httpClient.post(props.getGateway() + "/v3/pay/transactions/out-trade-no/" + record.getOutTradeNo() + "/close", body);
        }
        record.setStatus("closed");
        paymentRecordRepository.save(record);
    }

    @Override
    public boolean verifyNotify(Map<String, Object> body) {
        // 微信通道回调由 PaymentCompletionService 统一验签解密，本方法不单独使用
        return true;
    }

    @Override
    public void markPaidFromNotify(Map<String, Object> body, PaymentRecord record) {
        Object transactionId = body == null ? null : body.get("transaction_id");
        record.setChannelTradeNo(transactionId == null ? null : String.valueOf(transactionId));
        record.setStatus("paid");
        record.setPaidAt(LocalDateTime.now());
        paymentRecordRepository.save(record);
    }

    @Override
    public void markPaidFromQuery(PaymentRecord record) {
        record.setStatus("paid");
        record.setPaidAt(LocalDateTime.now());
        paymentRecordRepository.save(record);
    }

    private WechatJsPayParams buildJsPayParams(String prepayId) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String message = props.getAppId() + "\n" + timeStamp + "\n" + nonceStr + "\nprepay_id=" + prepayId + "\n";
        String paySign = WechatCrypto.sign(httpClient.getPrivateKey(), message);
        WechatJsPayParams params = new WechatJsPayParams();
        params.setAppId(props.getAppId());
        params.setTimeStamp(timeStamp);
        params.setNonceStr(nonceStr);
        params.setPkg("prepay_id=" + prepayId);
        params.setSignType("RSA");
        params.setPaySign(paySign);
        return params;
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
}
