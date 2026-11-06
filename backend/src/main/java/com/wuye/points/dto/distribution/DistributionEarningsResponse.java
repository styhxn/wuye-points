package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 「我的分账所得」响应。
 *
 * <p>分账所得并入各角色可提现余额、走同一条提现通道（老板 2026-11-06 定稿口径），
 * 因此 {@code walletIncluded=true}，各角色可提现数额见 {@code summary[].withdrawableAmount}。</p>
 */
@Schema(description = "我的分账所得（按角色汇总 + 明细分页）")
@Data
@Builder
public class DistributionEarningsResponse {

    @Schema(description = "分账所得是否已计入可提现余额。true=已在各角色余额中，"
            + "可在收益页直接申请提现（当前平台口径，恒为 true）；前端据此切换页面文案",
            example = "true")
    private Boolean walletIncluded;

    @Schema(description = "按角色汇总（未参与的角色不返回）")
    private List<DistributionEarningsSummaryItem> summary;

    @Schema(description = "明细分页（一订单+一角色一行，按分成落账时间倒序）")
    private List<DistributionEarningsDetailItem> list;

    @Schema(description = "当前页码", example = "1")
    private Integer page;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize;

    @Schema(description = "明细总条数", example = "57")
    private Long total;

    @Schema(description = "当前登录住户的物业公司ID（仅用于前端区分「本物业/历史」，服务端不用它过滤）",
            example = "pc_abc123")
    private String currentPropertyCompanyId;
}
