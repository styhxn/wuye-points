package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 积分池表 pp_xxxxxxxx
 * 注：服务器库 point_pools 实际含 created_at / updated_at 两列（与 BaseEntity 对齐），
 *     此前“无 created_at”的注释与库结构不符，已更正（2026-09-07）。
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
