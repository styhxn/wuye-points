package com.wuye.points.dto.admin.individualleader;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateIndividualLeaderRequest {
    private String residentId;
    private String sectorLeaderId;
    private String sector;
    private String name;
    private BigDecimal commissionRate;
    private String startDate;
    private String endDate;
    private String remark;
    private String status;
}
