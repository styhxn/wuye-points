package com.wuye.points.dto.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "住户钱包余额")
public class ResidentWalletResponse {

    @Schema(description = "个人奖励金（物业币）余额", example = "40.00")
    private BigDecimal coinBalance;

    @Schema(description = "钱包累计余额（未扣在途提现）", example = "100.00")
    private BigDecimal walletBalance;

    @Schema(description = "在途提现占用（pending/approved）", example = "40.00")
    private BigDecimal occupiedWithdrawalAmount;

    @Schema(description = "可提现余额 = 钱包累计余额 − 在途提现占用", example = "60.00")
    private BigDecimal withdrawableAmount;

    @Schema(description = "提现是否被冻结", example = "false")
    private Boolean withdrawalBlocked;

    @Schema(description = "提现手续费率（提交前即可算手续费；物业公司覆盖值优先，未配置取平台默认，"
            + "平台/角色手续费开关关闭时为 0）", example = "0.0060")
    private BigDecimal feeRate;

    @Schema(description = "手续费承担方：payee=从提现金额内扣（实到 = amount − amount × feeRate）",
            example = "payee")
    private String withdrawFeeBearer;
}
