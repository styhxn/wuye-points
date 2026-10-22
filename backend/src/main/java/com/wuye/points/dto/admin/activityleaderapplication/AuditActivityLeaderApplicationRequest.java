package com.wuye.points.dto.admin.activityleaderapplication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "审核活动组组长申请请求")
public class AuditActivityLeaderApplicationRequest {

    @NotBlank(message = "auditStatus不能为空")
    @Schema(description = "审核结果：approved=通过 / rejected=驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    private String auditStatus;

    @Schema(description = "驳回原因（rejected 时必填）")
    private String rejectReason;
}
