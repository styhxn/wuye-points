package com.wuye.points.dto.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "用户优惠券响应")
public class UserCouponResponse {

    @Schema(description = "用户券ID", example = "ucp_1a2b3c")
    private String id;

    @Schema(description = "券模板ID")
    private String templateId;

    @Schema(description = "券名称", example = "满100减20")
    private String name;

    @Schema(description = "券类型 full_reduction/discount/instant_reduction", example = "full_reduction")
    private String type;

    @Schema(description = "适用范围 platform/merchant", example = "platform")
    private String scope;

    @Schema(description = "限定商家ID（平台券为空）")
    private String merchantId;

    @Schema(description = "限定商家名称（平台券为空）")
    private String merchantName;

    @Schema(description = "面额")
    private BigDecimal valueAmount;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "折扣封顶金额")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "使用门槛")
    private BigDecimal minSpendAmount;

    @Schema(description = "状态 unused/locked/used/expired", example = "unused")
    private String status;

    @Schema(description = "来源 claim/grant", example = "claim")
    private String source;

    @Schema(description = "过期时间")
    private String expireAt;

    @Schema(description = "领取时间")
    private String claimedAt;

    @Schema(description = "核销时间")
    private String usedAt;

    @Schema(description = "核销订单ID")
    private String orderId;

    @Schema(description = "是否已过期（按当前时间计算）")
    private Boolean expired;
}