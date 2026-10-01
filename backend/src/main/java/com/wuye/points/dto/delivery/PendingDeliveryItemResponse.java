package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "待配送项响应")
public class PendingDeliveryItemResponse {

    @Schema(description = "配送ID", example = "del_001")
    private String id;

    @Schema(description = "订单ID", example = "order_001")
    private String orderId;

    @Schema(description = "订单编号", example = "ORD20240101001")
    private String orderNo;

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "商家名称", example = "幸福超市")
    private String merchantName;

    @Schema(description = "商家地址", example = "幸福路100号")
    private String merchantAddress;

    @Schema(description = "取货地址", example = "幸福超市")
    private String pickupAddress;

    @Schema(description = "配送地址", example = "幸福家园1栋101")
    private String deliveryAddress;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "配送费用", example = "5.00")
    private BigDecimal fee;

    @Schema(description = "配送员收入", example = "4.00")
    private BigDecimal courierEarning;

    @Schema(description = "配送状态", example = "pending", allowableValues = {"pending", "accepted", "delivering", "completed", "failed", "property_fallback"})
    private String status;

    @Schema(description = "承运类型 courier(平台配送员)/merchant(商家自配)", example = "courier")
    private String carrierType;

    @Schema(description = "商家自配配送费分成（仅 carrierType=merchant 有值）", example = "4.50")
    private BigDecimal merchantDeliveryFeeShare;

    @Schema(description = "超时时间(分钟)", example = "30")
    private Integer timeoutMinutes;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private String createdAt;
}
