package com.wuye.points.dto.projectleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建项目负责人请求")
public class CreateProjectLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "业主ID", example = "res_xxxxxxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String residentId;

    @Schema(description = "所属区域负责人ID", example = "rl_xxxxxxxx")
    private String regionalLeaderId;

    @NotBlank(message = "name不能为空")
    @Schema(description = "姓名", example = "李四", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "项目名称", example = "示范项目")
    private String projectName;

    @Schema(description = "分成比例（从区域负责人中抽取）", example = "0.1000")
    private BigDecimal shareRate;

    @Schema(description = "状态：active/inactive", example = "active")
    private String status;
}
