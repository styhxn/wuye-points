package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "一级经销商上架商品")
public class CreateDistributorProductRequest {

    @Schema(description = "一级代理商家ID（管理端必传，须已开通 is_distributor 权限；代理工作台自建时忽略，取当前商家）")
    private String distributorMerchantId;

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

    @Schema(description = "供给范围：管理端可传 company/platform（默认 company）；代理工作台禁止传（只能由管理端授予）", example = "company")
    private String supplyScope;
}
