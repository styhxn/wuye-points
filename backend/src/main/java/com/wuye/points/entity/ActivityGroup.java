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
 * 活动组表
 * 对应 schema.sql 中 activity_groups 表（id 前缀 ag_）
 */
@Data
@Entity
@Table(name = "activity_groups")
@EqualsAndHashCode(callSuper = true)
public class ActivityGroup extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "leader_id", length = 32, nullable = false)
    private String leaderId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "member_count")
    private Integer memberCount;

    @Column(name = "monthly_fee", precision = 10, scale = 2)
    private BigDecimal monthlyFee;

    @Column(name = "yearly_fee", precision = 10, scale = 2)
    private BigDecimal yearlyFee;

    @Column(name = "status", length = 20)
    private String status;
}
