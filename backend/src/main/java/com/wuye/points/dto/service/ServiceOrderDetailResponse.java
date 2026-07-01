package com.wuye.points.dto.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ServiceOrderDetailResponse {

    private String id;
    private String serviceId;
    private String serviceName;
    private String residentId;
    private String residentName;
    private String providerId;
    private String providerName;
    private String propertyCompanyId;
    private String orderStatus;
    private BigDecimal amount;
    private String paymentMethod;
    private String serviceTime;
    private String address;
    private String contactPhone;
    private String remark;
    private String createdAt;
    private String updatedAt;
}
