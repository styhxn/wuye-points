package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "订阅活动小组请求")
@Data
public class SubscribeActivityGroupRequest {

    @Schema(description = "订阅类型", example = "monthly", allowableValues = {"monthly", "yearly"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "subType不能为空")
    private String subType;

    @Schema(description = "支付方式", example = "property_coin", allowableValues = {"point", "property_coin", "wechat"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Schema(description = "是否自动续费", example = "true")
    private Boolean autoRenew;
}
