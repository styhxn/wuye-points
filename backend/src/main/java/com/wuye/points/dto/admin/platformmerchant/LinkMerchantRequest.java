package com.wuye.points.dto.admin.platformmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "关联商家请求")
public class LinkMerchantRequest {

    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;

    @NotNull(message = "抽佣比例不能为空")
    @DecimalMin(value = "0", message = "抽佣比例不能为负数")
    @Schema(description = "抽佣比例", example = "0.15", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal commissionRate;

    @Schema(description = "积分兑换比例", example = "1.0")
    private BigDecimal pointExchangeRate;

    @Schema(description = "会员折扣价格", example = "9.90")
    private BigDecimal memberDiscountPrice;

    @Schema(description = "金币返利比例", example = "0.05")
    private BigDecimal coinRebateRate;

    @Schema(description = "是否启用金币返利", example = "true")
    private Boolean coinRebateEnabled;

    @Schema(description = "商家等级", example = "普通")
    private String merchantLevel;

    @Schema(description = "排序顺序", example = "1")
    private Integer rankOrder;

    @Schema(description = "配送费", example = "3.00")
    private BigDecimal deliveryFee;

    @Schema(description = "免配送费门槛", example = "50.00")
    private BigDecimal freeDeliveryThreshold;
}
