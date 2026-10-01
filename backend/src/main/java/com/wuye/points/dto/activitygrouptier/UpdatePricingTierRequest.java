package com.wuye.points.dto.activitygrouptier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新活动组课时价格级别请求")
public class UpdatePricingTierRequest {

    @Schema(description = "级别名称，如一级、二级", example = "一级")
    private String tierName;

    @Schema(description = "级别编码，如 level_1", example = "level_1")
    private String tierCode;

    @Schema(description = "价格", example = "100.00")
    @DecimalMin(value = "0.00", message = "price不能为负数")
    private BigDecimal price;

    @Schema(description = "周期：monthly/quarterly/yearly", example = "monthly")
    private String period;

    @Schema(description = "排序，越大越靠前", example = "0")
    private Integer sortOrder;

    @Schema(description = "状态：active/inactive", example = "active")
    private String status;
}
