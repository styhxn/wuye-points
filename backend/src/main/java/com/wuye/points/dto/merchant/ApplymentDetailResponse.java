package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 商家进件状态/详情响应（商家端与运营后台共用；运营后台额外可见 retryCount/lastError）。
 */
@Schema(description = "商家微信进件状态/详情响应")
@Data
@Builder
public class ApplymentDetailResponse {

    @Schema(description = "商家ID")
    private String merchantId;

    @Schema(description = "微信支付申请单号（客服排查用）")
    private String applymentId;

    @Schema(description = "进件状态机：init/submitted/auditing/legal_validating/signing/finished/rejected")
    private String applymentState;

    @Schema(description = "微信侧状态原值（APPLYMENT_STATE_*）")
    private String wxState;

    @Schema(description = "主体类型：ENTERPRISE/INDIVIDUAL")
    private String organizationType;

    @Schema(description = "商户简称")
    private String merchantShortname;

    @Schema(description = "客服电话")
    private String servicePhone;

    @Schema(description = "所属行业（平台配置的入驻行业）")
    private String qualificationType;

    // ===== 以下为驳回后重填可回显的非敏感字段；证件号/银行卡号/邮箱等敏感项不回显 =====

    @Schema(description = "经营场景类型")
    private String salesScenesType;

    @Schema(description = "服务商小程序/公众号 AppID")
    private String salesAppId;

    @Schema(description = "商家小程序/公众号 AppID")
    private String salesSubAppId;

    @Schema(description = "场景截图 media_id 列表")
    private List<String> salesPics;

    @Schema(description = "门店名称")
    private String bizStoreName;

    @Schema(description = "门店省市编码")
    private String bizAddressCode;

    @Schema(description = "门店详细地址")
    private String bizStoreAddress;

    @Schema(description = "门头照片 media_id 列表")
    private List<String> storeEntrancePics;

    @Schema(description = "店内照片 media_id 列表")
    private List<String> storeIndoorPics;

    @Schema(description = "统一社会信用代码/注册号")
    private String businessLicenseNumber;

    @Schema(description = "营业执照商户名称")
    private String merchantName;

    @Schema(description = "法定代表人/经营者姓名")
    private String legalPerson;

    @Schema(description = "营业执照注册地址")
    private String licenseAddress;

    @Schema(description = "营业执照有效期开始")
    private String licensePeriodBegin;

    @Schema(description = "营业执照有效期结束")
    private String licensePeriodEnd;

    @Schema(description = "身份证有效期开始")
    private String idCardPeriodBegin;

    @Schema(description = "身份证有效期结束")
    private String idCardPeriodEnd;

    @Schema(description = "特殊资质图片 media_id 列表")
    private List<String> qualifications;

    @Schema(description = "联系人手机号（脱敏，重填时需重新输入完整号码）")
    private String contactMobile;

    @Schema(description = "超级管理员类型")
    private String contactType;

    @Schema(description = "超级管理员姓名")
    private String contactName;

    @Schema(description = "结算账户类型")
    private String bankAccountType;

    @Schema(description = "开户银行（银行列表 account_bank 值）")
    private String accountBank;

    @Schema(description = "银行别名编码")
    private String bankAliasCode;

    @Schema(description = "支行联行号")
    private String bankBranchId;

    @Schema(description = "开户银行全称")
    private String bankName;

    @Schema(description = "是否已提交微信（有 applyment_id）")
    private boolean submitted;

    @Schema(description = "超级管理员签约链接（待签约阶段返回，前端转二维码给超管扫）")
    private String signUrl;

    @Schema(description = "微信驳回原因")
    private String rejectReason;

    @Schema(description = "进件成功后微信分配的二级商户号")
    private String subMchid;

    @Schema(description = "提交方式：manual(运营后台)/auto(审核通过自动)")
    private String submitMode;

    @Schema(description = "提交失败错误信息（仅运营后台可见）")
    private String lastError;

    @Schema(description = "自动提交重试次数（仅运营后台可见）")
    private Integer retryCount;
}
