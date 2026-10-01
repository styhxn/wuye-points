package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "分成账户提现申请请求")
public class ApplySettlementWithdrawalRequest {

    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额必须大于0")
    @Schema(description = "提现金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "500.00")
    private BigDecimal amount;
}
