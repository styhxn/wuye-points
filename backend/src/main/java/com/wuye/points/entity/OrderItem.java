package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单商品表 oi_xxxxxxxx
 */
@Data
@Entity
@Table(name = "order_items")
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "order_id", length = 32, nullable = false)
    private String orderId;

    @Column(name = "product_id", length = 32, nullable = false)
    private String productId;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "product_cover", length = 500)
    private String productCover;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
