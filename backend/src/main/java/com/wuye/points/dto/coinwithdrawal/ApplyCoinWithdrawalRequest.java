package com.wuye.points.dto.coinwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "申请物业币提现请求")
public class ApplyCoinWithdrawalRequest {

    @NotNull(message = "兑换物业币数量不能为空")
    @DecimalMin(value = "0.01", message = "兑换物业币数量必须大于0")
    @Schema(description = "兑换物业币数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal coinAmount;
}
