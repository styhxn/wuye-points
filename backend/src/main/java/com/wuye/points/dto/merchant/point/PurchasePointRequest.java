package com.wuye.points.dto.merchant.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "商家购买积分请求")
@Data
public class PurchasePointRequest {

    @Schema(description = "购买积分数量", example = "1000", required = true)
    @NotNull(message = "购买积分数量不能为空")
    @Min(value = 1, message = "购买积分数量至少为1")
    private Integer pointAmount;

    /**
     * 2026-11-05 整改条目17：应付金额由后端按商家积分兑换比折算，前端不再提交。
     * 字段保留仅为兼容旧包；提交值一律忽略，避免金额被篡改。
     */
    @Schema(description = "支付金额（已废弃，后端按兑换比自动计算，传值忽略）", example = "10.00", deprecated = true)
    private BigDecimal payAmount;
}
