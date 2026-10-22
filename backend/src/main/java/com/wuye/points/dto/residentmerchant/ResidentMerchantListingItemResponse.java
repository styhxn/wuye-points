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

    @Schema(description = "拿货件数（从一级代理库存一次性扣减的件数）")
    private Integer tookQuantity;

    @Schema(description = "已卖出件数（订单完成结算时累加）")
    private Integer soldQuantity;

    @Schema(description = "可售件数（未锁定未售出）")
    private Integer quantityRemaining;

    @Schema(description = "是否因卖完自动下架")
    private Boolean soldOutUnlisted;

    private Integer shareCount;
    private String createdAt;
}
