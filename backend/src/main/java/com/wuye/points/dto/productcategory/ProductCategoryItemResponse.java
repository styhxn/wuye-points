package com.wuye.points.dto.productcategory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCategoryItemResponse {

    @Schema(description = "类目名称（中文）", example = "食品饮料")
    private String name;

    @Schema(description = "排序权重", example = "1")
    private Integer sortOrder;
}
