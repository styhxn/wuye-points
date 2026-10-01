package com.wuye.points.dto.admin.platformshare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 平台收益余额 / 对账快照（只读）。
 *
 * <p>微信 真分账模式下，平台抽成（订单分成 + 配送费分成 + 提现手续费分成）由微信
 * 直接划入平台 微信 账户、实时到账，<b>不存在内部待提钱包、不提供提现申请</b>。
 * 本响应仅用于前端看账与对照流水，字段语义如下：
 * <ul>
 *   <li>{@code totalEarned} 累计收益（全部构成之和，已实时入账平台 微信 账户）</li>
 *   <li>{@code settledBalance} 平台 微信 账户已入账余额（= 累计收益）</li>
 *   <li>{@code pendingAmount} 处理中金额（售后/结算在途，真分账直入账模式下通常为 0）</li>
 *   <li>{@code withdrawAvailable} 是否支持发起提现（微信 真分账下为 false，前端据此隐藏「申请提现」按钮）</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformEarningsBalanceResponse {

    /** 累计平台收益（订单分成 + 配送费 + 提现手续费），已实时入账 微信 平台账户 */
    private BigDecimal totalEarned;

    /** 平台 微信 账户已入账余额（微信 真分账模式无内部待提钱包，= 累计收益） */
    private BigDecimal settledBalance;

    /** 处理中金额（售后窗口 / 结算在途，微信 直入账模式下通常为 0） */
    private BigDecimal pendingAmount;

    /** 结算 / 入账方式：wechat_profit_sharing */
    private String settleMode;

    /** 结算状态文案（展示用） */
    private String settlementStatus;

    /** 是否支持发起提现（微信 真分账模式下为 false，前端据此隐藏「申请提现」按钮） */
    private Boolean withdrawAvailable;

    /** 构成：订单分成 */
    private BigDecimal distributionShare;

    /** 构成：配送费分成 */
    private BigDecimal deliveryShare;

    /** 构成：提现手续费分成 */
    private BigDecimal withdrawalFeeShare;
}
