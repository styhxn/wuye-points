package com.wuye.points.dto.admin.platformmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "商家批量配置请求")
public class BatchConfigRequest {
    @NotNull(message = "配置列表不能为空")
    @Valid
    @Schema(description = "配置列表")
    private List<MerchantConfigItem> items;

    @Data
    @Schema(description = "单个商家配置项")
    public static class MerchantConfigItem {
        @NotBlank(message = "商家ID不能为空")
        @Schema(description = "商家ID", example = "M001", requiredMode = Schema.RequiredMode.REQUIRED)
        private String merchantId;

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
    }
}
