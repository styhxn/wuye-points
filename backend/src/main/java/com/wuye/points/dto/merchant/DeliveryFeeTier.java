package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "多档配送费档位")
@Data
public class DeliveryFeeTier {

    @Schema(description = "起始公里（含）", example = "0")
    @DecimalMin(value = "0", message = "minKm不能为负数")
    private BigDecimal minKm;

    @Schema(description = "结束公里（不含），空表示不限上限", example = "1")
    private BigDecimal maxKm;

    @Schema(description = "该档配送费", example = "3.00")
    @DecimalMin(value = "0", message = "fee不能为负数")
    private BigDecimal fee;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
}
