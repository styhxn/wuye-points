package com.wuye.points.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayOrderResponse {

    private String orderId;
    private String orderNo;
    private String orderStatus;
    private String paymentMethod;
    private BigDecimal paidAmount;
    private Integer pointUsed;
    private BigDecimal coinUsed;
    private BigDecimal cashAmount;
    private String paidAt;
    private Integer pointEarned;
    private BigDecimal coinEarned;
}
