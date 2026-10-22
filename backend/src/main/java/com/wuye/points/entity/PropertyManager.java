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
 * 物业公司经理表（property_managers，id 前缀 pm_）
 *
 * <p>物业经理是「住户 + 物业经理」复合身份（不占用 residents.role），
 * 从该物业公司订单产生的单元楼长分成中按 managerRate 抽成。
 * 订单创建时快照 manager_id / manager_rate 到 orders 与 distribution_records。
 * 同一物业公司同一时刻仅允许一名 active 经理（应用层校验）。
 */
@Data
@Entity
@Table(name = "property_managers")
@EqualsAndHashCode(callSuper = true)
public class PropertyManager extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 经理对应的住户ID（residents.id，r_ 前缀） */
    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 经理抽成比例：占单元楼长分成的比例（0~1，默认 0） */
    @Column(name = "commission_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal commissionRate;

    /** active在职 / inactive停用 */
    @Column(name = "status", length = 20, nullable = false)
    private String status;
}
