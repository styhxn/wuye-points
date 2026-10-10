package com.wuye.points.payment;

import com.wuye.points.entity.PaymentRecord;

import java.math.BigDecimal;

/**
 * 统一支付通道接口。
 *
 * <p>支付通道抽象：统一微信支付（平台微信支付分账 partner 模式）实现，业务侧只依赖本接口。
 *
 * <p>微信侧下单走「平台微信支付分账 partner JSAPI」(/{@code v3/pay/partner/transactions/jsapi})，
 * 返给前端的 {@link WechatJsPayParams} 字段（appId/timeStamp/nonceStr/package/signType/paySign）由前端原样透传给 wx.requestPayment。
 */
public interface PayChannel {

    boolean isEnabled();

    void validate();

    PaymentRecord createIntent(String bizType, String bizId, String residentId,
                               String propertyCompanyId, String paymentMethod, BigDecimal amount,
                               int pointUsed, BigDecimal coinUsed, BigDecimal cashAmount);

    WechatJsPayParams miniPay(PaymentRecord record, String openId, String orderBody);

    /**
     * 指定入账二级商户号发起支付（电商收付通模式：用户付款直进「订单商家」的二级商户号）。
     * 默认实现仅委托 {@link #miniPay(PaymentRecord, String, String)}，实现类可覆盖以按商家路由。
     */
    default WechatJsPayParams miniPay(PaymentRecord record, String openId, String orderBody, String subMchid) {
        return miniPay(record, openId, orderBody);
    }

    /** 查单，返回微信交易状态枚举字符串（如 SUCCESS / NOTPAY / CLOSED）。 */
    String query(PaymentRecord record);

    /** 退款，返回微信退款单号（out_refund_no）。 */
    String refund(PaymentRecord record, BigDecimal refundAmountYuan, String reason);

    String queryRefund(PaymentRecord record);

    void close(PaymentRecord record);

    /**
     * 查单确认已支付后落账（仅置 paid + paidAt）。
     *
     * <p>注意：微信支付回调不走本接口的方法——回调统一由
     * {@code PaymentCompletionService.handleWechatNotify} 验签 + 解密 + 金额校验后落账，
     * 不存在「按回调 body 直接置 paid」的旁路。
     */
    void markPaidFromQuery(PaymentRecord record);

    static String fen(BigDecimal yuan) {
        return yuan.multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
    }
}
