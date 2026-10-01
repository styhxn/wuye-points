package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "订单支付试算请求")
public class PreviewOrderRequest {

    @NotBlank(message = "商家ID不能为空")
    @Schema(description = "商家ID", required = true, example = "mch_demo001")
    private String merchantId;

    @NotEmpty(message = "商品列表不能为空")
    @Valid
    @Schema(description = "商品列表", required = true)
    private List<CreateOrderRequest.OrderItemRequest> items;

    @DecimalMin(value = "0", message = "使用物业币不能为负数")
    @Schema(description = "期望抵扣的物业币金额", example = "20.00")
    private BigDecimal coinUsed;

    @Min(value = 0, message = "使用积分不能为负数")
    @Schema(description = "期望使用的积分", example = "100")
    private Integer pointUsed;

    @Schema(description = "家庭ID（使用家庭物业币池时传入）", example = "fam_abc123")
    private String familyId;
}
