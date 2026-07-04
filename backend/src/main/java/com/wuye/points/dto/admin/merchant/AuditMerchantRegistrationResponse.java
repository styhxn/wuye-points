package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "审核商家入驻申请响应")
@Data
@Builder
public class AuditMerchantRegistrationResponse {

    @Schema(description = "商家ID", example = "merchant_001")
    private String id;

    @Schema(description = "商家名称", example = "张记小厨")
    private String name;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "审核结果", example = "approved")
    private String auditResult;

    @Schema(description = "拒绝原因", example = "资料不完整")
    private String rejectReason;

    @Schema(description = "商家等级", example = "gold")
    private String merchantLevel;

    @Schema(description = "经营类别", example = "餐饮")
    private String category;

    @Schema(description = "操作人ID", example = "admin_001")
    private String operatorId;

    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "审核时间", example = "2026-07-01 14:30:00")
    private String auditedAt;
}
