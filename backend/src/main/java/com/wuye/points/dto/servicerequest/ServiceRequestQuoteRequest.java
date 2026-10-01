package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商家发送服务需求费用通知")
public class ServiceRequestQuoteRequest {

    @NotNull
    @Schema(description = "报价金额")
    private BigDecimal amount;

    @Schema(description = "币种类型 property_coin/point")
    private String currencyType;

    @Schema(description = "费用说明")
    private String description;

    @Schema(description = "报价有效小时数")
    private Integer validHours;
}
