package com.wuye.points.dto.propertyfee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建物业费请求")
public class CreatePropertyFeeRequest {

    @Schema(description = "住户ID", example = "res_b7m9x1", required = true)
    @NotBlank(message = "residentId不能为空")
    private String residentId;

    @Schema(description = "费用类型", example = "物业费", required = true)
    @NotBlank(message = "feeType不能为空")
    private String feeType;

    @Schema(description = "费用周期", example = "2026-01", required = true)
    @NotBlank(message = "period不能为空")
    private String period;

    @Schema(description = "费用金额", example = "1500.00", required = true)
    @NotNull(message = "amount不能为空")
    @DecimalMin(value = "0.01", message = "amount必须大于0")
    private BigDecimal amount;

    @Schema(description = "小区ID", example = "com_x9k2m")
    private String communityId;

    @Schema(description = "截止日期", example = "2026-01-01T00:00:00.000Z")
    private String dueDate;

    @Schema(description = "是否自动扣款", example = "false")
    private Boolean autoDeduct;
}
