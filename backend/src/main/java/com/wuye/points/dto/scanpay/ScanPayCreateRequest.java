package com.wuye.points.dto.scanpay;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** 商家扫码收银台：输金额后一次建单并拿到微信支付参数。 */
@Schema(description = "扫码付款创建请求")
@Data
public class ScanPayCreateRequest {

    @Schema(description = "收款商家ID（来自小程序码 merchantId 参数）", example = "mch_demo002", required = true)
    @NotBlank(message = "商家ID不能为空")
    private String merchantId;

    @Schema(description = "付款金额（元，两位小数，0.01~50000）", example = "28.50", required = true)
    @NotNull(message = "付款金额不能为空")
    @DecimalMin(value = "0.01", message = "付款金额最小0.01元")
    @DecimalMax(value = "50000.00", message = "付款金额最大50000元")
    @Digits(integer = 8, fraction = 2, message = "付款金额最多两位小数")
    private BigDecimal amount;
}
