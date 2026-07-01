package com.wuye.points.dto.delivery;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDeliveryRuleRequest {

    @Size(max = 100, message = "name长度不能超过100")
    private String name;

    @DecimalMin(value = "0.00", message = "baseFee不能为负")
    private BigDecimal baseFee;

    private BigDecimal perKgFee;
    private BigDecimal freeThreshold;
    private BigDecimal maxWeight;
    private BigDecimal maxDistance;
    private BigDecimal courierShareRate;

    @DecimalMin(value = "0.00", message = "courierPerOrder不能为负")
    private BigDecimal courierPerOrder;

    @Min(value = 1, message = "timeoutMinutes至少为1")
    private Integer timeoutMinutes;

    private String status;
}
