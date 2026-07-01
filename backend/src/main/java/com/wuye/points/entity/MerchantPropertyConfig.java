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
 * 商家-物业参数配置表（同一商家在不同物业下独立设定参数）
 */
@Data
@Entity
@Table(name = "merchant_property_configs")
@EqualsAndHashCode(callSuper = true)
public class MerchantPropertyConfig extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 抽佣比例（0.05=5%） */
    @Column(name = "commission_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal commissionRate;

    /** 积分兑换比（200积分=1元则填200） */
    @Column(name = "point_exchange_rate", precision = 10, scale = 4)
    private BigDecimal pointExchangeRate;

    /** 业主折扣价 */
    @Column(name = "member_discount_price", precision = 10, scale = 2)
    private BigDecimal memberDiscountPrice;

    /** 物业币返现比（仅高抽佣≥20%商家生效） */
    @Column(name = "coin_rebate_rate", precision = 5, scale = 4)
    private BigDecimal coinRebateRate;

    /** 是否启用物业币返现 */
    @Column(name = "coin_rebate_enabled")
    private Boolean coinRebateEnabled;

    @Column(name = "status", length = 20)
    private String status;
}
