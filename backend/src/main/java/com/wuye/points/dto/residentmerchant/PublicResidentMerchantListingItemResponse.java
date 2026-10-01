package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "业主商户公开橱窗商品项（对外展示，不含进货价）")
public class PublicResidentMerchantListingItemResponse {

    private String id;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "零售价")
    private BigDecimal retailPrice;

    @Schema(description = "货源类目（来自一级货源 distributor_products.category）")
    private String category;

    @Schema(description = "是否已上架（true=对外可售）")
    private Boolean enabled;

    @Schema(description = "分享次数")
    private Integer shareCount;
}
