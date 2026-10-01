package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分销商品更新请求（全部字段可选，仅更新传入字段）。
 * 2026-09-07 前端问题 H：管理端此前无法修改分销商品内容。
 */
@Data
@Schema(description = "分销商品更新请求（全部字段可选，仅更新传入字段）")
public class UpdateDistributorProductRequest {

    @Schema(description = "商品名称", example = "有机大米 5kg")
    private String name;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "类目/类目ID")
    private String category;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "批发价（元）", example = "35.00")
    private BigDecimal wholesalePrice;

    @Schema(description = "建议零售价（元）", example = "49.90")
    private BigDecimal suggestedRetailPrice;

    @Schema(description = "库存（正数补货 / 负数扣减到该值；传原值即不变）", example = "100")
    private Integer stock;

    @Schema(description = "服务区间 city(本市)/community(小区)")
    private String serviceScope;

    @Schema(description = "是否下架（status=inactive），传 false 恢复上架")
    private Boolean inactive;
}
