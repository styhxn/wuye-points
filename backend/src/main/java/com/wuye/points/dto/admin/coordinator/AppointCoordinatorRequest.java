package com.wuye.points.dto.admin.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "指定片区协调员请求")
public class AppointCoordinatorRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "住户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "RES001")
    private String residentId;

    @Schema(description = "佣金比例", example = "0.05")
    private BigDecimal commissionRate;

    @Schema(description = "生效日期", example = "2025-01-01")
    private String startDate;

    @Schema(description = "到期日期", example = "2025-12-31")
    private String endDate;

    @Schema(description = "备注", example = "新任协调员")
    private String remark;

    @Schema(description = "状态", example = "active")
    private String status;
}
