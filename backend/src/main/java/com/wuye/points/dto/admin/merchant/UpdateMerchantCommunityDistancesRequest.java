package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "配置商家-小区距离请求（管理端）")
@Data
public class UpdateMerchantCommunityDistancesRequest {

    @Schema(description = "距离配置列表；distanceKm 传 null 表示清除该小区的距离配置")
    @Valid
    private List<Item> items;

    @Schema(description = "距离配置项")
    @Data
    public static class Item {

        @Schema(description = "小区ID", example = "com_xxx", requiredMode = Schema.RequiredMode.REQUIRED)
        private String communityId;

        @Schema(description = "距离（公里），>=0；null 表示清除配置", example = "1.20")
        private BigDecimal distanceKm;
    }
}
