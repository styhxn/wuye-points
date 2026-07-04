package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "支付订单响应")
public class PayOrderResponse {

    @Schema(description = "订单ID", example = "ord_abc123")
    private String orderId;

    @Schema(description = "订单编号", example = "20260101000001")
    private String orderNo;

    @Schema(description = "订单状态", example = "paid")
    private String orderStatus;

    @Schema(description = "支付方式", example = "wechat")
    private String paymentMethod;

    @Schema(description = "支付金额", example = "99.90")
    private BigDecimal paidAmount;

    @Schema(description = "使用积分", example = "50")
    private Integer pointUsed;

    @Schema(description = "使用物业币", example = "10.00")
    private BigDecimal coinUsed;

    @Schema(description = "现金支付金额", example = "84.90")
    private BigDecimal cashAmount;

    @Schema(description = "支付时间", example = "2026-01-01T00:00:00.000Z")
    private String paidAt;

    @Schema(description = "获得积分", example = "10")
    private Integer pointEarned;

    @Schema(description = "获得物业币", example = "2.00")
    private BigDecimal coinEarned;
}
