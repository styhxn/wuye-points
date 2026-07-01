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
 * 服务订单表
 * 对应 schema.sql 中 service_orders 表（id 前缀 so_）
 */
@Data
@Entity
@Table(name = "service_orders")
@EqualsAndHashCode(callSuper = true)
public class ServiceOrder extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "service_id", length = 32, nullable = false)
    private String serviceId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "provider_id", length = 32)
    private String providerId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "order_status", length = 20)
    private String orderStatus;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "service_time")
    private LocalDateTime serviceTime;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "remark", length = 500)
    private String remark;
}
