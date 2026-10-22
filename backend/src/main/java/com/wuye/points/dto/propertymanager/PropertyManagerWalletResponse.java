package com.wuye.points.dto.propertymanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业经理钱包概览")
public class PropertyManagerWalletResponse {

    @Schema(description = "累计抽成收益（含退款冲正）")
    private BigDecimal totalEarning;

    @Schema(description = "已提现金额")
    private BigDecimal withdrawnAmount;

    @Schema(description = "在途提现金额（待审+已审待打款）")
    private BigDecimal pendingAmount;

    @Schema(description = "可提现余额")
    private BigDecimal withdrawableAmount;
}
