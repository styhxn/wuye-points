package com.wuye.points.dto.priceapproval;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "价格审批列表项响应")
public class PriceApprovalListItemResponse {

    @Schema(description = "审批ID", example = "pa_xxxx")
    private String id;

    @Schema(description = "物业公司ID", example = "pc_xxxx")
    private String propertyCompanyId;

    @Schema(description = "申请人ID", example = "res_xxxx")
    private String applicantId;

    @Schema(description = "申请人角色", example = "coordinator")
    private String applicantRole;

    @Schema(description = "审批类型", example = "product_price")
    private String itemType;

    @Schema(description = "目标对象ID", example = "p_xxxx")
    private String itemId;

    @Schema(description = "原值", example = "10.00")
    private String oldValue;

    @Schema(description = "新值", example = "12.00")
    private String newValue;

    @Schema(description = "原因", example = "原材料价格上涨")
    private String reason;

    @Schema(description = "状态: pending/approved/rejected", example = "pending")
    private String status;

    @Schema(description = "审批人ID", example = "res_xxxx")
    private String approverId;

    @Schema(description = "审批备注", example = "同意调整")
    private String approverRemark;

    @Schema(description = "审批时间", example = "2024-01-01 10:00:00")
    private String approvedAt;

    @Schema(description = "创建时间", example = "2024-01-01 09:00:00")
    private String createdAt;
}
