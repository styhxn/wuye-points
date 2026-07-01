package com.wuye.points.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDeliveryStatusRequest {

    @NotBlank(message = "status不能为空")
    private String status;
}
