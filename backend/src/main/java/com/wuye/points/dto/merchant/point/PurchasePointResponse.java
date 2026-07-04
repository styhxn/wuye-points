package com.wuye.points.dto.merchant.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "购买积分响应")
public class PurchasePointResponse {

    @Schema(description = "购买记录ID", example = "purchase_001")
    private String id;

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "购买积分数量", example = "1000")
    private Integer pointAmount;

    @Schema(description = "支付金额", example = "100.00")
    private BigDecimal payAmount;

    @Schema(description = "审核状态", example = "pending", allowableValues = {"pending_audit", "approved", "rejected"})
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;
}
