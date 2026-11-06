package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** 进件「开户银行」下拉项（微信银行列表的规范化字段，避免前端再解一层原始应答）。 */
@Schema(description = "开户银行选项")
@Data
@Builder
public class BankOptionResponse {

    @Schema(description = "开户银行名称（提交进件用这个）", example = "招商银行")
    private String accountBank;

    @Schema(description = "开户银行编码（提交进件用这个，微信要求数字）", example = "1001")
    private Integer accountBankCode;

    @Schema(description = "银行别名（仅展示/检索支行用）", example = "招商银行")
    private String bankAlias;

    @Schema(description = "银行别名编码（查支行时用，不能当结算银行编码提交）", example = "1000009561")
    private String bankAliasCode;

    @Schema(description = "是否必须选支行", example = "false")
    private Boolean needBankBranch;
}
