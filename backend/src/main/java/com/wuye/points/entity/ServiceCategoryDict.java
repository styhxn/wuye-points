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
@Table(name = "service_category_dict")
@EqualsAndHashCode(callSuper = true)
public class ServiceCategoryDict extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "keywords", columnDefinition = "json")
    private String keywords;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "status", length = 20)
    private String status;
}
