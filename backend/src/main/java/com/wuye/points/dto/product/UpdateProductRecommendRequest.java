package com.wuye.points.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "推荐/取消推荐商品请求")
public class UpdateProductRecommendRequest {

    @Schema(description = "是否推荐", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "recommended不能为空")
    private Boolean recommended;

    @Schema(description = "推荐排序（越大越靠前）", example = "10")
    private Integer recommendedSort;
}
