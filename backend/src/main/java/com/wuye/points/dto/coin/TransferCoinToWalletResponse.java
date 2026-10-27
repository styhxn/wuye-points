package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "奖励金转钱包响应")
public class TransferCoinToWalletResponse {

    @Schema(description = "奖励金流水ID", example = "coin_xxx")
    private String recordId;

    @Schema(description = "转出后个人奖励金余额", example = "40.00")
    private BigDecimal coinBalance;

    @Schema(description = "转出后钱包余额", example = "100.00")
    private BigDecimal walletBalance;
}
