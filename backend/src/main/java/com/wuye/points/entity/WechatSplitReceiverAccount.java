package com.wuye.points.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信收款账户注册表（wechat_split_receiver_accounts）。
 *
 * <p>记录各业务主体（商家 / 配送员 / 物业 / 平台等）的微信账户（商户号或 openid），
 * 用于普通分账时定位接收方。verified=true 才可作为有效接收方。</p>
 */
@Data
@Entity
@Table(name = "wechat_split_receiver_accounts")
public class WechatSplitReceiverAccount {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "owner_type", length = 32, nullable = false)
    private String ownerType;

    @Column(name = "owner_id", length = 32, nullable = false)
    private String ownerId;

    @Column(name = "merchant_no", length = 64)
    private String merchantNo;

    @Column(name = "account_no", length = 64)
    private String accountNo;

    @Column(name = "account_name", length = 128)
    private String accountName;

    @Column(name = "currency", length = 8)
    private String currency;

    @Column(name = "verified")
    private boolean verified;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
