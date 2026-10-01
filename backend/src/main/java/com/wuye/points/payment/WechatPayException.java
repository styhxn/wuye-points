package com.wuye.points.payment;

/** 微信支付调用异常，携带微信返回的错误码/信息。 */
public class WechatPayException extends RuntimeException {
    private final String code;
    private final String wxMessage;

    public WechatPayException(String code, String wxMessage, String detail) {
        super("微信支付异常 code=" + code + " message=" + wxMessage + " detail=" + detail);
        this.code = code;
        this.wxMessage = wxMessage;
    }

    public String getCode() {
        return code;
    }

    public String getWxMessage() {
        return wxMessage;
    }
}
