package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RechargeCoinResponse {

    private String recordId;
    private String residentId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private BigDecimal familyBalanceAfter;
    private String rechargedAt;
}
