package com.wuye.points.dto.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下单可用券（试算时按订单商品金额计算可抵扣金额）。
 */
@Data
@Builder
@Schema(description = "下单可用优惠券响应")
public class AvailableCouponResponse {

    @Schema(description = "用户券ID", example = "ucp_1a2b3c")
    private String id;

    @Schema(description = "券模板ID")
    private String templateId;

    @Schema(description = "券名称", example = "满100减20")
    private String name;

    @Schema(description = "券类型 full_reduction/discount/instant_reduction")
    private String type;

    @Schema(description = "适用范围 platform/merchant")
    private String scope;

    @Schema(description = "限定商家ID（平台券为空）")
    private String merchantId;

    @Schema(description = "面额")
    private BigDecimal valueAmount;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "折扣封顶金额")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "使用门槛")
    private BigDecimal minSpendAmount;

    @Schema(description = "过期时间")
    private String expireAt;

    @Schema(description = "本单可抵扣金额", example = "20.00")
    private BigDecimal discountAmount;

    @Schema(description = "是否满足使用门槛", example = "true")
    private Boolean usable;
}