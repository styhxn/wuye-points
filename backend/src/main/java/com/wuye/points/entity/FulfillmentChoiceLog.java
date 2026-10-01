package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 履约方式选择审计日志表。
 *
 * <p>所有履约方式的选择、超时兜底、管理员覆盖都会留痕，便于追溯：
 * <ul>
 *   <li>商家主动选择（choose）</li>
 *   <li>超时自动兜底（timeout / auto_choose）</li>
 *   <li>管理员强制覆盖（override）</li>
 * </ul>
 *
 * <p>对应表 {@code fulfillment_choice_logs}（id 前缀 fcl_）。
 */
@Data
@Entity
@Table(name = "fulfillment_choice_logs")
@EqualsAndHashCode(callSuper = true)
public class FulfillmentChoiceLog extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 关联订单ID */
    @Column(name = "order_id", length = 32, nullable = false)
    private String orderId;

    /** 关联商家ID */
    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    /** 触发方：merchant / system / admin */
    @Column(name = "actor_type", length = 20, nullable = false)
    private String actorType;

    /** 触发方ID（商家ID / 管理员ID / 系统固定值 system） */
    @Column(name = "actor_id", length = 32, nullable = false)
    private String actorId;

    /** 审计动作：choose / auto_choose / timeout / override */
    @Column(name = "action", length = 20, nullable = false)
    private String action;

    /** 变更前履约方式 */
    @Column(name = "before_mode", length = 20)
    private String beforeMode;

    /** 变更后履约方式：merchant_self / courier_hall */
    @Column(name = "after_mode", length = 20, nullable = false)
    private String afterMode;

    /** 选择/兜底说明（如“超时未选，默认平台配送”“管理员强制改派”） */
    @Column(name = "remark", length = 200)
    private String remark;

    /** 该次选择生成的配送单ID（若有） */
    @Column(name = "delivery_id", length = 32)
    private String deliveryId;

    /** 操作时间（冗余，便于按业务时间检索） */
    @Column(name = "choice_at")
    private LocalDateTime choiceAt;
}
