package com.wuye.points.dto.merchant.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "商家购买积分请求")
@Data
public class PurchasePointRequest {

    @Schema(description = "购买积分数量", example = "1000", required = true)
    @NotNull(message = "购买积分数量不能为空")
    @Min(value = 1, message = "购买积分数量至少为1")
    private Integer pointAmount;

    @Schema(description = "支付金额", example = "10.00", required = true)
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal payAmount;
}
