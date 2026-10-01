package com.wuye.points.dto.merchantkeyword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "关键词匹配的商家响应")
public class MatchedMerchantResponse {

    @Schema(description = "商家ID", example = "mch_abc12345")
    private String merchantId;

    @Schema(description = "商家名称", example = "幸福超市")
    private String name;

    @Schema(description = "命中的关键词列表")
    private List<String> matchedKeywords;

    @Schema(description = "匹配权重总和（用于排序）", example = "20")
    private Integer totalWeight;
}
