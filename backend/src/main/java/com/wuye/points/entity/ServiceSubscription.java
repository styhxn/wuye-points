package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 服务订阅表（包月/包年）
 * 对应 schema.sql 中 service_subscriptions 表（id 前缀 ss_）
 */
@Data
@Entity
@Table(name = "service_subscriptions")
@EqualsAndHashCode(callSuper = true)
public class ServiceSubscription extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "service_id", length = 32, nullable = false)
    private String serviceId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "sub_type", length = 20, nullable = false)
    private String subType;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "auto_renew")
    private Boolean autoRenew;
}
