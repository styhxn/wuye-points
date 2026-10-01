package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新分销商品收费周期请求")
public class UpdateBillingCycleRequest {

    @NotBlank(message = "billingCycle不能为空")
    @Schema(description = "收费周期: one_time/monthly/quarterly/yearly",
            example = "monthly", requiredMode = Schema.RequiredMode.REQUIRED)
    private String billingCycle;
}
