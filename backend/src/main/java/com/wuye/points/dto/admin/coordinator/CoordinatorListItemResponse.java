package com.wuye.points.dto.admin.coordinator;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CoordinatorListItemResponse {
    private String id;
    private String residentId;
    private String name;
    private String phone;
    private BigDecimal commissionRate;
    private Long sectorLeaderCount;
    private Long individualLeaderCount;
    private BigDecimal totalEarnings;
    private String status;
    private String appointedAt;
}
