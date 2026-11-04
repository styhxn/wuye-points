package com.wuye.points.dto.merchant.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "商家物业币应收-订单明细")
public class MerchantCoinReceivableItemResponse {

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单完成时间")
    private String completedAt;

    @Schema(description = "订单总额（含配送费）")
    private BigDecimal totalAmount;

    @Schema(description = "优惠券抵扣")
    private BigDecimal couponAmount;

    @Schema(description = "物业币支付金额")
    private BigDecimal coinUsed;

    @Schema(description = "现金支付金额")
    private BigDecimal cashAmount;

    @Schema(description = "支付通道：direct/partner/ecommerce；无现金支付时为 null")
    private String payMode;

    @Schema(description = "抽佣比例")
    private BigDecimal commissionRate;

    @Schema(description = "抽佣金额")
    private BigDecimal commissionAmount;

    @Schema(description = "商家应得（商品金额 − 抽佣 − 商家承担的配送补贴）")
    private BigDecimal merchantIncome;

    @Schema(description = "本单物业币应收（商家应得中由物业币支付的部分）")
    private BigDecimal coinReceivableAmount;

    @Schema(description = "本单已结算金额（按提现单 FIFO 归集）")
    private BigDecimal settledAmount;

    @Schema(description = "本单在途金额（pending/approved 提现占用）")
    private BigDecimal pendingAmount;

    @Schema(description = "本单可结算余额")
    private BigDecimal availableAmount;

    @Schema(description = "结算状态：available/partial/pending/settled")
    private String settlementStatus;
}
