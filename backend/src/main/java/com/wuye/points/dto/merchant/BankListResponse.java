package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 开户银行列表响应（后端已拉全量并缓存，支持关键字检索与分页）。 */
@Schema(description = "开户银行列表响应")
@Data
@Builder
public class BankListResponse {

    @Schema(description = "账户类型", example = "BANK_ACCOUNT_TYPE_CORPORATE")
    private String bankAccountType;

    @Schema(description = "该类型下银行总条数（微信全量，约 4400+）", example = "4451")
    private Integer total;

    @Schema(description = "过滤后总条数（未传 keyword 时等于 total）", example = "12")
    private Integer filteredTotal;

    @Schema(description = "本次返回条数", example = "12")
    private Integer count;

    @Schema(description = "本次起始偏移", example = "0")
    private Integer offset;

    @Schema(description = "银行列表")
    private List<BankOptionResponse> list;
}
