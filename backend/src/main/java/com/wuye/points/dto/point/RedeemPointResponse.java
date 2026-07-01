package com.wuye.points.dto.point;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RedeemPointResponse {

    private Integer pointsUsed;
    private BigDecimal redeemedAmount;
    private Integer balanceAfter;
    private Integer familyBalanceAfter;
    private String redeemedAt;
}
