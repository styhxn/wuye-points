package com.wuye.points.dto.projectleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新项目负责人请求")
public class UpdateProjectLeaderRequest {

    @Schema(description = "所属区域负责人ID", example = "rl_xxxxxxxx")
    private String regionalLeaderId;

    @Schema(description = "姓名", example = "李四")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "项目名称", example = "示范项目")
    private String projectName;

    @Schema(description = "分成比例", example = "0.1000")
    private BigDecimal shareRate;

    @Schema(description = "状态：active/inactive", example = "active")
    private String status;
}
