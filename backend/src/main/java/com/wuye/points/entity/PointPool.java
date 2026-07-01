package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 积分池表 pp_xxxxxxxx
 * 注：point_pools 表仅含 updated_at，无 created_at，
 *     继承 BaseEntity 后 createdAt 字段对应数据库列不存在，
 *     建表/查询时如遇问题需在子类覆盖或调整 BaseEntity。
 */
@Data
@Entity
@Table(name = "point_pools")
@EqualsAndHashCode(callSuper = true)
public class PointPool extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "total_in", precision = 10, scale = 2)
    private BigDecimal totalIn;

    @Column(name = "total_out", precision = 10, scale = 2)
    private BigDecimal totalOut;
}
