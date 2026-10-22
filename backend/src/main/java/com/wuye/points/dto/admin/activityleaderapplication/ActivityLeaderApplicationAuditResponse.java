package com.wuye.points.dto.admin.activityleaderapplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "活动组组长申请审核响应")
public class ActivityLeaderApplicationAuditResponse {

    @Schema(description = "申请记录ID")
    private String applicationId;

    @Schema(description = "审核结果 approved/rejected")
    private String auditStatus;

    @Schema(description = "申请人住户ID")
    private String residentId;

    @Schema(description = "申请人姓名")
    private String residentName;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "审核时间(ISO8601)")
    private String auditedAt;

    @Schema(description = "审核通过时创建的组长身份记录ID")
    private String activityLeaderIdentityId;
}
