package com.wuye.points.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConfirmOrderResponse {

    private String orderId;
    private String orderStatus;
    private String completedAt;
    private Integer pointEarned;
    private BigDecimal coinEarned;
}
