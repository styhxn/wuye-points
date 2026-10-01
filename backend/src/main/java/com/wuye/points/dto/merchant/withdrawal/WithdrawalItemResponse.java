package com.wuye.points.dto.merchant.withdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "提现项响应")
public class WithdrawalItemResponse {

    @Schema(description = "提现记录ID", example = "withdrawal_001")
    private String id;

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "商家名称", example = "万达超市")
    private String merchantName;

    @Schema(description = "提现金额", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "手续费比例", example = "0.01")
    private BigDecimal feeRate;

    @Schema(description = "手续费金额", example = "10.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额", example = "990.00")
    private BigDecimal actualAmount;

    @Schema(description = "审核状态", example = "pending", allowableValues = {"pending", "approved", "rejected", "completed"})
    private String status;

    @Schema(description = "审核时间", example = "2024-01-02T00:00:00")
    private String auditedAt;

    @Schema(description = "完成时间", example = "2024-01-03T00:00:00")
    private String completedAt;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;
}
