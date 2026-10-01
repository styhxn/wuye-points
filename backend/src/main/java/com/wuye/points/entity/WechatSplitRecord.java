package com.wuye.points.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 微信普通分账流水表（wechat_split_record）。
 *
 * <p>每笔「源支付」按收款方拆分为多条记录（同一 group_no 共享一笔支付），
 * 状态机：frozen_pending → frozen → finished → (withdrawing → withdrawn)，
 * 失败终态：freeze_failed / finish_failed / cancel_failed / reverse_failed / refund_failed / skipped。</p>
 */
@Data
@Entity
@Table(name = "wechat_split_record")
public class WechatSplitRecord {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "payment_record_id", length = 32)
    private String paymentRecordId;

    @Column(name = "out_trade_no", length = 64)
    private String outTradeNo;

    @Column(name = "biz_type", length = 32)
    private String bizType;

    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "split_no", length = 64)
    private String splitNo;

    @Column(name = "wechat_sub_trade_no", length = 64)
    private String wechatSubTradeNo;

    @Column(name = "payer_account", length = 64)
    private String payerAccount;

    @Column(name = "payee_account", length = 64)
    private String payeeAccount;

    @Column(name = "freeze_amount", precision = 10, scale = 2)
    private BigDecimal freezeAmount;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "freeze_resp", length = 4000)
    private String freezeResp;

    @Column(name = "finish_resp", length = 4000)
    private String finishResp;

    @Column(name = "reverse_resp", length = 4000)
    private String reverseResp;

    @Column(name = "remark", length = 4000)
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "group_no", length = 32)
    private String groupNo;

    @Column(name = "owner_type", length = 32)
    private String ownerType;

    @Column(name = "owner_id", length = 32)
    private String ownerId;

    @Column(name = "currency", length = 8)
    private String currency;

    @Column(name = "wechat_out_trade_no", length = 64)
    private String wechatOutTradeNo;

    @Column(name = "withdraw_status", length = 32)
    private String withdrawStatus;

    @Column(name = "withdraw_resp", length = 4000)
    private String withdrawResp;

    @Column(name = "withdraw_trade_no", length = 64)
    private String withdrawTradeNo;
}
