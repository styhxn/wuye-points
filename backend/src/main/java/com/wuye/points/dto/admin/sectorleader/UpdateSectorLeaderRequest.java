package com.wuye.points.dto.admin.sectorleader;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSectorLeaderRequest {
    private String residentId;
    private String sector;
    private BigDecimal commissionRate;
    private String coordinatorId;
    private String startDate;
    private String endDate;
    private String remark;
    private String status;
}
