package com.wuye.points.dto.admin.pointpool;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "调整积分池请求")
public class AdjustPointPoolRequest {

    @NotNull(message = "调整金额不能为空")
    @Schema(description = "调整金额，正数为增加，负数为扣减", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotBlank(message = "说明不能为空")
    @Schema(description = "调整说明", example = "业主充值积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;
}
