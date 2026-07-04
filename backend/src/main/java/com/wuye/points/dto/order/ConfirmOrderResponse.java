package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "确认订单响应")
public class ConfirmOrderResponse {

    @Schema(description = "订单ID", example = "ord_abc123")
    private String orderId;

    @Schema(description = "订单状态", example = "completed")
    private String orderStatus;

    @Schema(description = "完成时间", example = "2026-01-01T00:00:00.000Z")
    private String completedAt;

    @Schema(description = "获得积分", example = "10")
    private Integer pointEarned;

    @Schema(description = "获得物业币", example = "2.00")
    private BigDecimal coinEarned;
}
