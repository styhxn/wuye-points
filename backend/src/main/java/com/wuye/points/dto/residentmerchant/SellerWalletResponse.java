package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 业主卖货独立钱包（仅提现，不可消费）
 */
@Data
@Builder
@Schema(description = "业主卖货钱包概览")
public class SellerWalletResponse {

    @Schema(description = "住户ID", example = "res_demo001")
    private String residentId;

    @Schema(description = "累计卖货收益（已结算订单差额汇总）", example = "1250.00")
    private BigDecimal totalProfit;

    @Schema(description = "已提现金额（审核通过并已完成）", example = "500.00")
    private BigDecimal withdrawnAmount;

    @Schema(description = "在途金额（待审核+已通过待打款）", example = "100.00")
    private BigDecimal pendingAmount;

    @Schema(description = "可提现余额（累计收益 - 已提现 - 在途）", example = "650.00")
    private BigDecimal withdrawableAmount;
}