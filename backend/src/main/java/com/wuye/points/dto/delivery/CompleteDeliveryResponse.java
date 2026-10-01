package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "完成配送响应")
public class CompleteDeliveryResponse {

    @Schema(description = "配送ID", example = "del_001")
    private String id;

    @Schema(description = "订单ID", example = "order_001")
    private String orderId;

    @Schema(description = "配送状态", example = "completed", allowableValues = {"pending", "accepted", "delivering", "completed", "failed", "property_fallback"})
    private String status;

    @Schema(description = "配送员收入", example = "5.00")
    private BigDecimal courierEarning;

    @Schema(description = "商家自配配送费分成（仅 carrierType=merchant 的配送单有值）", example = "4.50")
    private BigDecimal merchantDeliveryFeeShare;

    @Schema(description = "承运类型 courier/merchant", example = "courier")
    private String carrierType;

    @Schema(description = "送达时间", example = "2024-01-01T11:00:00")
    private String deliveredAt;
}
