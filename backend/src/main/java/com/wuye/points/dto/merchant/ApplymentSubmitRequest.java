package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 商家微信进件资料（普通服务商「特约商户进件」）。
 *
 * <p>字段与官方《提交申请单》{@code POST /v3/applyment4sub/applyment/} 一一对应。
 * 敏感字段（身份证姓名/号码/居住地址、开户名、银行账号、联系邮箱、UBO 姓名/号码/地址）
 * 以明文提交，后端通过微信支付公钥 RSA-OAEP 加密后落库/上送；响应不回显这些明文。</p>
 *
 * <p>图片字段均需先调用媒体上传接口换取 {@code media_id}（有效期 3 天）。</p>
 */
@Schema(description = "商家微信进件资料提交请求（服务商特约商户进件）")
@Data
public class ApplymentSubmitRequest {

    // ===== 主体 =====

    @Schema(description = "主体类型：ENTERPRISE(企业)/INDIVIDUAL(个体工商户)", example = "ENTERPRISE", required = true)
    @NotBlank(message = "主体类型不能为空")
    private String organizationType;

    @Schema(description = "商户简称（支付完成页展示，需与行业相关）", example = "阳光物业", required = true)
    @NotBlank(message = "商户简称不能为空")
    @Size(max = 64, message = "商户简称长度不能超过64")
    private String merchantShortname;

    @Schema(description = "客服电话（交易记录对买家展示）", example = "0427-8888888", required = true)
    @NotBlank(message = "客服电话不能为空")
    @Size(max = 32, message = "客服电话长度不能超过32")
    private String servicePhone;

    // ===== 营业执照 =====

    @Schema(description = "营业执照 media_id", required = true)
    @NotBlank(message = "营业执照图片不能为空")
    private String businessLicenseMedia;

    @Schema(description = "统一社会信用代码/注册号", example = "91211100MA0XXXXXXX", required = true)
    @NotBlank(message = "统一社会信用代码不能为空")
    @Size(max = 64, message = "统一社会信用代码长度不能超过64")
    private String businessLicenseNumber;

    @Schema(description = "营业执照上的商户名称", example = "盘锦XX物业管理有限公司", required = true)
    @NotBlank(message = "营业执照商户名称不能为空")
    @Size(max = 128, message = "商户名称长度不能超过128")
    private String merchantName;

    @Schema(description = "经营者/法定代表人姓名（营业执照）", example = "张三", required = true)
    @NotBlank(message = "经营者/法定代表人姓名不能为空")
    @Size(max = 100, message = "法定代表人姓名长度不能超过100")
    private String legalPerson;

    @Schema(description = "注册地址（建议填写，缺失时微信会查工商信息）", example = "辽宁省盘锦市兴隆台区XX路1号")
    @Size(max = 512, message = "注册地址长度不能超过512")
    private String licenseAddress;

    @Schema(description = "营业执照有效期开始（YYYY-MM-DD，可选）", example = "2020-01-01")
    private String licensePeriodBegin;

    @Schema(description = "营业执照有效期结束（YYYY-MM-DD 或“长期”，可选）", example = "长期")
    private String licensePeriodEnd;

    // ===== 法人/经营者证件 =====

    @Schema(description = "法人/经营者姓名（证件）", example = "张三", required = true)
    @NotBlank(message = "法人姓名不能为空")
    @Size(max = 64, message = "法人姓名长度不能超过64")
    private String idCardName;

    @Schema(description = "身份证号", example = "210101199001011234", required = true)
    @NotBlank(message = "身份证号不能为空")
    @Size(max = 18, message = "身份证号长度不能超过18")
    private String idCardNumber;

    @Schema(description = "身份证人像面 media_id", required = true)
    @NotBlank(message = "身份证人像面图片不能为空")
    private String idCardCopyMedia;

    @Schema(description = "身份证国徽面 media_id", required = true)
    @NotBlank(message = "身份证国徽面图片不能为空")
    private String idCardNationalMedia;

    @Schema(description = "身份证有效期开始（YYYY-MM-DD）", example = "2015-12-28", required = true)
    @NotBlank(message = "身份证有效期开始不能为空")
    private String idCardPeriodBegin;

    @Schema(description = "身份证有效期结束（YYYY-MM-DD 或“长期”）", example = "2035-12-28", required = true)
    @NotBlank(message = "身份证有效期结束不能为空")
    private String idCardPeriodEnd;

    @Schema(description = "身份证居住地址（企业主体必填）", example = "辽宁省盘锦市兴隆台区XX小区1-1-101")
    @Size(max = 512, message = "身份证居住地址长度不能超过512")
    private String idCardAddress;

    // ===== 最终受益人（企业必填） =====

    @Schema(description = "最终受益人列表（企业有多名受益人时必填；仅法人一名时可留空由微信自动回填）")
    private List<UboInfoRequest> uboList;

    // ===== 经营场景 =====

    @Schema(description = "经营场景类型：SALES_SCENES_MINI_PROGRAM(小程序)/SALES_SCENES_STORE(线下门店)/SALES_SCENES_MP(公众号)",
            example = "SALES_SCENES_MINI_PROGRAM", required = true)
    @NotBlank(message = "经营场景类型不能为空")
    private String salesScenesType;

    @Schema(description = "服务商小程序/公众号 AppID；小程序场景留空时后端默认使用平台小程序 AppID",
            example = "wxf7230afd38275659")
    private String salesAppId;

    @Schema(description = "商家小程序/公众号 AppID（与 salesAppId 二选一）")
    private String salesSubAppId;

    @Schema(description = "小程序/公众号页面截图 media_id 列表")
    private List<String> salesPics;

    @Schema(description = "线下门店名称（SALES_SCENES_STORE 必填）")
    @Size(max = 50, message = "门店名称长度不能超过50")
    private String bizStoreName;

    @Schema(description = "门店所在省市编码（SALES_SCENES_STORE 必填，见官方省市对照表）")
    private String bizAddressCode;

    @Schema(description = "门店详细地址（SALES_SCENES_STORE 必填）")
    @Size(max = 512, message = "门店地址长度不能超过512")
    private String bizStoreAddress;

    @Schema(description = "门店门头照片 media_id 列表（SALES_SCENES_STORE 必填）")
    private List<String> storeEntrancePics;

    @Schema(description = "门店内部照片 media_id 列表（SALES_SCENES_STORE 必填）")
    private List<String> storeIndoorPics;

    // ===== 结算规则（费率由平台控制） =====

    @Schema(description = "所属行业名称（留空默认「居民生活服务」；可选值以 /industries 接口为准）")
    private String qualificationType;

    @Schema(description = "特殊资质图片 media_id 列表（行业要求时填写）")
    private List<String> qualifications;

    // ===== 结算账户 =====

    @Schema(description = "账户类型：BANK_ACCOUNT_TYPE_CORPORATE(对公)/BANK_ACCOUNT_TYPE_PERSONAL(经营者个人卡)",
            example = "BANK_ACCOUNT_TYPE_CORPORATE", required = true)
    @NotBlank(message = "结算账户类型不能为空")
    private String bankAccountType;

    @Schema(description = "开户名称（对公填企业名称，个人卡须与法人姓名一致）", example = "盘锦XX物业管理有限公司", required = true)
    @NotBlank(message = "开户名不能为空")
    @Size(max = 64, message = "开户名长度不能超过64")
    private String bankAccountName;

    @Schema(description = "开户银行（银行列表接口返回的 account_bank 值，进件字段）", example = "招商银行", required = true)
    @NotBlank(message = "开户银行不能为空")
    @Size(max = 128, message = "开户银行长度不能超过128")
    private String accountBank;

    @Schema(description = "银行别名编码 bank_alias_code（仅用于查询支行列表）", example = "1000009561")
    private String bankAliasCode;

    @Schema(description = "支行联行号 bank_branch_id（银行要求填写支行时必填）")
    private String bankBranchId;

    @Schema(description = "开户银行全称（含支行，与联行号二选一）")
    @Size(max = 128, message = "开户银行全称长度不能超过128")
    private String bankName;

    @Schema(description = "银行账号", example = "6217000012345678901", required = true)
    @NotBlank(message = "银行账号不能为空")
    @Size(max = 32, message = "银行账号长度不能超过32")
    private String bankAccountNumber;

    // ===== 超级管理员 =====

    @Schema(description = "超管类型：当前版本仅支持 LEGAL（法定代表人本人作为超级管理员）",
            example = "LEGAL", required = true)
    @NotBlank(message = "超级管理员类型不能为空")
    private String contactType;

    @Schema(description = "超级管理员姓名（须与法人姓名一致）", example = "张三", required = true)
    @NotBlank(message = "超级管理员姓名不能为空")
    @Size(max = 64, message = "超级管理员姓名长度不能超过64")
    private String contactName;

    @Schema(description = "超级管理员手机号（接收验证码与重要通知，须为法人实名手机号）", example = "13800138000", required = true)
    @NotBlank(message = "超级管理员手机号不能为空")
    @Size(max = 32, message = "手机号长度不能超过32")
    private String contactMobile;

    @Schema(description = "联系邮箱（接收开户邮件）", example = "wu@example.com", required = true)
    @NotBlank(message = "联系邮箱不能为空")
    @Size(max = 128, message = "邮箱长度不能超过128")
    private String contactEmail;
}
