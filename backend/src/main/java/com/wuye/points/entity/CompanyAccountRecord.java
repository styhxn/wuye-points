package com.wuye.points.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 公司账户资金流水表 car_xxxxxxxx
 * 注：表仅含 created_at，无 updated_at，故不继承 BaseEntity，
 *     仅声明 created_at 字段以适配 ddl-auto: validate。
 */
@Data
@Entity
@Table(name = "company_account_records")
@EntityListeners(AuditingEntityListener.class)
public class CompanyAccountRecord {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** leader_share/withdrawal_deposit/manual */
    @Column(name = "source", length = 30, nullable = false)
    private String source;

    @Column(name = "remark", length = 200)
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
