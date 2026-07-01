package com.wuye.points.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateOrderResponse {

    private String orderId;
    private String orderNo;
    private String merchantId;
    private String merchantName;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String paymentMethod;
    private Integer pointUsed;
    private BigDecimal coinUsed;
    private BigDecimal cashAmount;
    private String orderStatus;
    private String createdAt;
}
