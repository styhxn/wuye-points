package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 四级分成记录表 dr_xxxxxxxx
 */
@Data
@Entity
@Table(name = "distribution_records")
@EqualsAndHashCode(callSuper = true)
public class DistributionRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "order_id", length = 32, nullable = false)
    private String orderId;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "point_cost", precision = 10, scale = 2)
    private BigDecimal pointCost;

    @Column(name = "coin_cost", precision = 10, scale = 2)
    private BigDecimal coinCost;

    @Column(name = "distributable_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal distributableAmount;

    @Column(name = "property_share", nullable = false, precision = 10, scale = 2)
    private BigDecimal propertyShare;

    @Column(name = "coordinator_share", nullable = false, precision = 10, scale = 2)
    private BigDecimal coordinatorShare;

    @Column(name = "sector_leader_share", precision = 10, scale = 2)
    private BigDecimal sectorLeaderShare;

    @Column(name = "individual_leader_share", precision = 10, scale = 2)
    private BigDecimal individualLeaderShare;

    @Column(name = "coordinator_id", length = 32)
    private String coordinatorId;

    @Column(name = "sector_leader_id", length = 32)
    private String sectorLeaderId;

    @Column(name = "individual_leader_id", length = 32)
    private String individualLeaderId;

    /** 单元楼长分成（从物业公司分成中定向扣除） */
    @Column(name = "building_leader_share", precision = 10, scale = 2)
    private BigDecimal buildingLeaderShare;

    /** 楼长ID（bl_） */
    @Column(name = "building_leader_id", length = 32)
    private String buildingLeaderId;

    /** 楼长分成比例快照（占物业分成的比例） */
    @Column(name = "building_leader_rate", precision = 5, scale = 4)
    private BigDecimal buildingLeaderRate;

    /** 物业经理ID快照（pm_ 前缀，抽取楼长分成） */
    @Column(name = "manager_id", length = 32)
    private String managerId;

    /** 物业经理抽成比例快照（占楼长分成的比例） */
    @Column(name = "manager_rate", precision = 5, scale = 4)
    private BigDecimal managerRate;

    /** 物业经理抽成金额（从楼长分成毛额中抽取；2026-10-18 新增，历史数据为 null） */
    @Column(name = "manager_share", precision = 10, scale = 2)
    private BigDecimal managerShare;

    /** 用户积分预算比例快照（占抽佣基础额；V36 新增，历史数据为 null） */
    @Column(name = "user_point_share_rate", precision = 5, scale = 4)
    private BigDecimal userPointShareRate;

    /** 用户奖励金预算比例快照（占抽佣基础额；V36 新增，历史数据为 null） */
    @Column(name = "user_reward_share_rate", precision = 5, scale = 4)
    private BigDecimal userRewardShareRate;

    /** 本次从平台盘切出的用户积分预算（元；V36 新增，历史数据为 null） */
    @Column(name = "user_point_budget", precision = 10, scale = 2)
    private BigDecimal userPointBudget;

    /** 本次从平台盘切出的用户奖励金预算（元；V36 新增，历史数据为 null） */
    @Column(name = "user_reward_budget", precision = 10, scale = 2)
    private BigDecimal userRewardBudget;

    /** 平台分成（金额） */
    @Column(name = "platform_share", precision = 10, scale = 2)
    private BigDecimal platformShare;

    /** 平台收益（金额） */
    @Column(name = "platform_earning", precision = 10, scale = 2)
    private BigDecimal platformEarning;

    /** 平台手续费分成（金额） */
    @Column(name = "platform_fee_share", precision = 10, scale = 2)
    private BigDecimal platformFeeShare;
}
