package com.wuye.points.dto.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateOrderStatusResponse {

    private String orderId;
    private String orderStatus;
    private String courierId;
    private String deliveredAt;
    private Integer version;
    private String updatedAt;
}
