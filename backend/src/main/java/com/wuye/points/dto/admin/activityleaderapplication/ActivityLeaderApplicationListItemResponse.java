package com.wuye.points.dto.admin.activityleaderapplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "活动组组长申请列表项")
public class ActivityLeaderApplicationListItemResponse {

    @Schema(description = "申请记录ID")
    private String id;

    @Schema(description = "申请人住户ID")
    private String residentId;

    @Schema(description = "申请人姓名")
    private String residentName;

    @Schema(description = "申请人手机号（脱敏）")
    private String residentPhone;

    @Schema(description = "擅长领域/活动类型")
    private String activityField;

    @Schema(description = "自我介绍/备注")
    private String remark;

    @Schema(description = "审核状态 pending/approved/rejected")
    private String auditStatus;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "审核通过后创建的组长身份记录ID")
    private String activityLeaderIdentityId;

    @Schema(description = "审核时间(ISO8601)")
    private String auditedAt;

    @Schema(description = "申请时间(ISO8601)")
    private String createdAt;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;
}
