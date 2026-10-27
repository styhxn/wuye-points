package com.wuye.points.dto.coin;

import com.wuye.points.payment.WechatJsPayParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "充值物业币响应（预下单）")
public class RechargeCoinResponse {

    @Schema(description = "内部支付流水ID（pr_ 前缀）", example = "pr_abc123")
    private String paymentRecordId;

    @Schema(description = "商户订单号（wxo_ 前缀，微信侧对账使用）", example = "wxo_abc123")
    private String outTradeNo;

    @Schema(description = "支付状态，预下单固定为 pending", example = "pending")
    private String status;

    @Schema(description = "微信小程序支付参数，前端原样传给 wx.requestPayment", requiredMode = Schema.RequiredMode.REQUIRED)
    private WechatJsPayParams payParams;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "已废弃：充值成功后通过充值状态接口查询", example = "cr_abc123", deprecated = true)
    private String recordId;

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "已废弃：预下单时为空，到账后通过充值状态接口查询", example = "220.50", deprecated = true)
    private BigDecimal balanceAfter;

    @Schema(description = "已废弃：预下单时为空，到账后通过充值状态接口查询", example = "600.00", deprecated = true)
    private BigDecimal familyBalanceAfter;

    @Schema(description = "已废弃：预下单时为空，到账后通过充值状态接口查询", example = "2026-01-01T00:00:00.000Z", deprecated = true)
    private String rechargedAt;
}
