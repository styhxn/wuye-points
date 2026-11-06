package com.wuye.points.dto.distribution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 「我的分账所得」按角色的汇总项。
 *
 * <p>{@code payableAmount} 为应付分成（distribution_records 台账口径，订单完成即产生）；
 * {@code receivedAmount} 为已到账（wechat_split_record 已真实结算到本人微信的状态集合）。
 * 两者是两列，不做相减，由前端按需展示。</p>
 *
 * <p>{@code withdrawableAmount} 为可直接申请提现的余额，与提现接口校验同源；
 * 分账所得按「并入各角色可提现余额、走提现通道」的口径落地，见响应顶层 {@code walletIncluded}。</p>
 */
@Schema(description = "我的分账所得-角色汇总")
@Data
@Builder
public class DistributionEarningsSummaryItem {

    @Schema(description = "角色：COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER/BUILDING_LEADER/PROPERTY_MANAGER",
            example = "COORDINATOR")
    private String role;

    @Schema(description = "角色中文名", example = "统筹负责人")
    private String roleName;

    @Schema(description = "应付分成合计（元）", example = "120.00")
    private BigDecimal payableAmount;

    @Schema(description = "已到账合计（元）", example = "100.00")
    private BigDecimal receivedAmount;

    @Schema(description = "该角色当前有效身份已入账到可提现余额的累计金额（元，含退款负向冲正，不扣提现）。"
            + "与 receivedAmount 口径不同：前者是内部账面入账，后者是微信分账真实到账（分账未启用时恒为 0）。",
            example = "120.00")
    private BigDecimal walletCreditedAmount;

    @Schema(description = "该角色当前可提现余额（元）：与提现申请校验用的同一个函数、同一个数。"
            + "楼长返回的是账号统一收益账户余额（含业主卖货与名下商家），不是分账所得单列。",
            example = "100.00")
    private BigDecimal withdrawableAmount;

    @Schema(description = "在途提现占用（待审核 + 已通过待打款，元）", example = "20.00")
    private BigDecimal pendingWithdrawalAmount;

    @Schema(description = "该身份提现手续费率（物业公司覆盖值优先，未配置取平台默认，手续费开关关闭时为 0）",
            example = "0.0060")
    private BigDecimal feeRate;

    @Schema(description = "该角色是否可在小程序申请提现：false 表示身份已停用或不属于当前物业，"
            + "账面分成仍在但无提现入口，前端应隐藏提现按钮", example = "true")
    private Boolean withdrawEnabled;

    @Schema(description = "是否为历史身份（身份已停用或不属于当前物业）；true 时前端应打「历史」标记",
            example = "false")
    private Boolean isHistorical;

    @Schema(description = "身份状态：active(当前有效)/historical(历史)", example = "active")
    private String identityStatus;

    @Schema(description = "参与统计的身份记录数（同一角色可多条，如跨板块）", example = "1")
    private Integer identityCount;

    @Schema(description = "涉及物业公司ID列表（仅展示，不作为查询过滤条件）", example = "[\"pc_abc123\"]")
    private List<String> propertyCompanyIds;

    @Schema(description = "涉及物业公司名称列表（查不到名称时为空）", example = "[\"万科物业\"]")
    private List<String> propertyNames;
}
