package com.wuye.points.dto.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplyIndividualLeaderRequest {

    @NotBlank(message = "申请板块不能为空")
    @Size(max = 50, message = "板块名称不超过50字")
    @Schema(description = "申请板块（如 cleaning/repair/security/greening/other）", example = "repair", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sector;

    @Size(max = 50, message = "备注不超过50字")
    @Schema(description = "自我介绍或备注（选填）", example = "我是小区维修技工，从业10年")
    private String remark;
}
