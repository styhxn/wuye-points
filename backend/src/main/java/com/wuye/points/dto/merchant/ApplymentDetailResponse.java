package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 商家进件状态/详情响应（商家端与运营后台共用；运营后台额外可见 retryCount/lastError）。
 */
@Schema(description = "商家微信进件状态/详情响应")
@Data
@Builder
public class ApplymentDetailResponse {

    @Schema(description = "商家ID")
    private String merchantId;

    @Schema(description = "进件状态机：init/submitted/auditing/legal_validating/signing/finished/rejected")
    private String applymentState;

    @Schema(description = "微信侧状态原值")
    private String wxState;

    @Schema(description = "主体类型：ENTERPRISE/INDIVIDUAL/MICRO")
    private String organizationType;

    @Schema(description = "商户简称")
    private String merchantShortname;

    @Schema(description = "经营类目")
    private String merchantCategory;

    @Schema(description = "联系人手机号（脱敏）")
    private String contactMobile;

    @Schema(description = "结算账户类型")
    private String bankAccountType;

    @Schema(description = "开户银行全称")
    private String bankName;

    @Schema(description = "是否已提交微信（有 applyment_id）")
    private boolean submitted;

    @Schema(description = "法人验证链接（有时效，仅 legal_validating 阶段返回）")
    private String legalValidationUrl;

    @Schema(description = "签约链接（有时效，仅 signing 阶段返回）")
    private String signUrl;

    @Schema(description = "微信驳回原因")
    private String rejectReason;

    @Schema(description = "进件成功后微信分配的二级商户号")
    private String subMchid;

    @Schema(description = "提交方式：manual(运营后台)/auto(审核通过自动)")
    private String submitMode;

    @Schema(description = "提交失败错误信息（仅运营后台可见）")
    private String lastError;
}
