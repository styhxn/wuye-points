package com.wuye.points.dto.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "优惠券模板响应")
public class CouponTemplateResponse {

    @Schema(description = "模板ID", example = "cpt_1a2b3c")
    private String id;

    @Schema(description = "券名称", example = "满100减20")
    private String name;

    @Schema(description = "使用说明")
    private String description;

    @Schema(description = "券类型 full_reduction/discount/instant_reduction", example = "full_reduction")
    private String type;

    @Schema(description = "适用范围 platform/merchant", example = "platform")
    private String scope;

    @Schema(description = "商家ID")
    private String merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "面额")
    private BigDecimal valueAmount;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "折扣封顶金额")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "使用门槛")
    private BigDecimal minSpendAmount;

    @Schema(description = "有效期类型 fixed/relative")
    private String validType;

    @Schema(description = "有效期开始")
    private String validStartAt;

    @Schema(description = "有效期结束")
    private String validEndAt;

    @Schema(description = "领取后有效天数")
    private Integer validDays;

    @Schema(description = "发放总量，null=不限量")
    private Integer totalQuantity;

    @Schema(description = "已领取数量")
    private Integer claimedQuantity;

    @Schema(description = "剩余数量，null=不限量")
    private Integer remainingQuantity;

    @Schema(description = "每人限领")
    private Integer perUserLimit;

    @Schema(description = "状态 active/inactive")
    private String status;

    @Schema(description = "当前用户是否已领取满限（领券中心列表返回）")
    private Boolean claimedByMe;

    @Schema(description = "创建时间")
    private String createdAt;
}