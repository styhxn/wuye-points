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
 * 商家积分录入表（商家将购买的积分录入给线下消费用户）
 * 对应 schema.sql 中 merchant_point_grants 表（id 前缀 mpg_）
 */
@Data
@Entity
@Table(name = "merchant_point_grants")
@EqualsAndHashCode(callSuper = true)
public class MerchantPointGrant extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "purchase_id", length = 32, nullable = false)
    private String purchaseId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "family_id", length = 32)
    private String familyId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @Column(name = "exchange_rate", precision = 10, scale = 4)
    private BigDecimal exchangeRate;

    @Column(name = "description", length = 200)
    private String description;
}
