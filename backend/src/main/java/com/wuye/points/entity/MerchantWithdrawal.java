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
 * 商家提现表
 * 对应 schema.sql 中 merchant_withdrawals 表（id 前缀 mw_）
 */
@Data
@Entity
@Table(name = "merchant_withdrawals")
@EqualsAndHashCode(callSuper = true)
public class MerchantWithdrawal extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "fee_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal feeRate;

    @Column(name = "fee_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal feeAmount;

    @Column(name = "actual_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal actualAmount;

    @Column(name = "audit_status", length = 20)
    private String auditStatus;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    @Column(name = "auditor_id", length = 32)
    private String auditorId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
