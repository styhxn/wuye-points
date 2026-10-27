package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物业币充值支付状态。
 *
 * <p>前端在拉起微信支付后轮询本接口，只有 {@code status=paid} 且 {@code rechargedAt} 非空才表示已到账。
 * 不要把 {@code wx.requestPayment:success} 直接当成到账。</p>
 */
@Data
@Builder
@Schema(description = "物业币充值支付状态")
public class RechargeStatusResponse {

    @Schema(description = "内部支付流水ID（pr_ 前缀）", example = "pr_abc123")
    private String paymentRecordId;

    @Schema(description = "商户订单号（wxo_ 前缀，微信侧对账使用）", example = "wxo_abc123")
    private String outTradeNo;

    @Schema(description = "支付状态 pending/paid/closed/refunding/refunded/amount_mismatch/failed",
            example = "paid")
    private String status;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "到账后个人物业币余额；未到账时为 null", example = "220.50")
    private BigDecimal balanceAfter;

    @Schema(description = "到账后家庭物业币余额；无家庭或未到账时为 null", example = "600.00")
    private BigDecimal familyBalanceAfter;

    @Schema(description = "到账时间；未到账时为 null", example = "2026-01-01T00:00:00.000Z")
    private String rechargedAt;

    @Schema(description = "关闭/对账失败原因", example = "支付金额与预下单金额不一致")
    private String failReason;
}
