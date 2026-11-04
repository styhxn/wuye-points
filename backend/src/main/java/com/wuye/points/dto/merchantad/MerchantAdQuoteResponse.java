package com.wuye.points.dto.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "广告位按天计价试算响应")
public class MerchantAdQuoteResponse {

    @Schema(description = "展示天数")
    private Integer durationDays;

    @Schema(description = "单日价格（元）")
    private BigDecimal dayPrice;

    @Schema(description = "应付金额（元）= 单日价 × 天数")
    private BigDecimal payAmount;

    @Schema(description = "本周免费额度剩余条数（可免费投放）")
    private Integer freeQuotaRemaining;

    @Schema(description = "免费额度是否启用")
    private Boolean freeQuotaEnabled;

    @Schema(description = "广告微信支付是否可用（false 时前端只展示免费额度）")
    private Boolean wechatPaymentEnabled;
}
