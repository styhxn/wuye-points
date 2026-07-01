package com.wuye.points.dto.propertyfee;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayPropertyFeeResponse {

    private String propertyFeeId;
    private BigDecimal paidAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal remainingAmount;
    private String paymentStatus;
    private String paymentMethod;
    private Integer pointUsed;
    private BigDecimal coinUsed;
    private BigDecimal cashAmount;
    private String paidAt;
}
