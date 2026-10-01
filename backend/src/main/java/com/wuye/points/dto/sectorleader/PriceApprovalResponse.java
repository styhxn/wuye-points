package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "价格审批记录响应")
public class PriceApprovalResponse {

    @Schema(description = "审批记录ID")
    private String id;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "申请人ID")
    private String applicantId;

    @Schema(description = "申请人角色")
    private String applicantRole;

    @Schema(description = "审批类型")
    private String itemType;

    @Schema(description = "关联对象ID")
    private String itemId;

    @Schema(description = "原值")
    private String oldValue;

    @Schema(description = "新值")
    private String newValue;

    @Schema(description = "申请原因")
    private String reason;

    @Schema(description = "审批状态")
    private String status;

    @Schema(description = "审批人ID")
    private String approverId;

    @Schema(description = "审批备注")
    private String approverRemark;

    @Schema(description = "创建时间")
    private String createdAt;
}
