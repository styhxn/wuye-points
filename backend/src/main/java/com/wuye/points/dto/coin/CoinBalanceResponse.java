package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CoinBalanceResponse {

    private BigDecimal coinBalance;
    private BigDecimal familyCoinBalance;
    private String familyId;
    private Boolean coinFrozen;
    private Boolean coinHidden;
    private Boolean coinDisplayEnabled;
    private BigDecimal todayEarned;
    private BigDecimal todaySpent;
    private BigDecimal expiringSoon;
    private String expiryDate;
}
