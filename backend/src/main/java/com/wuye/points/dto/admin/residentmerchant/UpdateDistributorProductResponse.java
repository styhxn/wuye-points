package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分销商品更新响应（2026-09-07 前端问题 H）。
 */
@Data
@Builder
@Schema(description = "分销商品更新响应")
public class UpdateDistributorProductResponse {

    @Schema(description = "分销商品ID", example = "dpr_001")
    private String id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "批发价（元）")
    private BigDecimal wholesalePrice;

    @Schema(description = "建议零售价（元）")
    private BigDecimal suggestedRetailPrice;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "状态 active/inactive")
    private String status;
}
