package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "举报处理请求")
public class HandleContentReportRequest {

    @Schema(description = "处理动作 accept(通过举报，自动隐藏内容)/reject(驳回)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理动作不能为空")
    private String action;

    @Schema(description = "处理备注")
    private String handleRemark;
}
