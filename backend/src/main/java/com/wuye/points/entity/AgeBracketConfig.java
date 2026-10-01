package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "age_bracket_configs")
@EqualsAndHashCode(callSuper = true)
public class AgeBracketConfig extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "label", length = 50, nullable = false)
    private String label;

    @Column(name = "min_age", nullable = false)
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "status", length = 20)
    private String status;
}
