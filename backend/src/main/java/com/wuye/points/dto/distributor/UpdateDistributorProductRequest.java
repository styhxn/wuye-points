package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "一级经销商修改商品（仅填写需要修改的字段）")
public class UpdateDistributorProductRequest {

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

    @Schema(description = "状态 active/inactive")
    private String status;

    @Schema(description = "货源类目（如：食品饮料、日用百货）")
    private String category;

    @Schema(description = "服务区间 city(本市)/community(小区)")
    private String serviceScope;
}
