package com.wuye.points.dto.propertyfee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePropertyFeeRequest {

    @NotBlank(message = "residentId不能为空")
    private String residentId;

    @NotBlank(message = "feeType不能为空")
    private String feeType;

    @NotBlank(message = "period不能为空")
    private String period;

    @NotNull(message = "amount不能为空")
    @DecimalMin(value = "0.01", message = "amount必须大于0")
    private BigDecimal amount;

    private String communityId;

    private String dueDate;

    private Boolean autoDeduct;
}
