package com.wuye.points.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "购物车运费预估响应")
public class CartDeliveryFeeResponse {

    @Schema(description = "商品金额（不含配送费，满额判断口径）", example = "95.00")
    private BigDecimal productAmount;

    @Schema(description = "整单运费（全单仅计算一次，前端禁止按商品行累加）", example = "5.00")
    private BigDecimal deliveryFee;

    @Schema(description = "免配送费阈值", example = "99.00")
    private BigDecimal freeDeliveryThreshold;

    @Schema(description = "是否已满足免配送费条件", example = "false")
    private Boolean freeDeliveryEligible;

    @Schema(description = "距免配送费还差多少金额（已满足则为 0）", example = "94.00")
    private BigDecimal freeDeliveryShortfall;

    @Schema(description = "满额减免前基础配送费（= 商家配送费）", example = "5.00")
    private BigDecimal originalDeliveryFee;

    @Schema(description = "满额减免金额", example = "5.00")
    private BigDecimal deliveryWaiverAmount;

    @Schema(description = "减免承担方 platform/merchant", example = "platform")
    private String deliverySubsidySponsor;

    @Schema(description = "平台/商家补贴金额", example = "5.00")
    private BigDecimal deliverySubsidyAmount;

    @Schema(description = "配送结算基数 = 实付 + 补贴", example = "5.00")
    private BigDecimal deliverySettlementBase;

    @Schema(description = "减免原因 threshold/campaign/manual/none", example = "none")
    private String waiverReason;

    @Schema(description = "计算版本", example = "distribution_b_v1")
    private String calculationVersion;
}
