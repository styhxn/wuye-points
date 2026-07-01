package com.wuye.points.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderItemSnapshotResponse {

    private String productId;
    private String productName;
    private String productCover;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
