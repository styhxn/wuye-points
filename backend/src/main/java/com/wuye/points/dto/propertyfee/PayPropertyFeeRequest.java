package com.wuye.points.dto.propertyfee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "物业费支付请求")
public class PayPropertyFeeRequest {

    @Schema(description = "支付方式", example = "微信支付", required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Schema(description = "支付金额", example = "1500.00", required = true)
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "使用积分", example = "100")
    @Min(value = 0, message = "使用积分不能为负数")
    private Integer pointUsed;

    @Schema(description = "使用物业币", example = "50.00")
    @DecimalMin(value = "0.00", message = "使用物业币不能为负数")
    private BigDecimal coinUsed;

    @Schema(description = "微信支付码", example = "wx20260101123456")
    private String wechatPayCode;

    @Schema(description = "家庭ID", example = "fam_k3j8n2")
    private String familyId;
}
