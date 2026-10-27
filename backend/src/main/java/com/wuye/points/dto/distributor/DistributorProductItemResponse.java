package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "一级货源商品项")
public class DistributorProductItemResponse {

    private String distributorProductId;
    private String name;
    private String coverUrl;
    private BigDecimal wholesalePrice;
    private BigDecimal suggestedRetailPrice;
    private Integer stock;

    @Schema(description = "供给范围 company=本物业公司/platform=全平台", example = "company")
    private String supplyScope;
}
