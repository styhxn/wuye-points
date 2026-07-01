package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物业币兑换现金表（商家将物业币兑换现金）
 * 对应 schema.sql 中 coin_withdrawals 表（id 前缀 cw_）
 */
@Data
@Entity
@Table(name = "coin_withdrawals")
@EqualsAndHashCode(callSuper = true)
public class CoinWithdrawal extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "coin_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal coinAmount;

    @Column(name = "cash_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal cashAmount;

    @Column(name = "fee_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal feeRate;

    @Column(name = "fee_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal feeAmount;

    @Column(name = "actual_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal actualAmount;

    @Column(name = "audit_status", length = 20)
    private String auditStatus;
}
