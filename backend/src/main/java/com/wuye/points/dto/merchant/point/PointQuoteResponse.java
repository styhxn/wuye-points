package com.wuye.points.dto.merchant.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "积分报价响应")
@Data
@Builder
public class PointQuoteResponse {

    @Schema(description = "可购积分数量", example = "9500")
    private Long pointAmount;

    @Schema(description = "支付金额（元）", example = "100.00")
    private BigDecimal payAmount;
}
