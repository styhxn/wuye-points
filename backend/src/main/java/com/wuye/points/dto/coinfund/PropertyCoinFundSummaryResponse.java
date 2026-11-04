package com.wuye.points.dto.coinfund;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业币现金资金账户汇总")
public class PropertyCoinFundSummaryResponse {

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称")
    private String propertyName;

    @Schema(description = "平台代管物业币现金余额（充值入账 − 商家结算 − 转物业）")
    private BigDecimal balance;

    @Schema(description = "累计充值入账")
    private BigDecimal totalIn;

    @Schema(description = "累计出账（商家结算 + 转物业 + 调整）")
    private BigDecimal totalOut;

    @Schema(description = "已结算给商家的物业币应收累计")
    private BigDecimal merchantSettlementAmount;

    @Schema(description = "资金缺口 = max(0, −balance)；负数表示物业币支出超过充值现金（免费发放物业币）")
    private BigDecimal fundingGapAmount;
}
