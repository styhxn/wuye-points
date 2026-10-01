package com.wuye.points.dto.merchantcategory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantCategoryItemResponse {

    @Schema(description = "分类名称（中文）", example = "外卖")
    private String name;

    @Schema(description = "排序权重", example = "1")
    private Integer sortOrder;
}
