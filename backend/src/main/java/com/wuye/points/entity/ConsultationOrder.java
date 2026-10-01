package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "consultation_orders")
@EqualsAndHashCode(callSuper = true)
public class ConsultationOrder extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "consultant_id", length = 32, nullable = false)
    private String consultantId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 20, nullable = false)
    private String paymentMethod;

    @Column(name = "status", length = 20)
    private String status;
}
