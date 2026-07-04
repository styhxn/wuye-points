package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "取消订单响应")
public class CancelOrderResponse {

    @Schema(description = "订单ID", example = "ord_abc123")
    private String orderId;

    @Schema(description = "订单状态", example = "cancelled")
    private String orderStatus;

    @Schema(description = "取消时间", example = "2026-01-01T00:00:00.000Z")
    private String cancelledAt;

    @Schema(description = "退款金额", example = "99.90")
    private BigDecimal refundAmount;

    @Schema(description = "退还积分", example = "50")
    private Integer refundPoint;

    @Schema(description = "退还物业币", example = "10.00")
    private BigDecimal refundCoin;
}
