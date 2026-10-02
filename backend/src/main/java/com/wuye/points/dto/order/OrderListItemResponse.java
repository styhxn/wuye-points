package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@Schema(description = "订单列表项响应")
public class OrderListItemResponse {

    @Schema(description = "订单ID", example = "ord_abc123")
    private String id;

    @Schema(description = "订单编号", example = "20260101000001")
    private String orderNo;

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "用户姓名", example = "张先生")
    private String residentName;

    @Schema(description = "商家ID", example = "mer_abc123")
    private String merchantId;

    @Schema(description = "商家名称", example = "阳光超市")
    private String merchantName;

    @Schema(description = "商家封面URL", example = "https://example.com/merchant.jpg")
    private String merchantCoverUrl;

    @Schema(description = "订单状态", example = "pending")
    private String orderStatus;

    @Schema(description = "订单总金额", example = "99.90")
    private BigDecimal totalAmount;

    @Schema(description = "配送费", example = "5.00")
    private BigDecimal deliveryFee;

    @Schema(description = "支付方式", example = "wechat")
    private String paymentMethod;

    @Schema(description = "使用积分", example = "50")
    private Integer pointUsed;

    @Schema(description = "使用物业币", example = "10.00")
    private BigDecimal coinUsed;

    @Schema(description = "现金支付金额", example = "84.90")
    private BigDecimal cashAmount;

    @Schema(description = "使用的用户优惠券ID", example = "ucp_1a2b3c")
    private String couponId;

    @Schema(description = "优惠券抵扣金额", example = "20.00")
    private BigDecimal couponAmount;

    @Schema(description = "配送地址", example = "阳光花园1号楼1单元101")
    private String deliveryAddress;

    @Schema(description = "商品数量", example = "2")
    private Integer itemCount;

    @Schema(description = "商品快照列表")
    private List<OrderItemSnapshotResponse> items;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    @Schema(description = "快递员ID", example = "courier_001")
    private String courierId;

    @Schema(description = "支付时间", example = "2026-01-01T00:00:00.000Z")
    private String paidAt;

    @Schema(description = "送达时间", example = "2026-01-01T12:00:00.000Z")
    private String deliveredAt;

    @Schema(description = "完成时间", example = "2026-01-01T13:00:00.000Z")
    private String completedAt;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;

    @Schema(description = "是否需要配送（团购/社区食堂单为 false）", example = "true")
    private Boolean requiresDelivery;

    @Schema(description = "履约方式 pending_choice/merchant_self/courier_hall/none", example = "pending_choice")
    private String fulfillmentMode;

    @Schema(description = "履约方式中文", example = "待选择")
    private String fulfillmentModeLabel;

    @Schema(description = "履约方式选择截止时间", example = "2026-01-01T00:30:00.000Z")
    private String fulfillmentDeadline;

    @Schema(description = "履约方式选定时间", example = "2026-01-01T00:05:00.000Z")
    private String fulfillmentChosenAt;
}
