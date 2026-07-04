package com.wuye.points.dto.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业币提现记录响应")
public class CoinWithdrawalItemResponse {

    @Schema(description = "提现记录ID", example = "cw_m3n4o")
    private String id;

    @Schema(description = "商户ID", example = "mer_p5q6r")
    private String merchantId;

    @Schema(description = "物业币数量", example = "100.00")
    private BigDecimal coinAmount;

    @Schema(description = "现金金额", example = "95.00")
    private BigDecimal cashAmount;

    @Schema(description = "手续费费率", example = "0.05")
    private BigDecimal feeRate;

    @Schema(description = "手续费金额", example = "5.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额", example = "95.00")
    private BigDecimal actualAmount;

    @Schema(description = "审核状态", example = "待审核")
    private String auditStatus;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
