package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新业主商户配置")
public class UpdateResidentMerchantSettingsRequest {

    @Schema(description = "保证金默认金额", example = "500.00")
    private BigDecimal defaultDepositAmount;

    @Schema(description = "平台佣金比例（0~1）", example = "0.05")
    private BigDecimal platformCommissionRate;

    @Schema(description = "业主商户销售一级经销商货品的平台佣金比例（默认0.10）")
    private BigDecimal distributorProductCommissionRate;

    @Schema(description = "退款窗口天数", example = "7")
    private Integer refundWindowDays;

    @Schema(description = "业主小店加价下限（0~1，默认0.05）", example = "0.05")
    private BigDecimal markupMinRate;

    @Schema(description = "业主小店加价上限（0~1，默认0.10）", example = "0.10")
    private BigDecimal markupMaxRate;
}
