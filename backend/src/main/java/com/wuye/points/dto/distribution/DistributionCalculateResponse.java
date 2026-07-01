package com.wuye.points.dto.distribution;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DistributionCalculateResponse {

    private BigDecimal totalAmount;
    private BigDecimal commissionRate;
    private BigDecimal pointCost;
    private BigDecimal coinCost;
    private BigDecimal distributableAmount;
    private BigDecimal propertyShare;
    private BigDecimal coordinatorShare;
    private BigDecimal sectorLeaderShare;
    private BigDecimal individualLeaderShare;
    private String coordinatorId;
    private String sectorLeaderId;
    private String individualLeaderId;
}
