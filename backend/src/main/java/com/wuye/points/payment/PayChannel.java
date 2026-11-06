package com.wuye.points.payment;

import com.wuye.points.entity.PaymentRecord;

import java.math.BigDecimal;

/**
 * 统一支付通道接口。
 *
 * <p>统一微信支付实现（{@code WechatPayService}），业务侧只依赖本接口。平台接入两个独立商户主体：
 * 直连商户（平台自营业务）与服务商（商城订单 + 官方分账），由支付流水固化的 {@code pay_mode} 路由；
 * 返给前端的 {@link WechatJsPayParams} 字段（appId/timeStamp/nonceStr/package/signType/paySign）
 * 由前端原样透传给 wx.requestPayment。</p>
 */
public interface PayChannel {

    boolean isEnabled();

    /**
     * 直连商户通道是否就绪（平台自营业务：物业费/充值/食堂/广告/服务预约等）。
     * 默认与 {@link #isEnabled()} 一致；双通道实现按直连商户实际配置判断。
     */
    default boolean isDirectEnabled() {
        return isEnabled();
    }

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
     * 二次发起支付前作废遗留的待支付流水（用户取消微信支付后重付的场景）。
     *
     * <p>先查单：微信侧已支付成功的流水不能关，否则用户会撞上「已关单但已支付 → 原路退款」，
     * 此时抛 {@code ORDER_CONFLICT} 提示稍后再试；未支付则调微信关单，查单/关单失败（未配置、
     * 无 out_trade_no 等）退化为本地置 closed，保证二次支付不被永久挡住。</p>
     *
     * <p>调用方随后新建 pending 流水前无需再 flush：本方法内部已强制下刷 UPDATE。</p>
     *
     * @param bizLabel            日志用的业务名，如「物业币充值」
     * @param alreadyPaidMessage  上一笔实际已支付时返回给用户的文案
     */
    void closeForRetry(PaymentRecord record, String bizLabel, String alreadyPaidMessage);

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
