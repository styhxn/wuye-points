package com.wuye.points.dto.admin.coinmallrule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "更新物业币商城规则请求")
@Data
public class UpdateMallRulesRequest {

    @Schema(description = "物业币商城是否启用", example = "true", required = true)
    @NotNull(message = "coinMallEnabled不能为空")
    private Boolean coinMallEnabled;

    @Schema(description = "物业币抵扣最大比例（0~1）", example = "1.0000", required = true)
    @NotNull(message = "coinMallMaxRatio不能为空")
    @DecimalMin(value = "0.0000", message = "coinMallMaxRatio不能小于0")
    @DecimalMax(value = "1.0000", message = "coinMallMaxRatio不能大于1")
    private BigDecimal coinMallMaxRatio;

    @Schema(description = "物业币抵扣最低消费金额", example = "0.01", required = true)
    @NotNull(message = "coinMallMinAmount不能为空")
    @DecimalMin(value = "0.00", message = "coinMallMinAmount不能小于0")
    private BigDecimal coinMallMinAmount;
}
