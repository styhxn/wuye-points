package com.wuye.points.dto.admin.coordinator;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CoordinatorDetailResponse {
    private String id;
    private String residentId;
    private String residentName;
    private String phone;
    private String propertyCompanyId;
    private BigDecimal commissionRate;
    private Long sectorLeaderCount;
    private Long individualLeaderCount;
    private BigDecimal totalEarnings;
    private String status;
    private String appointedAt;
    private String createdAt;
    private String updatedAt;
}
