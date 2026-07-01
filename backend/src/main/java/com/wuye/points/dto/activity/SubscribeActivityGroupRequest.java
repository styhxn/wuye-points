package com.wuye.points.dto.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscribeActivityGroupRequest {

    @NotBlank(message = "subType不能为空")
    private String subType;

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    private Boolean autoRenew;
}
