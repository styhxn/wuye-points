package com.wuye.points.dto.merchant.withdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "申请提现请求")
@Data
public class ApplyWithdrawalRequest {

    @Schema(description = "提现金额", example = "100.00", required = true)
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额必须大于0")
    private BigDecimal amount;
}
