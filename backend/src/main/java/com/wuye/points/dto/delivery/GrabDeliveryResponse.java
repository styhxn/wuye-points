package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "抢单响应")
public class GrabDeliveryResponse {

    @Schema(description = "配送ID", example = "del_001")
    private String id;

    @Schema(description = "订单ID", example = "order_001")
    private String orderId;

    @Schema(description = "配送员ID", example = "courier_001")
    private String courierId;

    @Schema(description = "配送状态", example = "accepted", allowableValues = {"pending", "accepted", "delivering", "completed", "failed", "property_fallback"})
    private String status;

    @Schema(description = "配送员收入", example = "5.00")
    private BigDecimal courierEarning;

    @Schema(description = "接受时间", example = "2024-01-01T10:00:00")
    private String acceptedAt;

    @Schema(description = "超时时间", example = "2024-01-01T10:30:00")
    private String timeoutAt;
}
