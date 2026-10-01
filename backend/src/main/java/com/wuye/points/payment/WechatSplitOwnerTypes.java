package com.wuye.points.payment;

/**
 * 微信分账收款方主体类型，对应 {@code wechat_split_receiver_accounts.owner_type}
 * 与 {@code wechat_split_record.owner_type}。
 *
 * <p>普通分账（profit sharing）的接收方只能是 MERCHANT_ID（商户号）或 PERSONAL_OPENID（个人 openid），
 * 不支持电商收付通的 sub_mchid 进件模式。本枚举用于标识业务侧「这笔分账最终落到哪个主体」。</p>
 */
public enum WechatSplitOwnerTypes {

    /** 住户 */
    RESIDENT,
    /** 商家 / 技工（作为商家型收益主体） */
    MERCHANT,
    /** 快递员 */
    COURIER,
    /** 物业公司 */
    PROPERTY,
    /** 社区协调员 */
    COORDINATOR,
    /** 区域负责人 */
    SECTOR_LEADER,
    /** 独立团长 */
    INDIVIDUAL_LEADER,
    /** 平台 */
    PLATFORM
}
