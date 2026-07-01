package com.wuye.points.dto.admin.individualleader;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class IndividualLeaderListItemResponse {
    private String id;
    private String residentId;
    private String residentName;
    private String phone;
    private String name;
    private String sectorLeaderId;
    private String sector;
    private String sectorName;
    private BigDecimal commissionRate;
    private BigDecimal totalEarnings;
    private String status;
    private String appointedAt;
}
