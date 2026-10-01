package com.wuye.points.dto.admin.deliverypricerange;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建配送价格区间请求")
public class CreateDeliveryPriceRangeRequest {

    @NotNull(message = "minPrice不能为空")
    @DecimalMin(value = "0.00", message = "minPrice不能为负")
    @Schema(description = "最低价格", example = "0.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal minPrice;

    @NotNull(message = "maxPrice不能为空")
    @DecimalMin(value = "0.00", message = "maxPrice不能为负")
    @Schema(description = "最高价格", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal maxPrice;

    @Schema(description = "距离类型", example = "any", allowableValues = {"1km", "3km", "5km", "citywide", "any"})
    private String distanceType;

    @Schema(description = "配送范围", example = "both", allowableValues = {"in_community", "out_community", "both"})
    private String deliveryScope;

    @Schema(description = "物业公司ID（platform_admin 可指定，其他角色自动取上下文）", example = "pc_xxx")
    private String propertyCompanyId;
}
