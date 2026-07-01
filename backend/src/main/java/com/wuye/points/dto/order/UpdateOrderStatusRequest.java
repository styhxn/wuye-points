package com.wuye.points.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotBlank(message = "订单状态不能为空")
    private String orderStatus;

    private Integer version;

    private String courierId;
}
