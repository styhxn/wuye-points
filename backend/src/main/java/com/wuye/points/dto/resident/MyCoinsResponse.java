package com.wuye.points.dto.resident;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MyCoinsResponse {

    private String residentId;
    private BigDecimal coinBalance;
    private Boolean coinFrozen;
    private Boolean coinHidden;
    private Boolean coinDisplayEnabled;
    private String familyId;
    private BigDecimal familyCoinBalance;
    private BigDecimal earnedThisMonth;
    private BigDecimal spentThisMonth;
    private BigDecimal expiringSoon;
    private String expiryDate;
}
