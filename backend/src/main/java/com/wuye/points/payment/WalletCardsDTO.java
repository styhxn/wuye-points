package com.wuye.points.payment;

import java.math.BigDecimal;

/**
 * 钱包「两张卡」视图（HTML ②-5）：统一资产结构，把同一主体的资金拆成两类展示。
 *
 * <ul>
 *   <li>卡 A（legacy 钱包余额）：平台内部钱包可提现余额（{@code wallet_flows.balance_type='withdrawable'} 汇总），含历史商家/配送等收益。</li>
 *   <li>卡 B（微信 分账冻结）：经微信 微信 冻结/完结/提现中的资金（{@code wechat_split_record.freeze_amount} 汇总）。</li>
 * </ul>
 *
 * 该对象为只读聚合，不写库，可安全对接前端钱包页与技工/商家工作台。
 */
public class WalletCardsDTO {

    /** 卡 A：钱包可提现余额（元） */
    private BigDecimal legacyWithdrawableAmount;

    /** 卡 B：微信 冻结/完结中金额（元，未提现） */
    private BigDecimal freezingAmount;

    /** 卡 B：微信 已完结/已提现金额（元，已落袋） */
    private BigDecimal settledAmount;

    /** 是否已在微信开户（有 微信 分账账户） */
    private boolean hasWechatSplitReceiverAccount;

    public WalletCardsDTO() {
    }

    public WalletCardsDTO(BigDecimal legacyWithdrawableAmount, BigDecimal freezingAmount,
                          BigDecimal settledAmount, boolean hasWechatSplitReceiverAccount) {
        this.legacyWithdrawableAmount = legacyWithdrawableAmount;
        this.freezingAmount = freezingAmount;
        this.settledAmount = settledAmount;
        this.hasWechatSplitReceiverAccount = hasWechatSplitReceiverAccount;
    }

    public BigDecimal getLegacyWithdrawableAmount() {
        return legacyWithdrawableAmount;
    }

    public void setLegacyWithdrawableAmount(BigDecimal legacyWithdrawableAmount) {
        this.legacyWithdrawableAmount = legacyWithdrawableAmount;
    }

    public BigDecimal getFreezingAmount() {
        return freezingAmount;
    }

    public void setFreezingAmount(BigDecimal freezingAmount) {
        this.freezingAmount = freezingAmount;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(BigDecimal settledAmount) {
        this.settledAmount = settledAmount;
    }

    public boolean isHasWechatSplitReceiverAccount() {
        return hasWechatSplitReceiverAccount;
    }

    public void setHasWechatSplitReceiverAccount(boolean hasWechatSplitReceiverAccount) {
        this.hasWechatSplitReceiverAccount = hasWechatSplitReceiverAccount;
    }
}
