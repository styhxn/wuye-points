package com.wuye.points.dto.admin.merchantaudit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "商家提现列表项响应")
@Data
@Builder
public class MerchantWithdrawalListItemResponse {

    @Schema(description = "提现记录ID", example = "withdrawal_001")
    private String id;

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "商家名称", example = "张记小厨")
    private String merchantName;

    @Schema(description = "提现金额（元）", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "手续费比例", example = "0.02")
    private BigDecimal feeRate;

    @Schema(description = "手续费（元）", example = "10.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额（元）", example = "490.00")
    private BigDecimal actualAmount;

    @Schema(description = "审核状态", example = "pending")
    private String auditStatus;

    @Schema(description = "审核时间", example = "2026-07-01 14:30:00")
    private String auditedAt;

    @Schema(description = "完成时间", example = "2026-07-01 15:00:00")
    private String completedAt;

    @Schema(description = "创建时间", example = "2026-07-01 10:00:00")
    private String createdAt;
}
