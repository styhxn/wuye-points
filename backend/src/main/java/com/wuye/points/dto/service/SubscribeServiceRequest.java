package com.wuye.points.dto.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscribeServiceRequest {

    @NotBlank(message = "subType不能为空")
    private String subType;

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    private Boolean autoRenew;
}
