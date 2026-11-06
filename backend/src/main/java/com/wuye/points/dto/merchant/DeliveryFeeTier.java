package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "配送费档位：商家自由配置，未命中任何档位时回落到 merchants.delivery_fee（默认值）")
@Data
public class DeliveryFeeTier {

    @Schema(description = "区间维度：amount=按订单商品金额（默认自由配置用），km=按商家到小区的距离。"
            + "留空时按旧数据推断——填了 minKm/maxKm 视为 km，否则视为 amount",
            example = "amount", allowableValues = {"amount", "km"})
    private String metric;

    @Schema(description = "区间下界（含）。metric=amount 时单位元，metric=km 时单位公里", example = "0")
    @DecimalMin(value = "0", message = "min不能为负数")
    private BigDecimal min;

    @Schema(description = "区间上界（不含），空表示不限上限", example = "20")
    private BigDecimal max;

    @Schema(description = "起始公里（含），metric=km 的历史字段，与新字段 min 等价", example = "0")
    @DecimalMin(value = "0", message = "minKm不能为负数")
    private BigDecimal minKm;

    @Schema(description = "结束公里（不含），空表示不限上限；metric=km 的历史字段", example = "1")
    private BigDecimal maxKm;

    @Schema(description = "该档配送费", example = "3.00")
    @DecimalMin(value = "0", message = "fee不能为负数")
    private BigDecimal fee;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /** 区间维度归一：未显式声明时按历史字段推断，保证现网 km 档位不改数据即可继续用。 */
    public String resolvedMetric() {
        if (metric != null && !metric.isBlank()) {
            return metric.trim();
        }
        return (minKm != null || maxKm != null) ? "km" : "amount";
    }

    /** 下界：新字段优先，退回历史 km 字段。 */
    public BigDecimal resolvedMin() {
        if (min != null) {
            return min;
        }
        return minKm != null ? minKm : BigDecimal.ZERO;
    }

    /** 上界（不含）：新字段优先，退回历史 km 字段；null 表示不限。 */
    public BigDecimal resolvedMax() {
        return min != null || max != null ? max : maxKm;
    }

    public boolean isEnabled() {
        return !Boolean.FALSE.equals(enabled);
    }
}
