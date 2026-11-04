package com.wuye.points.dto.merchant.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "商家物业币应收")
public class MerchantCoinReceivableResponse {

    @Schema(description = "物业币应收累计（已完成订单）")
    private BigDecimal grossAmount;

    @Schema(description = "已结算金额")
    private BigDecimal settledAmount;

    @Schema(description = "在途金额（pending/approved）")
    private BigDecimal pendingAmount;

    @Schema(description = "可结算余额 = gross − settled − pending")
    private BigDecimal availableAmount;

    @Schema(description = "含物业币支付的订单数")
    private long orderCount;

    @Schema(description = "已结算订单数")
    private long settledOrderCount;

    @Schema(description = "订单明细")
    private List<MerchantCoinReceivableItemResponse> items;

    private int page;

    private int pageSize;

    private long totalElements;
}
