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

    @Schema(description = "支付方式（property_coin/point/wechat/mixed）", example = "wechat", required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Schema(description = "使用积分（mixed时可选）", example = "5000")
    private Integer pointUsed;

    @Schema(description = "使用物业币（mixed时可选）", example = "30")
    private java.math.BigDecimal coinUsed;

    @Schema(description = "是否自动续订", example = "true")
    private Boolean autoRenew;
}
