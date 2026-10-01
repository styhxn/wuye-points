package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "支付服务需求报价")
public class PayServiceRequestQuoteRequest {

    @NotBlank
    @Schema(description = "支付方式 point/property_coin/mixed")
    private String paymentMethod;

    @Schema(description = "混合支付积分")
    private Integer pointUsed;

    @Schema(description = "混合支付物业币")
    private BigDecimal coinUsed;
}
