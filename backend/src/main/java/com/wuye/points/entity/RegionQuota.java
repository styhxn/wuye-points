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
 * 区域/板块额度配额（例如某板块周期内可发放的积分/资金额度）。
 */
@Data
@Entity
@Table(name = "region_quotas")
@EqualsAndHashCode(callSuper = true)
public class RegionQuota extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 板块/区域标识 */
    @Column(name = "sector", length = 50, nullable = false)
    private String sector;

    /** 配额类型 point / amount */
    @Column(name = "quota_type", length = 20, nullable = false)
    private String quotaType;

    /** 配额总量 */
    @Column(name = "quota_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal quotaAmount;

    /** 已使用量 */
    @Column(name = "used_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal usedAmount;

    /** 周期开始日期 */
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    /** 周期结束日期 */
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /** 状态 active / disabled */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "remark", length = 200)
    private String remark;
}
