package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-商家额度余额响应
 */
@Data
@Builder
@Schema(description = "商家额度余额响应")
public class MerchantQuotaBalanceResponse {

    @Schema(description = "主商家ID", example = "mch_xxx")
    private String merchantId;

    @Schema(description = "可用额度（= Σ已到账剩余额度）", example = "980.00")
    private BigDecimal availableQuota;

    @Schema(description = "累计购买到账额度", example = "9800.00")
    private BigDecimal totalPurchasedQuota;

    @Schema(description = "累计已充值额度", example = "8820.00")
    private BigDecimal totalRecharged;
}
