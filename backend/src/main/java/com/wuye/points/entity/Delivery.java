package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送表
 * 对应 schema.sql 中 deliveries 表（id 前缀 dlv_）
 */
@Data
@Entity
@Table(name = "deliveries")
@EqualsAndHashCode(callSuper = true)
public class Delivery extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "order_id", length = 32, nullable = false)
    private String orderId;

    @Column(name = "courier_id", length = 32)
    private String courierId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal fee;

    @Column(name = "courier_earning", precision = 10, scale = 2)
    private BigDecimal courierEarning;

    @Column(name = "pickup_address", length = 200)
    private String pickupAddress;

    @Column(name = "delivery_address", length = 200)
    private String deliveryAddress;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}
