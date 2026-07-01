package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FamilyCoinBalanceResponse {

    private String familyId;
    private String familyName;
    private BigDecimal coinBalance;
    private Integer memberCount;
}
