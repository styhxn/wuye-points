package com.wuye.points.dto.propertyfee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayPropertyFeeRequest {

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    @Min(value = 0, message = "使用积分不能为负数")
    private Integer pointUsed;

    @DecimalMin(value = "0.00", message = "使用物业币不能为负数")
    private BigDecimal coinUsed;

    private String wechatPayCode;

    private String familyId;
}
