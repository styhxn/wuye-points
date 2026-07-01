package com.wuye.points.dto.delivery;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDeliveryStatusResponse {

    private String id;
    private String orderId;
    private String status;
    private String pickedAt;
    private String updatedAt;
}
