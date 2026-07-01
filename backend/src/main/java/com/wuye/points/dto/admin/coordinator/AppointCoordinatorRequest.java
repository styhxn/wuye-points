package com.wuye.points.dto.admin.coordinator;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppointCoordinatorRequest {

    @NotBlank(message = "residentId不能为空")
    private String residentId;

    private BigDecimal commissionRate;

    private String startDate;

    private String endDate;

    private String remark;

    private String status;
}
