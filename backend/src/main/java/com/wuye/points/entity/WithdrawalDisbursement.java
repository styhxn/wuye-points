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
 * 提现自动打款流水表（出金调用全生命周期）
 * 对应 withdrawal_disbursements 表（id 前缀 wd_）
 *
 * <p>自动打款（微信 applycash）是异步的：申请成功 != 到账。本表记录每次调用的
 * 请求/响应/状态/重试/异步回调，支撑失败重试、对账与状态机驱动。</p>
 */
@Data
@Entity
@Table(name = "withdrawal_disbursements")
@EqualsAndHashCode(callSuper = true)
public class WithdrawalDisbursement extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 关联提现单ID（merchant_withdrawals / withdrawal_records） */
    @Column(name = "withdrawal_id", length = 32, nullable = false)
    private String withdrawalId;

    /** merchant / role */
    @Column(name = "withdrawal_type", length = 20, nullable = false)
    private String withdrawalType;

    /** 打款渠道: auto_wechat */
    @Column(name = "channel", length = 20, nullable = false)
    private String channel;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /** 收款方类型: merchant / resident */
    @Column(name = "payee_type", length = 20)
    private String payeeType;

    /** 收款方ID */
    @Column(name = "payee_id", length = 32)
    private String payeeId;

    /** 收款结算卡号（脱敏存储） */
    @Column(name = "settle_card_no", length = 64)
    private String settleCardNo;

    /** 请求报文(JSON) */
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    /** 响应报文(JSON) */
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    /** 微信流水号 */
    @Column(name = "trade_no", length = 64)
    private String tradeNo;

    /** init / calling / success / failed */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /** 失败原因 */
    @Column(name = "error_msg", length = 512)
    private String errorMsg;

    /** 重试次数 */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /** 末次尝试时间 */
    @Column(name = "last_tried_at")
    private LocalDateTime lastTriedAt;

    /** 异步到账回调时间 */
    @Column(name = "callback_at")
    private LocalDateTime callbackAt;
}
