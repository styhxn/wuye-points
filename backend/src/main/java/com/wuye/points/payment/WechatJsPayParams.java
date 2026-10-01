package com.wuye.points.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信 JSAPI 支付参数，返给前端后由 {@code wx.requestPayment} 原样透传。
 *
 * <p>字段名与微信小程序支付要求一致：appId / timeStamp / nonceStr / package / signType / paySign。
 * 其中 {@code package} 为 Java 保留字，内部以 {@code pkg} 承载并序列化回 {@code package}。</p>
 */
@Data
public class WechatJsPayParams implements Serializable {

    private String appId;

    private String timeStamp;

    private String nonceStr;

    @JsonProperty("package")
    private String pkg;

    private String signType;

    private String paySign;
}
