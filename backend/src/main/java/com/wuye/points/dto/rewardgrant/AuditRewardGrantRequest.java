package com.wuye.points.dto.rewardgrant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "审批奖励金/积分发放申请")
public class AuditRewardGrantRequest {

    @Schema(description = "审批动作 approve / reject", example = "approve", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "审批动作不能为空")
    private String action;

    @Schema(description = "审批备注（拒绝时填写原因）", example = "同意发放")
    private String remark;
}
