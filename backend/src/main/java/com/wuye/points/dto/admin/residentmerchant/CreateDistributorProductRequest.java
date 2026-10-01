package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "一级经销商上架商品")
public class CreateDistributorProductRequest {

    @NotBlank
    private String name;

    private String coverUrl;
    private String categoryId;
    private String category;
    private String description;

    @NotNull
    private BigDecimal wholesalePrice;

    @NotNull
    private BigDecimal suggestedRetailPrice;

    private Integer stock;

    @Schema(description = "服务区间 city(本市)/community(小区)")
    private String serviceScope;
}
