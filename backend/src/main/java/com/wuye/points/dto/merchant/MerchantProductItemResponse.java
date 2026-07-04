package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "商家商品项响应")
public class MerchantProductItemResponse {

    @Schema(description = "商品ID", example = "product_001")
    private String id;

    @Schema(description = "商品名称", example = "矿泉水")
    private String name;

    @Schema(description = "商品描述", example = "550ml矿泉水")
    private String description;

    @Schema(description = "封面图片URL", example = "https://example.com/product.jpg")
    private String coverUrl;

    @Schema(description = "商品价格", example = "2.00")
    private BigDecimal price;

    @Schema(description = "会员价格", example = "1.80")
    private BigDecimal memberPrice;

    @Schema(description = "积分价格", example = "200")
    private Integer pointPrice;

    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    @Schema(description = "商品类别", example = "饮料")
    private String category;

    @Schema(description = "商品状态", example = "active")
    private String status;
}
