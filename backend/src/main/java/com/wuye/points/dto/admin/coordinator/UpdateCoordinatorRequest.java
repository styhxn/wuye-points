package com.wuye.points.dto.admin.coordinator;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCoordinatorRequest {
    private String residentId;
    private BigDecimal commissionRate;
    private String startDate;
    private String endDate;
    private String remark;
    private String status;
}
