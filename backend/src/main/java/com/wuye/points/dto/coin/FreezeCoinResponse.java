package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FreezeCoinResponse {

    private String id;
    private String residentId;
    private String residentName;
    private String action;
    private BigDecimal amount;
    private String reason;
    private String operatorId;
    private String operatorName;
    private String frozenAt;
}
