package com.wuye.points.dto.coin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FreezeCoinRequest {

    @NotBlank(message = "用户ID不能为空")
    private String residentId;

    @DecimalMin(value = "0", message = "冻结金额不能为负")
    private BigDecimal amount;

    @NotBlank(message = "冻结原因不能为空")
    @Size(min = 1, max = 200, message = "冻结原因长度需在1-200之间")
    private String reason;
}
