package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "设置帖子可见范围请求")
public class SetPostVisibilityRequest {

    @NotBlank(message = "visibilityType不能为空")
    @Schema(description = "可见范围：community=本小区可见；family=仅指定住户可见", example = "family", requiredMode = Schema.RequiredMode.REQUIRED)
    private String visibilityType;

    @Schema(description = "当 visibilityType=family 时必填，可见的指定住户ID", example = "res_001")
    private String visibleResidentId;
}
