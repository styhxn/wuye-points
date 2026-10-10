package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "分配配送响应")
public class AssignDeliveryResponse {

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

    @Schema(description = "配送费用", example = "5.00")
    private BigDecimal fee;

    @Schema(description = "满额减免前基础配送费", example = "5.00")
    private BigDecimal originalDeliveryFee;

    @Schema(description = "减免承担方 none/merchant/property/platform", example = "merchant")
    private String deliverySubsidySponsor;

    @Schema(description = "配送补贴金额", example = "0.00")
    private BigDecimal deliverySubsidyAmount;

    @Schema(description = "配送结算基数 = 实付配送费 + 配送补贴", example = "5.00")
    private BigDecimal deliverySettlementBase;

    @Schema(description = "公司配送收入 = 基数 × platformDeliveryShareRate", example = "0.50")
    private BigDecimal platformDeliveryShare;

    @Schema(description = "兼容旧字段，等价于 courierEarning", example = "4.00")
    private BigDecimal courierShare;

    @Schema(description = "结算状态 pending/settled/reversed", example = "pending")
    private String settlementStatus;

    @Schema(description = "接受时间", example = "2024-01-01T10:00:00")
    private String acceptedAt;
}
