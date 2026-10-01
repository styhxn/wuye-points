package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "个体负责人设置满额配送费请求")
public class SetFreeDeliveryThresholdRequest {

    @NotBlank(message = "merchantId不能为空")
    @Schema(description = "商家ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "mch_xxx")
    private String merchantId;

    @NotNull(message = "freeDeliveryThreshold不能为空")
    @Schema(description = "满额免配送费阈值", requiredMode = Schema.RequiredMode.REQUIRED, example = "30.00")
    private BigDecimal freeDeliveryThreshold;
}
