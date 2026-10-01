package com.wuye.points.dto.admin.deliverypricerange;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "配送价格区间响应")
public class DeliveryPriceRangeResponse {

    @Schema(description = "区间ID", example = "dpr_xxx")
    private String id;

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @Schema(description = "最低价格", example = "0.00")
    private BigDecimal minPrice;

    @Schema(description = "最高价格", example = "100.00")
    private BigDecimal maxPrice;

    @Schema(description = "距离类型", example = "any", allowableValues = {"1km", "3km", "5km", "citywide", "any"})
    private String distanceType;

    @Schema(description = "距离类型中文", example = "不限距离")
    private String distanceTypeLabel;

    @Schema(description = "配送范围", example = "both", allowableValues = {"in_community", "out_community", "both"})
    private String deliveryScope;

    @Schema(description = "配送范围中文", example = "小区内外均配送")
    private String deliveryScopeLabel;

    @Schema(description = "状态 code", example = "active")
    private String statusCode;

    @Schema(description = "状态中文", example = "启用")
    private String statusLabel;

    @Schema(description = "状态（展示用）", example = "启用")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01 10:00:00")
    private String updatedAt;
}
