package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物业币冻结/解冻记录表 cfr_xxxxxxxx
 */
@Data
@Entity
@Table(name = "coin_freeze_records")
@EqualsAndHashCode(callSuper = true)
public class CoinFreezeRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "action", length = 20, nullable = false)
    private String action;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "operator_id", length = 32, nullable = false)
    private String operatorId;
}
