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
 * 单元楼长表（building_leaders，id 前缀 bl_）
 *
 * <p>楼长是与「小区 + 单元」绑定的推广角色，与活动组、楼栋无关。
 * 单元内住户产生消费时，楼长按 commissionRate（占物业公司分成的比例）分成。
 * 订单创建时快照 building_leader_id / building_leader_rate，保证分成定向绑定。
 */
@Data
@Entity
@Table(name = "building_leaders")
@EqualsAndHashCode(callSuper = true)
public class BuildingLeader extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32, nullable = false)
    private String communityId;

    @Column(name = "unit", length = 20, nullable = false)
    private String unit;

    /** 楼长分成比例：占物业公司分成的比例（0~1） */
    @Column(name = "commission_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal commissionRate;

    @Column(name = "description", length = 500)
    private String description;

    /** active启用 / inactive停用 */
    @Column(name = "status", length = 20)
    private String status;
}