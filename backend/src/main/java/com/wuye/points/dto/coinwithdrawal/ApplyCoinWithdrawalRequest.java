package com.wuye.points.dto.coinwithdrawal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyCoinWithdrawalRequest {

    @NotNull(message = "兑换物业币数量不能为空")
    @DecimalMin(value = "0.01", message = "兑换物业币数量必须大于0")
    private BigDecimal coinAmount;
}
