package com.wuye.points.dto.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新优惠券模板（仅传需变更字段）。发放数量/类型/范围不可变更。
 */
@Data
@Schema(description = "更新优惠券模板请求")
public class UpdateCouponTemplateRequest {

    @Size(max = 100, message = "券名称长度不能超过100")
    @Schema(description = "券名称")
    private String name;

    @Size(max = 500, message = "描述长度不能超过500")
    @Schema(description = "使用说明")
    private String description;

    @DecimalMin(value = "0.01", message = "面额必须大于0")
    @Schema(description = "面额（满减/立减）")
    private BigDecimal valueAmount;

    @DecimalMin(value = "0.01", message = "折扣率必须大于0")
    @Schema(description = "折扣率，8.50 表示 8.5 折")
    private BigDecimal discountRate;

    @DecimalMin(value = "0.01", message = "折扣封顶必须大于0")
    @Schema(description = "折扣券封顶金额")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0", message = "使用门槛不能为负数")
    @Schema(description = "使用门槛")
    private BigDecimal minSpendAmount;

    @Schema(description = "有效期开始（fixed）")
    private String validStartAt;

    @Schema(description = "有效期结束（fixed）")
    private String validEndAt;

    @Min(value = 1, message = "有效天数至少为1")
    @Schema(description = "领取后有效天数（relative）")
    private Integer validDays;

    @Min(value = 1, message = "发放总量至少为1")
    @Schema(description = "发放总量")
    private Integer totalQuantity;

    @Min(value = 1, message = "每人限领至少为1")
    @Schema(description = "每人限领张数")
    private Integer perUserLimit;

    @Pattern(regexp = "active|inactive", message = "状态只能为active或inactive")
    @Schema(description = "状态")
    private String status;
}