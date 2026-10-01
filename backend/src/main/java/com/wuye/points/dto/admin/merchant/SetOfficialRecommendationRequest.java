package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "设置/取消官方推荐请求")
public class SetOfficialRecommendationRequest {

    @Schema(description = "是否官方推荐", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "isRecommended不能为空")
    private Boolean isRecommended;

    @Schema(description = "推荐排序（越大越靠前）", example = "100")
    private Integer recommendedSort;
}
