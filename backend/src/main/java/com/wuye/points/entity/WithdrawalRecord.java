package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 多角色通用提现记录表
 * 对应 withdrawal_records 表（id 前缀 wr_）
 */
@Data
@Entity
@Table(name = "withdrawal_records")
@EqualsAndHashCode(callSuper = true)
public class WithdrawalRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 提现类型: individual_leader / courier */
    @Column(name = "withdrawal_type", length = 30, nullable = false)
    private String withdrawalType;

    /** 申请人主体ID（个体负责人ID或快递负责人ID） */
    @Column(name = "applicant_id", length = 32, nullable = false)
    private String applicantId;

    /** 关联住户ID */
    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "fee_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal feeRate;

    @Column(name = "fee_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal feeAmount;

    /** 平台管理员从提现手续费中的分成 */
    @Column(name = "platform_fee_share", precision = 10, scale = 2)
    private BigDecimal platformFeeShare;

    @Column(name = "actual_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal actualAmount;

    /** pending/approved/rejected/completed */
    @Column(name = "audit_status", length = 20)
    private String auditStatus;

    @Column(name = "auditor_id", length = 32)
    private String auditorId;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** 打款渠道: manual(人工)/auto_wechat(微信自动) */
    @Column(name = "disburse_channel", length = 20)
    private String disburseChannel;

    /** 微信打款流水号(trade_no) */
    @Column(name = "disburse_trade_no", length = 64)
    private String disburseTradeNo;

    /** 打款状态: pending/success/failed */
    @Column(name = "disburse_status", length = 20)
    private String disburseStatus;

    /** 打款完成时间 */
    @Column(name = "disburse_at")
    private LocalDateTime disburseAt;

    /** 打款失败原因/备注 */
    @Column(name = "disburse_remark", length = 255)
    private String disburseRemark;

    /** 人工转账凭证URL */
    @Column(name = "transfer_proof", length = 512)
    private String transferProof;
}
