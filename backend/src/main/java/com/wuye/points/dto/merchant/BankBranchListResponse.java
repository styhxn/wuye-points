package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 支行列表响应（微信原始应答的规范化结果）。
 *
 * <p>微信的 {@code limit} 上限为 500，超出会被夹到 500；生效值通过 {@code limitEffective}
 * 字段回显，前端不再被静默截断。</p>
 */
@Schema(description = "支行列表响应")
@Data
@Builder
public class BankBranchListResponse {

    @Schema(description = "支行总条数", example = "37")
    private Integer total;

    @Schema(description = "本次返回条数", example = "37")
    private Integer count;

    @Schema(description = "本次起始偏移", example = "0")
    private Integer offset;

    @Schema(description = "后端实际生效的 limit（请求值超 500 时回显 500）", example = "500")
    private Integer limitEffective;

    @Schema(description = "所属开户银行名称", example = "招商银行")
    private String accountBank;

    @Schema(description = "所属开户银行编码", example = "1001")
    private Integer accountBankCode;

    @Schema(description = "银行别名", example = "招商银行")
    private String bankAlias;

    @Schema(description = "银行别名编码", example = "1000009561")
    private String bankAliasCode;

    @Schema(description = "支行列表")
    private List<Item> list;

    @Schema(description = "支行项")
    @Data
    @Builder
    public static class Item {

        @Schema(description = "支行名称", example = "招商银行股份有限公司北京分行")
        private String bankBranchName;

        @Schema(description = "支行联行号（提交进件用这个）", example = "308100005019")
        private String bankBranchId;
    }
}
