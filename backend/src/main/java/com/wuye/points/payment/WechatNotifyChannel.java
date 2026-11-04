package com.wuye.points.payment;

/**
 * 微信支付回调通道：服务商与直连商户各有独立的验签公钥、APIv3 密钥与回调地址，
 * 必须由对应入口接收，不能共用一个验签器。
 */
public enum WechatNotifyChannel {

    /** 服务商通道（wechat.pay）：商城订单支付结果。 */
    PARTNER,

    /** 直连商户通道（wechat.direct）：平台自营业务支付结果。 */
    DIRECT
}
