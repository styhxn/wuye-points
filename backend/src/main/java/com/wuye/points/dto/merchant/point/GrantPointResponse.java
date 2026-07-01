package com.wuye.points.dto.merchant.point;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GrantPointResponse {

    private String id;
    private String merchantId;
    private String purchaseId;
    private String residentId;
    private Integer pointAmount;
    private BigDecimal exchangeRate;
    private Integer purchaseRemainingPoints;
    private Integer residentBalanceAfter;
    private String createdAt;
}
