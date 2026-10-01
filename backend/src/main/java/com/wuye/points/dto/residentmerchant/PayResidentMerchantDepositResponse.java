package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "缴纳保证金响应")
public class PayResidentMerchantDepositResponse {

    private String depositId;
    private String status;
    private String statusCode;

    @Schema(description = "支付状态：pending=待支付（需前端调起微信支付）/ frozen=已缴", example = "pending")
    private String payStatus;

    @Schema(description = "支付流水ID（两段式支付时返回）", example = "pr_xxx")
    private String paymentRecordId;

    @Schema(description = "微信小程序调起支付参数（payParams非空时，前端调 wx.requestPayment，保证金保持待缴，支付成功后由回调确认）")
    private com.wuye.points.payment.WechatJsPayParams payParams;
}
