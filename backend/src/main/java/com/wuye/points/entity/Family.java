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
 * 家庭表
 */
@Data
@Entity
@Table(name = "families")
@EqualsAndHashCode(callSuper = true)
public class Family extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 50)
    private String name;

    /** 户主ID */
    @Column(name = "owner_id", length = 32, nullable = false)
    private String ownerId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "building", length = 20)
    private String building;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "room", length = 20)
    private String room;

    @Column(name = "member_count")
    private Integer memberCount;

    /** 家庭积分池 */
    @Column(name = "point_balance")
    private Integer pointBalance;

    /** 家庭物业币池 */
    @Column(name = "coin_balance", precision = 10, scale = 2)
    private BigDecimal coinBalance;

    /** 投票权重（一家一票=1） */
    @Column(name = "vote_weight")
    private Integer voteWeight;

    /** pending/approved/rejected */
    @Column(name = "audit_status", length = 20)
    private String auditStatus;

    @Column(name = "status", length = 20)
    private String status;
}
