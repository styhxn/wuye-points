package com.wuye.points.dto.admin.merchantaudit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "商家积分采购列表项响应")
@Data
@Builder
public class MerchantPointPurchaseListItemResponse {

    @Schema(description = "采购记录ID", example = "purchase_001")
    private String id;

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "商家名称", example = "张记小厨")
    private String merchantName;

    @Schema(description = "申请积分数量", example = "1000")
    private Integer pointAmount;

    @Schema(description = "支付金额（元）", example = "100.00")
    private BigDecimal payAmount;

    @Schema(description = "审核状态", example = "pending")
    private String auditStatus;

    @Schema(description = "剩余积分", example = "5000")
    private Integer remainingPoints;

    @Schema(description = "审核时间", example = "2026-07-01 14:30:00")
    private String auditedAt;

    @Schema(description = "审核人ID", example = "admin_001")
    private String auditorId;

    @Schema(description = "创建时间", example = "2026-07-01 10:00:00")
    private String createdAt;
}
