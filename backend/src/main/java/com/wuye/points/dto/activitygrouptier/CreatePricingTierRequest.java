package com.wuye.points.dto.activitygrouptier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "创建活动组课时价格级别请求")
@Data
public class CreatePricingTierRequest {

    @Schema(description = "级别名称，如一级、二级", example = "一级", required = true)
    @NotBlank(message = "tierName不能为空")
    private String tierName;

    @Schema(description = "级别编码，如 level_1", example = "level_1", required = true)
    @NotBlank(message = "tierCode不能为空")
    private String tierCode;

    @Schema(description = "价格", example = "100.00", required = true)
    @NotNull(message = "price不能为空")
    @DecimalMin(value = "0.00", message = "price不能为负数")
    private BigDecimal price;

    @Schema(description = "周期：monthly/quarterly/yearly", example = "monthly", required = true)
    @NotBlank(message = "period不能为空")
    private String period;

    @Schema(description = "排序，越大越靠前", example = "0")
    private Integer sortOrder;
}
