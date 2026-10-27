package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "奖励金转钱包请求")
public class TransferCoinToWalletRequest {

    @NotNull(message = "奖励金数量不能为空")
    @DecimalMin(value = "0.01", message = "奖励金数量至少为0.01")
    @Schema(description = "奖励金数量（只能从个人奖励金转出）", required = true, example = "100.00")
    private BigDecimal coinAmount;
}
