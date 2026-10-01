package com.wuye.points.dto.merchantkeyword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新关键词权重请求")
public class UpdateKeywordRequest {

    @Schema(description = "权重，越大越优先匹配", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "weight不能为空")
    private Integer weight;
}
