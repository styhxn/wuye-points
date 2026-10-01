package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "业主商户上架商品项")
public class ResidentMerchantListingItemResponse {

    private String id;
    private String distributorProductId;
    private String productName;
    private String coverUrl;
    private BigDecimal wholesalePrice;
    private BigDecimal retailPrice;
    private Boolean enabled;
    private Integer shareCount;
    private String createdAt;
}
