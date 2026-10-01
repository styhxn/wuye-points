package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-后台调整供货优惠比例请求
 */
@Data
@Schema(description = "调整供货优惠比例请求")
public class BindingUpdateRequest {

    @DecimalMin(value = "0", message = "供货优惠比例不能小于0")
    @DecimalMax(value = "1", message = "供货优惠比例不能大于1")
    @Schema(description = "供货优惠比例（0.20=子商家给主商家供货8折）", example = "0.20")
    private BigDecimal supplyDiscountRate;
}
