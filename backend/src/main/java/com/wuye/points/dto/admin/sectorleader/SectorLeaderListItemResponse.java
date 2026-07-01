package com.wuye.points.dto.admin.sectorleader;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SectorLeaderListItemResponse {
    private String id;
    private String residentId;
    private String residentName;
    private String phone;
    private String sector;
    private String sectorName;
    private BigDecimal commissionRate;
    private String coordinatorId;
    private Long individualLeaderCount;
    private BigDecimal totalEarnings;
    private String status;
    private String appointedAt;
}
