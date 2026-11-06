package com.wuye.points.dto.scanpay;

import com.wuye.points.payment.WechatJsPayParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** 扫码付款创建响应：payParams 原样传给 wx.requestPayment。 */
@Schema(description = "扫码付款创建响应")
@Data
@Builder
public class ScanPayCreateResponse {

    @Schema(description = "扫码付款单ID（轮询用）")
    private String scanPayId;

    @Schema(description = "收款商家名称")
    private String merchantName;

    @Schema(description = "付款金额（元）")
    private BigDecimal amount;

    @Schema(description = "支付流水ID")
    private String paymentRecordId;

    @Schema(description = "微信商户订单号")
    private String outTradeNo;

    @Schema(description = "pending（待支付）；支付回调后由轮询接口变为 paid")
    private String status;

    @Schema(description = "微信小程序支付参数，原样透传 wx.requestPayment")
    private WechatJsPayParams payParams;
}
