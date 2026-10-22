package com.wuye.points.dto.activityleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "活动组组长申请响应")
public class ApplyActivityLeaderResponse {

    @Schema(description = "申请记录ID")
    private String applicationId;

    @Schema(description = "状态：none=未申请 / pending=审核中 / approved=已通过 / rejected=已驳回")
    private String status;

    @Schema(description = "擅长领域/活动类型")
    private String activityField;

    @Schema(description = "自我介绍/备注")
    private String remark;

    @Schema(description = "驳回原因（rejected 时返回）")
    private String rejectReason;

    @Schema(description = "状态描述")
    private String message;
}
