package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "订阅服务请求")
public class SubscribeServiceRequest {

    @Schema(description = "订阅类型", example = "monthly", required = true, allowableValues = {"monthly", "quarterly", "yearly"})
    @NotBlank(message = "subType不能为空")
    private String subType;

    @Schema(description = "支付方式", example = "微信支付", required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Schema(description = "是否自动续订", example = "true")
    private Boolean autoRenew;
}
