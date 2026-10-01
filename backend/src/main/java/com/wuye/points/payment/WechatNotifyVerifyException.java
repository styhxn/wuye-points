package com.wuye.points.payment;

/**
 * 微信支付 APIv3 回调验签 / 解密失败。
 *
 * <p>控制器捕获后应应答 {@code {code:"FAIL"}}，由微信按官方策略重推；不应抛 5xx 导致无限重试噪音。
 */
public class WechatNotifyVerifyException extends RuntimeException {

    public WechatNotifyVerifyException(String message) {
        super(message);
    }

    public WechatNotifyVerifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
