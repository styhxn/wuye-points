package com.wuye.points.dto.rewardgrant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "奖励金/积分发放申请条目")
public class RewardGrantItemResponse {

    @Schema(description = "申请ID")
    private String id;

    @Schema(description = "申请人ID")
    private String applicantId;

    @Schema(description = "申请人角色")
    private String applicantRole;

    @Schema(description = "目标住户ID")
    private String targetResidentId;

    @Schema(description = "目标住户姓名")
    private String targetResidentName;

    @Schema(description = "发放类型 points/reward")
    private String grantType;

    @Schema(description = "积分数量")
    private Integer pointAmount;

    @Schema(description = "归属模式 property/platform")
    private String attributionMode;

    @Schema(description = "申请理由")
    private String reason;

    @Schema(description = "审核状态 pending_audit/approved/rejected")
    private String auditStatus;

    @Schema(description = "审批人ID")
    private String approverId;

    @Schema(description = "审批备注")
    private String approverRemark;

    @Schema(description = "审批时间")
    private LocalDateTime auditedAt;

    @Schema(description = "申请时间")
    private LocalDateTime createdAt;
}
