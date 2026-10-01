package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 四层树角色收款账号（merchant_transfer_accounts）。
 *
 * <p>四层分账树（物业/统筹/板块/个体）不在微信侧分账（接收方多为无商户号个人），
 * 由平台内部结算后通过「微信商家转账」出款。本表维护各角色收款渠道：
 * zero（微信零钱，openid）或 bank（银行卡，卡号/行名加密存储），核验通过（verified=1）才可发起转账。</p>
 */
@Data
@Entity
@Table(name = "merchant_transfer_accounts")
@EqualsAndHashCode(callSuper = true)
public class MerchantTransferAccount extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 角色：PROPERTY/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER */
    @Column(name = "owner_type", length = 32, nullable = false)
    private String ownerType;

    /** 角色ID（物业公司/统筹/板块/个体负责人ID） */
    @Column(name = "owner_id", length = 32, nullable = false)
    private String ownerId;

    /** 收款渠道：zero(微信零钱)/bank(银行卡) */
    @Column(name = "channel", length = 16, nullable = false)
    private String channel;

    /** 微信 openid（zero 渠道） */
    @Column(name = "openid", length = 64)
    private String openid;

    /** 银行卡号（RSA 加密，bank 渠道） */
    @Column(name = "bank_account_no_enc", length = 1024)
    private String bankAccountNoEnc;

    /** 开户银行全称（bank 渠道） */
    @Column(name = "bank_name", length = 128)
    private String bankName;

    /** 持卡人姓名（RSA 加密，bank 渠道） */
    @Column(name = "account_name_enc", length = 512)
    private String accountNameEnc;

    /** 加密所用微信平台证书序列号（bank 渠道；证书轮换后需重新绑定） */
    @Column(name = "cert_serial", length = 64)
    private String certSerial;

    /** 是否已核验可用 */
    @Column(name = "verified")
    private Boolean verified;
}
