package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小区表
 */
@Data
@Entity
@Table(name = "communities")
@EqualsAndHashCode(callSuper = true)
public class Community extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "total_buildings")
    private Integer totalBuildings;

    @Column(name = "total_units")
    private Integer totalUnits;

    @Column(name = "status", length = 20)
    private String status;
}
