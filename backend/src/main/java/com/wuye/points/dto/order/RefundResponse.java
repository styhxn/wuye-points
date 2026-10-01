package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundResponse {

    @Schema(description = "订单ID", example = "ord_xxx")
    private String orderId;

    @Schema(description = "订单状态", example = "refunding")
    private String orderStatus;

    @Schema(description = "退款申请时间（ISO-8601 UTC）", example = "2026-08-21T10:30:00Z")
    private String requestedAt;

    @Schema(description = "提示信息", example = "退款申请已提交，待物业审核")
    private String message;
}
