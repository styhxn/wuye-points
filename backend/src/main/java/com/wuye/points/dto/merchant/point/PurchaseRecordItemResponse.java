package com.wuye.points.dto.merchant.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "购买记录项响应")
public class PurchaseRecordItemResponse {

    @Schema(description = "购买记录ID", example = "purchase_001")
    private String id;

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "商家名称", example = "万达超市")
    private String merchantName;

    @Schema(description = "购买积分数量", example = "1000")
    private Integer pointAmount;

    @Schema(description = "支付金额", example = "100.00")
    private BigDecimal payAmount;

    @Schema(description = "审核状态", example = "approved", allowableValues = {"pending_audit", "approved", "rejected"})
    private String status;

    @Schema(description = "审核备注", example = "审核通过")
    private String auditRemark;

    @Schema(description = "剩余积分", example = "500")
    private Integer remainingPoints;

    @Schema(description = "审核时间", example = "2024-01-02T00:00:00")
    private String auditedAt;

    @Schema(description = "审核人ID", example = "admin_001")
    private String auditorId;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;
}
