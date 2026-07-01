package com.wuye.points.dto.admin.pointpool;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PointPoolBalanceResponse {

    private String propertyCompanyId;
    private String propertyCompanyName;
    private BigDecimal balance;
    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal equivalentAmount;
    private String updatedAt;
}
