package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家微信进件资料提交请求（阶段1）。
 *
 * <p>敏感字段（身份证号、银行卡号、开户名）以明文提交，后端用微信平台证书
 * RSA-OAEP 加密后落库（*_enc），并仅在提交微信进件时使用，不对外回显。</p>
 */
@Schema(description = "商家微信进件资料提交请求")
@Data
public class ApplymentSubmitRequest {

    @Schema(description = "主体类型：ENTERPRISE(企业)/INDIVIDUAL(个体户)/MICRO(小微,无营业执照)", example = "INDIVIDUAL", required = true)
    @NotBlank(message = "主体类型不能为空")
    private String organizationType;

    @Schema(description = "商户简称（对外显示，进件必填）", example = "阳光水果店", required = true)
    @NotBlank(message = "商户简称不能为空")
    @Size(max = 64, message = "商户简称长度不能超过64")
    private String merchantShortname;

    @Schema(description = "经营类目（微信类目，联调对齐）", example = "生鲜水果")
    @Size(max = 64, message = "经营类目长度不能超过64")
    private String merchantCategory;

    @Schema(description = "联系人姓名", example = "张三", required = true)
    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 64, message = "联系人姓名长度不能超过64")
    private String contactName;

    @Schema(description = "联系人手机号", example = "13800138000", required = true)
    @NotBlank(message = "联系人手机号不能为空")
    @Size(max = 32, message = "联系人手机号长度不能超过32")
    private String contactMobile;

    @Schema(description = "联系人邮箱", example = "zhangsan@example.com")
    @Size(max = 128, message = "联系人邮箱长度不能超过128")
    private String contactEmail;

    @Schema(description = "法人/经营者姓名", example = "张三", required = true)
    @NotBlank(message = "法人/经营者姓名不能为空")
    @Size(max = 64, message = "法人/经营者姓名长度不能超过64")
    private String idCardName;

    @Schema(description = "身份证号", example = "110101199001011234", required = true)
    @NotBlank(message = "身份证号不能为空")
    @Size(max = 18, message = "身份证号长度不能超过18")
    private String idCardNumber;

    @Schema(description = "身份证人像面 media_id（先调媒体上传接口获取）", required = true)
    @NotBlank(message = "身份证人像面图片不能为空")
    private String idCardCopyMedia;

    @Schema(description = "身份证国徽面 media_id（先调媒体上传接口获取）", required = true)
    @NotBlank(message = "身份证国徽面图片不能为空")
    private String idCardNationalMedia;

    @Schema(description = "营业执照 media_id（小微商户可空）")
    private String businessLicenseMedia;

    @Schema(description = "结算账户类型：BANK_ACCOUNT_TYPE_CORPORATE(对公)/BANK_ACCOUNT_TYPE_PERSONAL(个人)", example = "BANK_ACCOUNT_TYPE_PERSONAL", required = true)
    @NotBlank(message = "结算账户类型不能为空")
    private String bankAccountType;

    @Schema(description = "开户名（对公填企业名称，个人填户主姓名）", example = "张三", required = true)
    @NotBlank(message = "开户名不能为空")
    @Size(max = 64, message = "开户名长度不能超过64")
    private String bankAccountName;

    @Schema(description = "银行账号", example = "6217000012345678901", required = true)
    @NotBlank(message = "银行账号不能为空")
    @Size(max = 32, message = "银行账号长度不能超过32")
    private String bankAccountNumber;

    @Schema(description = "开户银行全称（含支行）", example = "中国工商银行北京海淀支行", required = true)
    @NotBlank(message = "开户银行全称不能为空")
    @Size(max = 128, message = "开户银行全称长度不能超过128")
    private String bankName;
}
