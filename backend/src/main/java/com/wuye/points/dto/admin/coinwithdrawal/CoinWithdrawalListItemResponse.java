package com.wuye.points.dto.admin.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "积分提现列表项响应")
public class CoinWithdrawalListItemResponse {

    @Schema(description = "提现记录ID", example = "WD001")
    private String id;

    @Schema(description = "商户ID", example = "MER001")
    private String merchantId;

    @Schema(description = "商户名称", example = "某某商店")
    private String merchantName;

    @Schema(description = "积分金额", example = "1000.00")
    private BigDecimal coinAmount;

    @Schema(description = "现金金额", example = "950.00")
    private BigDecimal cashAmount;

    @Schema(description = "手续费率", example = "0.05")
    private BigDecimal feeRate;

    @Schema(description = "手续费金额", example = "50.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额", example = "900.00")
    private BigDecimal actualAmount;

    @Schema(description = "审核状态", example = "pending")
    private String auditStatus;

    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private String createdAt;
}
