package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家微信进件申请单（merchant_applyments）。
 *
 * <p>电商收付通（平台收付通）模式下，商家需在微信服务商平台进件为「二级商户」方可在线收款。
 * 本表承载进件全流程：资料收集（阶段 init）→ 提交微信（submitted）→ 审核（auditing）
 * → 法人验证（legal_validating）→ 签约（signing）→ 完成（finished，回填 sub_mchid）/
 * 驳回（rejected，可改资料重提）。</p>
 *
 * <p>敏感字段（身份证号、银行卡号、开户名）一律以微信平台证书 RSA-OAEP 加密后落库
 * （*_enc），对外绝不回显明文。</p>
 */
@Data
@Entity
@Table(name = "merchant_applyments")
@EqualsAndHashCode(callSuper = true)
public class MerchantApplyment extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    /** 微信进件申请单号（applyments 接口返回） */
    @Column(name = "applyment_id", length = 64)
    private String applymentId;

    /** 本地状态机：init/submitted/auditing/legal_validating/signing/finished/rejected */
    @Column(name = "applyment_state", length = 32, nullable = false)
    private String applymentState;

    /** 微信侧申请状态原值（FINISH/AUDITING/REJECTED 等，调试与对账用） */
    @Column(name = "wx_state", length = 64)
    private String wxState;

    /** 主体类型：ENTERPRISE(企业)/INDIVIDUAL(个体户)/MICRO(小微) */
    @Column(name = "organization_type", length = 20)
    private String organizationType;

    /** 商户简称（进件必填，对外展示） */
    @Column(name = "merchant_shortname", length = 64)
    private String merchantShortname;

    /** 经营类目（联调时对齐微信类目表，占位） */
    @Column(name = "merchant_category", length = 64)
    private String merchantCategory;

    @Column(name = "contact_name", length = 64)
    private String contactName;

    @Column(name = "contact_mobile", length = 32)
    private String contactMobile;

    @Column(name = "contact_email", length = 128)
    private String contactEmail;

    /** 法人/经营者姓名（RSA 加密） */
    @Column(name = "id_card_name_enc", length = 512)
    private String idCardNameEnc;

    /** 身份证号（RSA 加密） */
    @Column(name = "id_card_number_enc", length = 1024)
    private String idCardNumberEnc;

    /** 身份证人像面 media_id（微信媒体上传后返回） */
    @Column(name = "id_card_copy_media", length = 128)
    private String idCardCopyMedia;

    /** 身份证国徽面 media_id */
    @Column(name = "id_card_national_media", length = 128)
    private String idCardNationalMedia;

    /** 营业执照 media_id（小微可空） */
    @Column(name = "business_license_media", length = 128)
    private String businessLicenseMedia;

    /** 结算账户类型：BANK_ACCOUNT_TYPE_CORPORATE(对公)/BANK_ACCOUNT_TYPE_PERSONAL(个人) */
    @Column(name = "bank_account_type", length = 20)
    private String bankAccountType;

    /** 开户名（RSA 加密） */
    @Column(name = "bank_account_name_enc", length = 512)
    private String bankAccountNameEnc;

    /** 银行账号（RSA 加密） */
    @Column(name = "bank_account_number_enc", length = 1024)
    private String bankAccountNumberEnc;

    /** 开户银行全称（含支行） */
    @Column(name = "bank_name", length = 128)
    private String bankName;

    /** 法人验证链接（审核通过后返回，有时效） */
    @Column(name = "legal_validation_url", length = 1024)
    private String legalValidationUrl;

    /** 签约链接（法人验证通过后返回） */
    @Column(name = "sign_url", length = 1024)
    private String signUrl;

    /** 微信驳回原因 */
    @Column(name = "reject_reason", length = 512)
    private String rejectReason;

    /** 进件成功后微信分配的二级商户号（回填 merchant_wechat_accounts） */
    @Column(name = "sub_mchid", length = 32)
    private String subMchid;

    /** 提交方式：manual(运营后台)/auto(审核通过自动) */
    @Column(name = "submit_mode", length = 16, nullable = false)
    private String submitMode;

    /** 状态推进重试次数 */
    @Column(name = "retry_count")
    private Integer retryCount;

    /** 最近一次查询/提交错误 */
    @Column(name = "last_error", length = 500)
    private String lastError;
}
