package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "更新配送状态响应")
public class UpdateDeliveryStatusResponse {

    @Schema(description = "配送ID", example = "del_001")
    private String id;

    @Schema(description = "订单ID", example = "order_001")
    private String orderId;

    @Schema(description = "配送状态", example = "delivering", allowableValues = {"pending", "accepted", "delivering", "completed", "failed", "property_fallback"})
    private String status;

    @Schema(description = "取货时间", example = "2024-01-01T10:15:00")
    private String pickedAt;

    @Schema(description = "更新时间", example = "2024-01-01T10:45:00")
    private String updatedAt;
}
