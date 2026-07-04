package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @NotBlank(message = "商家ID不能为空")
    @Schema(description = "商家ID", required = true, example = "mer_abc123")
    private String merchantId;

    @NotEmpty(message = "商品列表不能为空")
    @Valid
    @Schema(description = "商品列表", required = true)
    private List<OrderItemRequest> items;

    @Size(max = 200, message = "配送地址长度不能超过200")
    @Schema(description = "配送地址", example = "阳光花园1号楼1单元101")
    private String address;

    @Size(max = 20, message = "联系电话长度不能超过20")
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注", example = "请尽快配送")
    private String remark;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    @Data
    @Schema(description = "订单商品项")
    public static class OrderItemRequest {
        @NotBlank(message = "商品ID不能为空")
        @Schema(description = "商品ID", required = true, example = "prod_xyz789")
        private String productId;

        @Min(value = 1, message = "数量至少为1")
        @Schema(description = "数量", example = "2")
        private int quantity;
    }
}
