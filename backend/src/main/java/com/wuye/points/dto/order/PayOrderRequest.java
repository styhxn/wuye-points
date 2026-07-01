package com.wuye.points.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayOrderRequest {

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Min(value = 0, message = "使用积分不能为负数")
    private Integer pointUsed;

    @Min(value = 0, message = "使用物业币不能为负数")
    private BigDecimal coinUsed;

    private String wechatPayCode;

    private String familyId;
}
