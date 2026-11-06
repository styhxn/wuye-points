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

    @Schema(description = "可提现余额（累计抽成流水 − 已占用提现 − 微信分账已实时到账，"
            + "与 POST /property-managers/my/withdrawals 校验同源）")
    private BigDecimal withdrawableAmount;

    @Schema(description = "提现手续费率（物业公司覆盖值优先，未配置取平台默认，手续费开关关闭时为 0）",
            example = "0.0060")
    private BigDecimal feeRate;

    @Schema(description = "手续费承担方：payee=从提现金额内扣（实到 = amount − amount × feeRate）",
            example = "payee")
    private String withdrawFeeBearer;
}
