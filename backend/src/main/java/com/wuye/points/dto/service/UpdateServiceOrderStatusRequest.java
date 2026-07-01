package com.wuye.points.dto.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateServiceOrderStatusRequest {

    @NotBlank(message = "orderStatus不能为空")
    private String orderStatus;
}
