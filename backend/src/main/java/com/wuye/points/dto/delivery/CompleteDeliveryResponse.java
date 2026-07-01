package com.wuye.points.dto.delivery;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CompleteDeliveryResponse {

    private String id;
    private String orderId;
    private String status;
    private BigDecimal courierEarning;
    private String deliveredAt;
}
