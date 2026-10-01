package com.wuye.points.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "购物车运费预估请求")
public class CartDeliveryFeeRequest {

    @NotBlank(message = "商家ID不能为空")
    @Schema(description = "商家ID", required = true, example = "mch_demo001")
    private String merchantId;

    @NotEmpty(message = "商品列表不能为空")
    @Valid
    @Schema(description = "购物车商品列表", required = true)
    private List<Item> items;

    @Schema(description = "收货地址ID（预留，当前按商家配送费报价）", example = "addr_abc123")
    private String addressId;

    @Data
    @Schema(description = "购物车商品项")
    public static class Item {
        @NotBlank(message = "商品ID不能为空")
        @Schema(description = "商品ID", required = true, example = "prod_abc123")
        private String productId;

        @Min(value = 1, message = "商品数量至少为1")
        @Schema(description = "购买数量", required = true, example = "2")
        private int quantity;
    }
}
