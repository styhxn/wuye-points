package com.wuye.points.dto.canteen;

import com.wuye.points.payment.WechatJsPayParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-发起购买额度响应
 */
@Data
@Builder
@Schema(description = "发起购买额度响应")
public class QuotaPurchaseResponse {

    @Schema(description = "购买流水ID", example = "cqp_xxx")
    private String purchaseId;

    @Schema(description = "申请购买额度金额", example = "100.00")
    private BigDecimal quotaAmount;

    @Schema(description = "手续费金额", example = "2.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账额度", example = "98.00")
    private BigDecimal actualQuota;

    @Schema(description = "支付金额", example = "100.00")
    private BigDecimal payAmount;

    @Schema(description = "支付流水ID", example = "pr_xxx")
    private String paymentRecordId;

    @Schema(description = "状态 pending/paid", example = "pending")
    private String status;

    @Schema(description = "微信小程序调起支付参数（payParams非空时，前端调 wx.requestPayment，支付成功由回调确认到账）")
    private WechatJsPayParams payParams;
}
