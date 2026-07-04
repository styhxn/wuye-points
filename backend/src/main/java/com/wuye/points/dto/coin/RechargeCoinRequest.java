package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "充值物业币请求")
public class RechargeCoinRequest {

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额至少为0.01")
    @Schema(description = "充值金额", required = true, example = "100.00")
    private BigDecimal amount;
}
