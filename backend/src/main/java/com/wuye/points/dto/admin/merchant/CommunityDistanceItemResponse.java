package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "商家-小区距离配置项（管理端）")
@Data
@Builder
public class CommunityDistanceItemResponse {

    @Schema(description = "小区ID", example = "com_xxx")
    private String communityId;

    @Schema(description = "小区名称", example = "阳光花园")
    private String communityName;

    @Schema(description = "商家距该小区的公里数；未配置时为 null（不展示距离）", example = "1.20")
    private BigDecimal distanceKm;
}
