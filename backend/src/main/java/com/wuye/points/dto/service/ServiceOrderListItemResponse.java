package com.wuye.points.dto.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ServiceOrderListItemResponse {

    private String id;
    private String serviceId;
    private String serviceName;
    private String residentId;
    private String residentName;
    private String providerId;
    private String orderStatus;
    private BigDecimal amount;
    private String paymentMethod;
    private String serviceTime;
    private String address;
    private String contactPhone;
    private String createdAt;
}
