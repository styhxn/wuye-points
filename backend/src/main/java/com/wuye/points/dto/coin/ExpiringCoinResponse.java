package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExpiringCoinResponse {

    private BigDecimal expiringSoon;
    private String expiryDate;
}
