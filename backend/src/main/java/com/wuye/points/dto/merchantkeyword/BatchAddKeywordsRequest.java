package com.wuye.points.dto.merchantkeyword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量增加商家关键词请求")
public class BatchAddKeywordsRequest {

    @Schema(description = "关键词列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "keywords不能为空")
    private List<String> keywords;
}
