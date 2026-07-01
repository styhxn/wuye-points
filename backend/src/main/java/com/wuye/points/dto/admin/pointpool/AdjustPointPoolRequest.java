package com.wuye.points.dto.admin.pointpool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustPointPoolRequest {

    @NotNull(message = "调整金额不能为空")
    private BigDecimal amount;

    @NotBlank(message = "说明不能为空")
    private String description;

    private String propertyCompanyId;
}
