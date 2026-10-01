package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 钱包流水账本（wf_xxxxxxxx）。
 *
 * <p>对齐《商品分账与满额配送费减免-移动端对齐规范》§7.4：每次入账/冲正都落一条不可变流水，
 * 通过 {@code idempotencyKey} 保证幂等（§9.5），退款/取消通过负数冲正流水完成（§10.5），不删除原流水。</p>
 *
 * <p>余额仍由订单/配送/分账状态实时聚合（与既有体系一致），本表作为官方审计账本，
 * 覆盖商家、公司（platform）、物业、统筹、板块负责人、个体负责人、配送员全部角色。</p>
 */
@Data
@Entity
@Table(name = "wallet_flows", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wallet_flows_idempotency", columnNames = "idempotency_key")
})
@EqualsAndHashCode(callSuper = true)
public class WalletFlow extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 收款/扣款主体类型：merchant/platform/property/coordinator/sector_leader/individual_leader/courier */
    @Column(name = "owner_type", length = 20, nullable = false)
    private String ownerType;

    /** 收款/扣款主体 ID（platform 固定为 "platform"） */
    @Column(name = "owner_id", length = 32, nullable = false)
    private String ownerId;

    @Column(name = "order_id", length = 32)
    private String orderId;

    /** 配送流水可关联配送单 */
    @Column(name = "delivery_id", length = 32)
    private String deliveryId;

    @Column(name = "distribution_record_id", length = 32)
    private String distributionRecordId;

    /** 入账/冲正类型（规范§7.4） */
    @Column(name = "entry_type", length = 40, nullable = false)
    private String entryType;

    /** 正数入账，负数冲正/扣减 */
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** pending/withdrawable/frozen/receivable */
    @Column(name = "balance_type", length = 20, nullable = false)
    private String balanceType;

    /** 幂等键，如 distribution:settle:{orderId}:... / delivery:settle:{deliveryId} / distribution:reverse:... */
    @Column(name = "idempotency_key", length = 100, nullable = false)
    private String idempotencyKey;

    /** 冲正流水关联的原流水 ID */
    @Column(name = "ref_record_id", length = 32)
    private String refRecordId;
}
