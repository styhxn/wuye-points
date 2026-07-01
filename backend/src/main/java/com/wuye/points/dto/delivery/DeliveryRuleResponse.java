package com.wuye.points.dto.delivery;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeliveryRuleResponse {

    private String id;
    private String name;
    private BigDecimal baseFee;
    private BigDecimal perKgFee;
    private BigDecimal freeThreshold;
    private BigDecimal maxWeight;
    private BigDecimal maxDistance;
    private BigDecimal courierShareRate;
    private BigDecimal courierPerOrder;
    private Integer timeoutMinutes;
    private String propertyCompanyId;
    private String status;
    private String createdAt;
    private String updatedAt;
}
