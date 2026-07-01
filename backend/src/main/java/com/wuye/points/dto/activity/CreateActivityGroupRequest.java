package com.wuye.points.dto.activity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateActivityGroupRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    private String coverUrl;

    private String communityId;

    @DecimalMin(value = "0.00", message = "monthlyFee不能为负数")
    private BigDecimal monthlyFee;

    @DecimalMin(value = "0.00", message = "yearlyFee不能为负数")
    private BigDecimal yearlyFee;
}
