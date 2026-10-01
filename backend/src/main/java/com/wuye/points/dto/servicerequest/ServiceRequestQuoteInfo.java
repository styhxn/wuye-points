package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "服务需求费用通知")
public class ServiceRequestQuoteInfo {

    private BigDecimal amount;
    private String currencyType;
    private String description;
    private Integer validHours;
}
