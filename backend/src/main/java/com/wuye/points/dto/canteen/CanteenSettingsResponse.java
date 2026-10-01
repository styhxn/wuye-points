package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-配置响应
 */
@Data
@Builder
@Schema(description = "社区食堂配置响应")
public class CanteenSettingsResponse {

    @Schema(description = "购买额度手续费比例（内扣）", example = "0.02")
    private BigDecimal quotaFeeRate;
}
