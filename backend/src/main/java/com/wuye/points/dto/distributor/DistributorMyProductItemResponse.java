package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "一级经销商-我的商品条目")
public class DistributorMyProductItemResponse {

    @Schema(description = "商品ID")
    private String id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "进货价（批发价）")
    private BigDecimal wholesalePrice;

    @Schema(description = "建议零售价")
    private BigDecimal suggestedRetailPrice;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "状态 active/inactive")
    private String status;

    @Schema(description = "货源类目", example = "食品饮料")
    private String category;

    @Schema(description = "服务区间 city(本市)/community(小区)")
    private String serviceScope;

    @Schema(description = "下游拿货上架数")
    private long pickupCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
