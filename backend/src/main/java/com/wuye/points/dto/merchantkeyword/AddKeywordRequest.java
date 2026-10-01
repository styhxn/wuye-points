package com.wuye.points.dto.merchantkeyword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "增加商家关键词请求")
public class AddKeywordRequest {

    @Schema(description = "关键词", example = "水果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "keyword不能为空")
    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

    @Schema(description = "权重，越大越优先匹配", example = "10")
    private Integer weight;
}
