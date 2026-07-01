package com.wuye.points.dto.distribution;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DistributionRecordItemResponse {

    private String id;
    private String orderId;
    private String orderNo;
    private String merchantId;
    private String merchantName;
    private String residentId;
    private String residentName;
    private String propertyCompanyId;
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
    private String coordinatorName;
    private String sectorLeaderId;
    private String sectorLeaderName;
    private String individualLeaderId;
    private String individualLeaderName;
    private String createdAt;
}
