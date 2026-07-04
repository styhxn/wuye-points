package com.wuye.points.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "商品详情响应")
public class ProductDetailResponse {

    @Schema(description = "商品ID", example = "p_001")
    private String id;

    @Schema(description = "商家ID", example = "m_001")
    private String merchantId;

    @Schema(description = "商家名称", example = "绿城物业")
    private String merchantName;

    @Schema(description = "商品名称", example = "精美绿植盆栽")
    private String name;

    @Schema(description = "商品描述", example = "适合阳台摆放的绿植盆栽，净化空气")
    private String description;

    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "商品价格", example = "29.90")
    private BigDecimal price;

    @Schema(description = "业主优惠价", example = "25.00")
    private BigDecimal memberPrice;

    @Schema(description = "积分兑换价", example = "500")
    private Integer pointPrice;

    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    @Schema(description = "商品分类", example = "绿植")
    private String category;

    @Schema(description = "商品状态", example = "on_sale")
    private String status;

    @Schema(description = "创建时间", example = "2025-01-01 10:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2025-01-15 10:30:00")
    private String updatedAt;
}
