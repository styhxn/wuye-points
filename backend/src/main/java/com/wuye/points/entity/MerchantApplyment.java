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
 * <p>普通服务商「特约商户进件」：商家需在平台提交资料，由平台调
 * {@code POST /v3/applyment4sub/applyment/} 申请特约商户号（sub_mchid），
 * 审核通过后超管扫码签约，商家才能作为商城收款方并参与分账。</p>
 *
 * <p>状态机：init → submitted → auditing → legal_validating（待账户验证）
 * → signing（待签约）→ finished（回填 sub_mchid）/ rejected（可改资料重提）。</p>
 *
 * <p>证件姓名/号码/居住地址、开户名、银行卡号、联系邮箱、UBO 姓名/号码/地址
 * 以微信支付公钥/平台证书 RSA-OAEP 加密后落库（*_enc），对外绝不回显明文。
 * 超管姓名/手机号需用于展示与提交时重新加密，明文落库并在响应中脱敏。</p>
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

    /** 业务申请编号（每次重新提交必须更换，微信侧同编号视为同一申请单） */
    @Column(name = "business_code", length = 124)
    private String businessCode;

    /** 本地状态机：init/submitted/auditing/legal_validating/signing/finished/rejected */
    @Column(name = "applyment_state", length = 32, nullable = false)
    private String applymentState;

    /** 微信侧申请状态原值（APPLYMENT_STATE_*，调试与对账用） */
    @Column(name = "wx_state", length = 64)
    private String wxState;

    /** 主体类型：ENTERPRISE(企业)/INDIVIDUAL(个体工商户) */
    @Column(name = "organization_type", length = 32)
    private String organizationType;

    /** 是否金融机构主体（官方字段 finance_institution，默认 false） */
    @Column(name = "finance_institution")
    private Boolean financeInstitution;

    /** 商户简称（进件必填，对外展示） */
    @Column(name = "merchant_shortname", length = 64)
    private String merchantShortname;

    /** 客服电话（交易记录对买家展示） */
    @Column(name = "service_phone", length = 32)
    private String servicePhone;

    /** 营业执照上的商户名称 */
    @Column(name = "merchant_name", length = 128)
    private String merchantName;

    /** 统一社会信用代码/注册号 */
    @Column(name = "business_license_number", length = 64)
    private String businessLicenseNumber;

    /** 营业执照经营者/法定代表人姓名（公开信息，不加密） */
    @Column(name = "legal_person", length = 100)
    private String legalPerson;

    /** 营业执照注册地址 */
    @Column(name = "license_address", length = 512)
    private String licenseAddress;

    @Column(name = "license_period_begin", length = 32)
    private String licensePeriodBegin;

    @Column(name = "license_period_end", length = 32)
    private String licensePeriodEnd;

    /** 经营场景类型：SALES_SCENES_MINI_PROGRAM / SALES_SCENES_STORE / SALES_SCENES_MP 等 */
    @Column(name = "sales_scenes_type", length = 64)
    private String salesScenesType;

    /** 小程序/公众号 AppID（服务商侧） */
    @Column(name = "sales_app_id", length = 256)
    private String salesAppId;

    /** 小程序/公众号 AppID（商家侧） */
    @Column(name = "sales_sub_app_id", length = 256)
    private String salesSubAppId;

    /** 场景截图 media_id（逗号分隔） */
    @Column(name = "sales_pics", columnDefinition = "text")
    private String salesPics;

    @Column(name = "biz_store_name", length = 50)
    private String bizStoreName;

    /** 门店省市编码（官方省市对照表） */
    @Column(name = "biz_address_code", length = 32)
    private String bizAddressCode;

    @Column(name = "biz_store_address", length = 512)
    private String bizStoreAddress;

    /** 门头照片 media_id（逗号分隔） */
    @Column(name = "store_entrance_pics", columnDefinition = "text")
    private String storeEntrancePics;

    /** 店内照片 media_id（逗号分隔） */
    @Column(name = "store_indoor_pics", columnDefinition = "text")
    private String storeIndoorPics;

    /** 入驻结算规则ID（平台配置） */
    @Column(name = "settlement_id", length = 32)
    private String settlementId;

    /** 所属行业名称（平台配置） */
    @Column(name = "qualification_type", length = 200)
    private String qualificationType;

    /** 特殊资质图片 media_id（逗号分隔） */
    @Column(name = "qualifications", columnDefinition = "text")
    private String qualifications;

    @Column(name = "contact_name", length = 64)
    private String contactName;

    @Column(name = "contact_mobile", length = 32)
    private String contactMobile;

    /** 联系邮箱（RSA 加密） */
    @Column(name = "contact_email_enc", columnDefinition = "text")
    private String contactEmailEnc;

    /** 超管类型：当前版本仅 LEGAL(法人/经营者) */
    @Column(name = "contact_type", length = 32)
    private String contactType;

    /** 法人/经营者姓名（RSA 加密） */
    @Column(name = "id_card_name_enc", length = 512)
    private String idCardNameEnc;

    /** 身份证号（RSA 加密） */
    @Column(name = "id_card_number_enc", columnDefinition = "text")
    private String idCardNumberEnc;

    /** 身份证人像面 media_id（微信媒体上传后返回；实测可达 110+ 字符） */
    @Column(name = "id_card_copy_media", length = 255)
    private String idCardCopyMedia;

    /** 身份证国徽面 media_id */
    @Column(name = "id_card_national_media", length = 255)
    private String idCardNationalMedia;

    /** 身份证居住地址（企业主体必填；官方要求敏感信息加密，落库与上送均为密文） */
    @Column(name = "id_card_address_enc", columnDefinition = "text")
    private String idCardAddressEnc;

    @Column(name = "id_card_period_begin", length = 32)
    private String idCardPeriodBegin;

    @Column(name = "id_card_period_end", length = 32)
    private String idCardPeriodEnd;

    /** 最终受益人列表 JSON（姓名/号码/地址已加密） */
    @Column(name = "ubo_json", columnDefinition = "text")
    private String uboJson;

    /** 营业执照 media_id（小微可空） */
    @Column(name = "business_license_media", length = 255)
    private String businessLicenseMedia;

    /** 结算账户类型：BANK_ACCOUNT_TYPE_CORPORATE(对公)/BANK_ACCOUNT_TYPE_PERSONAL(个人)
     *  枚举值最长 27 字符，列宽不得小于 32（线上曾因 varchar(20) 导致保存 409） */
    @Column(name = "bank_account_type", length = 32)
    private String bankAccountType;

    /** 开户名（RSA 加密） */
    @Column(name = "bank_account_name_enc", length = 512)
    private String bankAccountNameEnc;

    /** 银行账号（RSA 加密） */
    @Column(name = "bank_account_number_enc", columnDefinition = "text")
    private String bankAccountNumberEnc;

    /** 开户银行全称（含支行） */
    @Column(name = "bank_name", length = 128)
    private String bankName;

    /** 开户银行（银行列表返回的 account_bank 值，进件字段） */
    @Column(name = "account_bank", length = 128)
    private String accountBank;

    /** 银行别名编码 bank_alias_code（仅用于查询支行列表） */
    @Column(name = "bank_alias_code", length = 64)
    private String bankAliasCode;

    /** 支行联行号 bank_branch_id */
    @Column(name = "bank_branch_id", length = 255)
    private String bankBranchId;

    /** 超级管理员签约链接（待签约阶段返回，转二维码给超管扫码） */
    @Column(name = "sign_url", columnDefinition = "text")
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
