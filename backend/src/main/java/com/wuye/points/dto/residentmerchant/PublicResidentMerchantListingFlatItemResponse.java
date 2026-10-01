package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "业主商城扁平公开货源项（单接口商品级分页，消除 N+1）")
public class PublicResidentMerchantListingFlatItemResponse {

    @Schema(description = "上架ID（rml_xxx）")
    private String listingId;

    @Schema(description = "业主商户(住户)ID")
    private String residentId;

    @Schema(description = "店主姓名")
    private String residentName;

    @Schema(description = "货源类目", example = "食品饮料")
    private String category;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "零售价")
    private BigDecimal retailPrice;

    @Schema(description = "分享次数")
    private Integer shareCount;
}
