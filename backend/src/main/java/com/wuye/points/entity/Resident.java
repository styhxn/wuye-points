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
 * 业主/住户表
 */
@Data
@Entity
@Table(name = "residents")
@EqualsAndHashCode(callSuper = true)
public class Resident extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 微信openid */
    @Column(name = "openid", length = 100, unique = true)
    private String openid;

    /** 微信unionid */
    @Column(name = "unionid", length = 100)
    private String unionid;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /** 0未知 1男 2女 */
    @Column(name = "gender")
    private Integer gender;

    @Column(name = "age")
    private Integer age;

    /** single/married/divorced/widowed */
    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "has_children")
    private Boolean hasChildren;

    /** owner业主/tenant租住人员 */
    @Column(name = "user_type", length = 20)
    private String userType;

    /** 见角色枚举 */
    @Column(name = "role", length = 30)
    private String role;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "building", length = 20)
    private String building;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "room", length = 20)
    private String room;

    @Column(name = "family_id", length = 32)
    private String familyId;

    /** owner/spouse/child/parent/sibling/other */
    @Column(name = "family_relation", length = 20)
    private String familyRelation;

    @Column(name = "point_balance")
    private Integer pointBalance;

    @Column(name = "coin_balance", precision = 10, scale = 2)
    private BigDecimal coinBalance;

    /** 物业币是否冻结 */
    @Column(name = "coin_frozen")
    private Boolean coinFrozen;

    /** 物业币卡片是否隐藏 */
    @Column(name = "coin_hidden")
    private Boolean coinHidden;

    @Column(name = "wechat_bound")
    private Boolean wechatBound;

    /** active/frozen/disabled */
    @Column(name = "status", length = 20)
    private String status;
}
