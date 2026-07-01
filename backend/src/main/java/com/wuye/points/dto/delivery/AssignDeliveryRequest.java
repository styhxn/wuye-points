package com.wuye.points.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignDeliveryRequest {

    @NotBlank(message = "courierId不能为空")
    private String courierId;
}
