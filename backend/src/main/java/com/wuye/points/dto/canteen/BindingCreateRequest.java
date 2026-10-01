package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-后台绑定子商家到主商家请求
 */
@Data
@Schema(description = "后台绑定子商家到主商家请求")
public class BindingCreateRequest {

    @NotBlank(message = "主商家ID不能为空")
    @Schema(description = "主商家ID（餐饮公司）", required = true, example = "mch_xxx")
    private String mainMerchantId;

    @NotBlank(message = "子商家ID不能为空")
    @Schema(description = "子商家ID", required = true, example = "mch_yyy")
    private String subMerchantId;

    @DecimalMin(value = "0", message = "供货优惠比例不能小于0")
    @DecimalMax(value = "1", message = "供货优惠比例不能大于1")
    @Schema(description = "供货优惠比例（0.20=子商家给主商家供货8折）", example = "0.20")
    private BigDecimal supplyDiscountRate;
}
