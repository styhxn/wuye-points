package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 四级分成记录表 dr_xxxxxxxx
 */
@Data
@Entity
@Table(name = "distribution_records")
@EqualsAndHashCode(callSuper = true)
public class DistributionRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "order_id", length = 32, nullable = false)
    private String orderId;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "point_cost", precision = 10, scale = 2)
    private BigDecimal pointCost;

    @Column(name = "coin_cost", precision = 10, scale = 2)
    private BigDecimal coinCost;

    @Column(name = "distributable_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal distributableAmount;

    @Column(name = "property_share", nullable = false, precision = 10, scale = 2)
    private BigDecimal propertyShare;

    @Column(name = "coordinator_share", nullable = false, precision = 10, scale = 2)
    private BigDecimal coordinatorShare;

    @Column(name = "sector_leader_share", precision = 10, scale = 2)
    private BigDecimal sectorLeaderShare;

    @Column(name = "individual_leader_share", precision = 10, scale = 2)
    private BigDecimal individualLeaderShare;

    @Column(name = "coordinator_id", length = 32)
    private String coordinatorId;

    @Column(name = "sector_leader_id", length = 32)
    private String sectorLeaderId;

    @Column(name = "individual_leader_id", length = 32)
    private String individualLeaderId;

    /** 平台分成（金额） */
    @Column(name = "platform_share", precision = 10, scale = 2)
    private BigDecimal platformShare;

    /** 平台收益（金额） */
    @Column(name = "platform_earning", precision = 10, scale = 2)
    private BigDecimal platformEarning;

    /** 平台手续费分成（金额） */
    @Column(name = "platform_fee_share", precision = 10, scale = 2)
    private BigDecimal platformFeeShare;
}
