package com.wuye.points.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "商家ID不能为空")
    private String merchantId;

    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 200, message = "配送地址长度不能超过200")
    private String address;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String contactPhone;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    private String familyId;

    @Data
    public static class OrderItemRequest {
        @NotBlank(message = "商品ID不能为空")
        private String productId;

        @Min(value = 1, message = "数量至少为1")
        private int quantity;
    }
}
