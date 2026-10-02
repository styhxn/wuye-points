package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 向业主商户下单（购买其上架商品）的支付试算请求。
 * 上架商品无商城 productId（非 prd_ 前缀），试算走独立口径，故独立于普通 PreviewOrderRequest。
 */
@Data
@Schema(description = "业主上架商品支付试算请求")
public class PreviewListingOrderRequest {

    @NotBlank(message = "上架ID不能为空")
    @Schema(description = "业主商户上架记录ID", required = true, example = "rml_abc123")
    private String listingId;

    @Min(value = 1, message = "数量至少为1")
    @Schema(description = "数量，默认1", example = "1")
    private Integer quantity;

    @DecimalMin(value = "0", message = "使用物业币不能为负数")
    @Schema(description = "期望抵扣的物业币金额", example = "20.00")
    private BigDecimal coinUsed;

    @Min(value = 0, message = "使用积分不能为负数")
    @Schema(description = "期望使用的积分", example = "100")
    private Integer pointUsed;

    @Schema(description = "家庭ID（使用家庭物业币池时传入）", example = "fam_abc123")
    private String familyId;

    @Schema(description = "期望使用的用户优惠券ID（user_coupon.id）", example = "ucp_1a2b3c")
    private String userCouponId;
}