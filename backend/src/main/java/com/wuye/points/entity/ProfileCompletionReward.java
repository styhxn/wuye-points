package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 信息采集积分奖励配置表 pcr_xxxxxxxx
 * scope 取值：property_company/community/platform/coordinator
 */
@Data
@Entity
@Table(name = "profile_completion_rewards")
@EqualsAndHashCode(callSuper = true)
public class ProfileCompletionReward extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "scope", length = 20, nullable = false)
    private String scope;

    @Column(name = "scope_id", length = 32, nullable = false)
    private String scopeId;

    @Column(name = "field_name", length = 50, nullable = false)
    private String fieldName;

    @Column(name = "reward_points", nullable = false)
    private Integer rewardPoints;

    @Column(name = "once_per_user")
    private Boolean oncePerUser;

    @Column(name = "status", length = 20)
    private String status;
}
