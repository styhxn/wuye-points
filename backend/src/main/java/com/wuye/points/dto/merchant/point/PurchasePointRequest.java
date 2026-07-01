package com.wuye.points.dto.merchant.point;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchasePointRequest {

    @NotNull(message = "购买积分数量不能为空")
    @Min(value = 1, message = "购买积分数量至少为1")
    private Integer pointAmount;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal payAmount;
}
