package com.wuye.points.dto.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SubscribeServiceResponse {

    private String id;
    private String serviceId;
    private String serviceName;
    private String residentId;
    private String subType;
    private BigDecimal amount;
    private String startDate;
    private String endDate;
    private Boolean autoRenew;
    private String status;
    private String createdAt;
}
