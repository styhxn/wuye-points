package com.wuye.points.dto.merchantkeyword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "商家关键词列表响应")
public class KeywordListResponse {

    @Schema(description = "商家ID", example = "mch_abc12345")
    private String merchantId;

    @Schema(description = "商家名称", example = "幸福超市")
    private String merchantName;

    @Schema(description = "关键词列表")
    private List<KeywordItemResponse> keywords;
}
