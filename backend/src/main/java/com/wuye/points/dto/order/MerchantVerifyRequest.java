package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商家核实订单请求
 */
@Data
@Schema(description = "商家核实订单请求")
public class MerchantVerifyRequest {

    @Schema(description = "是否通过", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean approved;

    @Schema(description = "核实备注（拒绝时建议填写原因）")
    private String reason;
}
