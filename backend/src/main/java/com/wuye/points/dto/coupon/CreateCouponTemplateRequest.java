package com.wuye.points.dto.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建优惠券模板请求")
public class CreateCouponTemplateRequest {

    @NotBlank(message = "券名称不能为空")
    @Size(max = 100, message = "券名称长度不能超过100")
    @Schema(description = "券名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "满100减20")
    private String name;

    @Size(max = 500, message = "描述长度不能超过500")
    @Schema(description = "使用说明")
    private String description;

    @NotBlank(message = "券类型不能为空")
    @Pattern(regexp = "full_reduction|discount|instant_reduction",
            message = "券类型只能为full_reduction(满减)、discount(折扣)、instant_reduction(立减)")
    @Schema(description = "券类型", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"full_reduction", "discount", "instant_reduction"})
    private String type;

    @NotBlank(message = "适用范围不能为空")
    @Pattern(regexp = "platform|merchant", message = "适用范围只能为platform(平台券)或merchant(商家券)")
    @Schema(description = "适用范围", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"platform", "merchant"})
    private String scope;

    @Size(max = 32, message = "merchantId格式不正确")
    @Schema(description = "商家ID（scope=merchant 时必填）", example = "mch_demo002")
    private String merchantId;

    @DecimalMin(value = "0.01", message = "面额必须大于0")
    @Schema(description = "面额（type=full_reduction/instant_reduction 必填）", example = "20.00")
    private BigDecimal valueAmount;

    @DecimalMin(value = "0.01", message = "折扣率必须大于0")
    @Schema(description = "折扣率（type=discount 必填），8.50 表示 8.5 折", example = "8.50")
    private BigDecimal discountRate;

    @DecimalMin(value = "0.01", message = "折扣封顶必须大于0")
    @Schema(description = "折扣券封顶金额（type=discount 可选）", example = "50.00")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0", message = "使用门槛不能为负数")
    @Schema(description = "使用门槛（订单商品金额需≥该值），默认0", example = "100.00")
    private BigDecimal minSpendAmount;

    @NotBlank(message = "有效期类型不能为空")
    @Pattern(regexp = "fixed|relative", message = "有效期类型只能为fixed(固定起止)或relative(领取后N天)")
    @Schema(description = "有效期类型", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"fixed", "relative"})
    private String validType;

    @Schema(description = "有效期开始（validType=fixed 必填）", example = "2026-10-01 00:00:00")
    private String validStartAt;

    @Schema(description = "有效期结束（validType=fixed 必填）", example = "2026-12-31 23:59:59")
    private String validEndAt;

    @Min(value = 1, message = "有效天数至少为1")
    @Schema(description = "领取后有效天数（validType=relative 必填）", example = "30")
    private Integer validDays;

    @Min(value = 1, message = "发放总量至少为1")
    @Schema(description = "发放总量，不传=不限量", example = "1000")
    private Integer totalQuantity;

    @NotNull(message = "每人限领不能为空")
    @Min(value = 1, message = "每人限领至少为1")
    @Schema(description = "每人限领张数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer perUserLimit;

    @Pattern(regexp = "active|inactive", message = "状态只能为active或inactive")
    @Schema(description = "状态，默认active", allowableValues = {"active", "inactive"})
    private String status;
}