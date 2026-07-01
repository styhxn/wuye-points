package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 邻居私聊每日限制表
 * 对应 schema.sql 中 neighbor_contact_limits 表（id 前缀 ncl_）
 */
@Data
@Entity
@Table(name = "neighbor_contact_limits")
@EqualsAndHashCode(callSuper = true)
public class NeighborContactLimit extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "contact_date", nullable = false)
    private LocalDate contactDate;

    @Column(name = "contact_count")
    private Integer contactCount;
}
