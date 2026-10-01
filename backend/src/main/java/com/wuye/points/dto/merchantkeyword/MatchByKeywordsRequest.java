package com.wuye.points.dto.merchantkeyword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "按关键词匹配商家请求")
public class MatchByKeywordsRequest {

    @Schema(description = "关键词列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "keywords不能为空")
    private List<String> keywords;

    @Schema(description = "物业公司ID（未登录时可指定范围）", example = "pc_xxx")
    private String propertyCompanyId;
}
