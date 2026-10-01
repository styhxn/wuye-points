package com.wuye.points.dto.technician;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "技工更新工单状态请求")
public class UpdateTechnicianTaskStatusRequest {

    @NotBlank(message = "status不能为空")
    @Schema(description = "目标状态：pending/assigned/in_progress/completed/cancelled",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "assigned")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
