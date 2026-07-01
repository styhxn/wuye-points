package com.wuye.points.dto.delivery;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PendingDeliveryItemResponse {

    private String id;
    private String orderId;
    private String orderNo;
    private String merchantId;
    private String merchantName;
    private String merchantAddress;
    private String pickupAddress;
    private String deliveryAddress;
    private String contactPhone;
    private BigDecimal fee;
    private BigDecimal courierEarning;
    private String status;
    private Integer timeoutMinutes;
    private String createdAt;
}
