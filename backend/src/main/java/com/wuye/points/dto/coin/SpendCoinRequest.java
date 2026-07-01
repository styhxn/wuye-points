package com.wuye.points.dto.coin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpendCoinRequest {

    @NotBlank(message = "用户ID不能为空")
    private String residentId;

    @NotNull(message = "物业币数量不能为空")
    @DecimalMin(value = "0.01", message = "物业币数量至少为0.01")
    private BigDecimal coinAmount;

    @NotBlank(message = "来源不能为空")
    private String source;

    @Size(max = 200, message = "描述长度不能超过200")
    private String description;

    private String sourceId;

    private String familyId;

    private Boolean useFamilyPool;
}
