package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新配送状态请求")
public class UpdateDeliveryStatusRequest {

    @NotBlank(message = "status不能为空")
    @Schema(description = "配送状态", example = "delivering", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"pending", "accepted", "delivering", "completed", "failed", "property_fallback"})
    private String status;
}
