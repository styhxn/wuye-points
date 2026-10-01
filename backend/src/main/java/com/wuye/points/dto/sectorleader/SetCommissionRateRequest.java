package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "设置个体负责人分成比例请求")
public class SetCommissionRateRequest {

    @NotNull(message = "commissionRate不能为空")
    @Schema(description = "分成比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.05")
    private BigDecimal commissionRate;
}
