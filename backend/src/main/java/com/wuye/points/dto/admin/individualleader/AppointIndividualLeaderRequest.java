package com.wuye.points.dto.admin.individualleader;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppointIndividualLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    private String residentId;

    @NotBlank(message = "sectorLeaderId不能为空")
    private String sectorLeaderId;

    @NotBlank(message = "sector不能为空")
    private String sector;

    private String name;

    private BigDecimal commissionRate;

    private String startDate;

    private String endDate;

    private String remark;

    private String status;
}
