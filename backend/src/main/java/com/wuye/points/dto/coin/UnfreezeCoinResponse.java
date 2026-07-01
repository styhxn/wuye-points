package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UnfreezeCoinResponse {

    private String residentId;
    private BigDecimal unfrozenAmount;
    private BigDecimal newBalance;
    private String unfrozenAt;
    private String operatorId;
}
