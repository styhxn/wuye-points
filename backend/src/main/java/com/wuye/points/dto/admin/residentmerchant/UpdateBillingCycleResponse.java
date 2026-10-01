package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "更新分销商品收费周期响应")
public class UpdateBillingCycleResponse {

    @Schema(description = "分销商品ID", example = "dpr_001")
    private String id;

    @Schema(description = "收费周期", example = "monthly")
    private String billingCycle;
}
