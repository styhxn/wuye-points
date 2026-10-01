package com.wuye.points.dto.admin.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditIndividualLeaderApplicationRequest {

    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "approved|rejected", message = "auditStatus只能是approved或rejected")
    @Schema(description = "审核结果", example = "approved", requiredMode = Schema.RequiredMode.REQUIRED)
    private String auditStatus;

    @Size(max = 200, message = "拒绝原因不超过200字")
    @Schema(description = "拒绝原因（reject时必填）", example = "暂不满足条件")
    private String rejectReason;
}
