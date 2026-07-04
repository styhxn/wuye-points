package com.wuye.points.dto.opinion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建意见箱请求")
public class CreateOpinionBoxRequest {

    @NotBlank(message = "标题不能为空")
    @Schema(description = "意见箱标题", example = "小区绿化改造建议", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "意见箱内容", example = "建议对小区中心花园进行绿化升级改造", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "开始时间，为空则立即开始", example = "2025-01-01 00:00:00")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @Schema(description = "结束时间", example = "2025-01-31 23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endTime;
}
