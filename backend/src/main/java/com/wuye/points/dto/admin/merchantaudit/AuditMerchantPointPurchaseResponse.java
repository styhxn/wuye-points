package com.wuye.points.dto.admin.merchantaudit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "审核商家积分采购响应")
@Data
@Builder
public class AuditMerchantPointPurchaseResponse {

    @Schema(description = "采购记录ID", example = "purchase_001")
    private String id;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "申请积分数量", example = "1000")
    private Integer pointAmount;

    @Schema(description = "实际积分数量", example = "1000")
    private Integer actualPointAmount;

    @Schema(description = "商家积分余额", example = "5000")
    private Integer merchantPointBalance;

    @Schema(description = "审核时间", example = "2026-07-01 14:30:00")
    private String auditedAt;

    @Schema(description = "审核人ID", example = "admin_001")
    private String auditorId;
}
