package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-发起购买额度请求
 */
@Data
@Schema(description = "发起购买额度请求")
public class QuotaPurchaseRequest {

    @DecimalMin(value = "0.01", message = "购买金额必须大于0")
    @Schema(description = "申请购买额度金额", required = true, example = "100.00")
    private BigDecimal amount;

    @Schema(description = "用户openid（微信小程序支付需要；不传则取店主绑定openid）", example = "oXXXXXX")
    private String openid;
}
