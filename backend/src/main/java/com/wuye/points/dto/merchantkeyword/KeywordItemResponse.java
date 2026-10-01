package com.wuye.points.dto.merchantkeyword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "商家关键词项响应")
public class KeywordItemResponse {

    @Schema(description = "关键词ID", example = "mk_abc12345")
    private String id;

    @Schema(description = "商家ID", example = "mch_abc12345")
    private String merchantId;

    @Schema(description = "关键词", example = "水果")
    private String keyword;

    @Schema(description = "权重", example = "10")
    private Integer weight;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T00:00:00")
    private String updatedAt;
}
