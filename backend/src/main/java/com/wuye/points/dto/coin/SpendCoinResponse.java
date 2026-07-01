package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SpendCoinResponse {

    private String recordId;
    private String residentId;
    private BigDecimal coinAmount;
    private BigDecimal balanceAfter;
    private BigDecimal familyBalanceAfter;
    private String source;
    private String operatorId;
    private String createdAt;
}
