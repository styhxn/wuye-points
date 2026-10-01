package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "一级经销商调整库存（正数补货，负数扣减）")
public class AdjustDistributorProductStockRequest {

    @Schema(description = "库存变动数量，正数补货、负数扣减", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "库存变动数量不能为空")
    private Integer delta;
}
