package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "更新订单状态响应")
public class UpdateOrderStatusResponse {

    @Schema(description = "订单ID", example = "ord_abc123")
    private String orderId;

    @Schema(description = "订单状态", example = "shipped")
    private String orderStatus;

    @Schema(description = "快递员ID", example = "courier_001")
    private String courierId;

    @Schema(description = "送达时间", example = "2026-01-01T12:00:00.000Z")
    private String deliveredAt;

    @Schema(description = "版本号", example = "2")
    private Integer version;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;
}
