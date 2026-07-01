package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CoinRecordItemResponse {

    private String id;
    private String residentId;
    private String familyId;
    private String coinType;
    private BigDecimal amount;
    private BigDecimal balance;
    private String source;
    private String sourceId;
    private String description;
    private String operatorId;
    private String createdAt;
}
