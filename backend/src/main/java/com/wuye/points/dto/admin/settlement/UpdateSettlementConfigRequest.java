package com.wuye.points.dto.admin.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新结算/分账模式配置请求（所有字段均可选，仅更新非 null 项）")
public class UpdateSettlementConfigRequest {

    @Schema(description = "真分账(微信)模式开关")
    private Boolean wechatSplitEnabled;

    @Schema(description = "记账兜底模式开关（平台持币代付）")
    private Boolean ledgerFallbackEnabled;

    @Schema(description = "自动提现开关")
    private Boolean autoWithdrawalEnabled;

    @Schema(description = "平台提现手续费总开关")
    private Boolean platformWithdrawalFeeEnabled;

    @Schema(description = "平台提现手续费率（0~1，例如 0.0060 表示千分之六）")
    private BigDecimal platformWithdrawalFeeRate;

    @Schema(description = "平台提现手续费分成比例（0~1，默认 1.0000 表示平台独占）")
    private BigDecimal platformWithdrawalFeeShareRate;

    @Schema(description = "商家提现是否收取平台手续费")
    private Boolean feeForMerchant;

    @Schema(description = "角色(自然人)提现是否收取平台手续费")
    private Boolean feeForRole;

    @Schema(description = "物业公司提现是否收取平台手续费")
    private Boolean feeForProperty;

    @Schema(description = "居民(积分)提现是否收取平台手续费")
    private Boolean feeForCoin;
}
