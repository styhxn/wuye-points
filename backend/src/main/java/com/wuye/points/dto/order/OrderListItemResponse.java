package com.wuye.points.dto.order;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
public class OrderListItemResponse {

    private String id;
    private String orderNo;
    private String merchantId;
    private String merchantName;
    private String merchantCoverUrl;
    private String orderStatus;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String paymentMethod;
    private Integer pointUsed;
    private BigDecimal coinUsed;
    private BigDecimal cashAmount;
    private Integer itemCount;
    private List<OrderItemSnapshotResponse> items;
    private String familyId;
    private String courierId;
    private String paidAt;
    private String deliveredAt;
    private String completedAt;
    private String createdAt;
}
