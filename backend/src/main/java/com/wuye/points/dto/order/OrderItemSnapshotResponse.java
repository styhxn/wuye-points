package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "订单商品快照响应")
public class OrderItemSnapshotResponse {

    @Schema(description = "商品ID", example = "prod_xyz789")
    private String productId;

    @Schema(description = "商品名称", example = "洗衣液")
    private String productName;

    @Schema(description = "商品封面URL", example = "https://example.com/product.jpg")
    private String productCover;

    @Schema(description = "数量", example = "2")
    private Integer quantity;

    @Schema(description = "单价", example = "29.90")
    private BigDecimal unitPrice;

    @Schema(description = "总价", example = "59.80")
    private BigDecimal totalPrice;
}
