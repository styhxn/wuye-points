package com.wuye.points.dto.point;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeductFeeResponse {

    private String propertyFeeId;
    private Integer pointsUsed;
    private BigDecimal deductedAmount;
    private BigDecimal remainingAmount;
    private String paymentStatus;
    private Integer residentBalance;
    private Integer familyBalance;
    private String deductedAt;
}
