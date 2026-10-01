package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "发起图文咨询订单")
public class CreateConsultationChatOrderRequest {

    @NotBlank
    @Schema(description = "支付方式 point/property_coin/mixed")
    private String paymentMethod;

    private Integer pointUsed;
    private BigDecimal coinUsed;
}
