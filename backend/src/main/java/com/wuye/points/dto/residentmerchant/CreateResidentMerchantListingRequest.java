package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "上架业主商户商品")
public class CreateResidentMerchantListingRequest {

    @NotBlank
    private String distributorProductId;

    @NotNull
    private BigDecimal retailPrice;

    @Schema(description = "拿货件数（从一级代理库存一次性扣减，默认1，须≥1）")
    private Integer quantity;

    private Boolean enabled;
}
