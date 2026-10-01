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
 * 内部结算转账单（merchant_transfers）。
 *
 * <p>电商收付通模式下，平台佣金经分账到平台商户号后，按四层树（物业/统筹/板块/个体）
 * 通过微信商家转账出款。状态机：pending → processing → success / failed（可重试）/ closed。
 * out_bill_no = 本表 id（mt_），保证微信侧幂等。</p>
 */
@Data
@Entity
@Table(name = "merchant_transfers")
@EqualsAndHashCode(callSuper = true)
public class MerchantTransfer extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 角色：PROPERTY/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER */
    @Column(name = "owner_type", length = 32, nullable = false)
    private String ownerType;

    /** 角色ID */
    @Column(name = "owner_id", length = 32, nullable = false)
    private String ownerId;

    /** 业务ID（订单号/分账 group_no） */
    @Column(name = "biz_id", length = 64)
    private String bizId;

    /** 转账金额（元） */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /** 转账渠道：zero/bank */
    @Column(name = "channel", length = 16, nullable = false)
    private String channel;

    /** 收款账号（openid 或脱敏银行卡，冗余展示） */
    @Column(name = "payee_account", length = 64)
    private String payeeAccount;

    /** pending/processing/success/failed/closed */
    @Column(name = "status", length = 24, nullable = false)
    private String status;

    /** 微信转账批次号 */
    @Column(name = "wx_batch_id", length = 64)
    private String wxBatchId;

    /** 微信转账单号 */
    @Column(name = "wx_transfer_bill_id", length = 64)
    private String wxTransferBillId;

    /** 失败原因 */
    @Column(name = "fail_reason", length = 500)
    private String failReason;

    /** 重试次数 */
    @Column(name = "retry_count")
    private Integer retryCount;
}
