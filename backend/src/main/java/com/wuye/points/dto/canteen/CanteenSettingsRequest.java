package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-后台配置请求
 */
@Data
@Schema(description = "社区食堂配置请求")
public class CanteenSettingsRequest {

    @DecimalMin(value = "0", message = "手续费比例不能小于0")
    @DecimalMax(value = "1", message = "手续费比例不能大于1")
    @Schema(description = "购买额度手续费比例（内扣）", example = "0.02")
    private BigDecimal quotaFeeRate;
}
