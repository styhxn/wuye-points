package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 积分记录表 pt_xxxxxxxx
 */
@Data
@Entity
@Table(name = "point_records")
@EqualsAndHashCode(callSuper = true)
public class PointRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "family_id", length = 32)
    private String familyId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "point_type", length = 30, nullable = false)
    private String pointType;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "balance", nullable = false)
    private Integer balance;

    @Column(name = "source", length = 30)
    private String source;

    @Column(name = "source_id", length = 32)
    private String sourceId;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "operator_id", length = 32)
    private String operatorId;
}
