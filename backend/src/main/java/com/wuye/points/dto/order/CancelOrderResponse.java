package com.wuye.points.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CancelOrderResponse {

    private String orderId;
    private String orderStatus;
    private String cancelledAt;
    private BigDecimal refundAmount;
    private Integer refundPoint;
    private BigDecimal refundCoin;
}
