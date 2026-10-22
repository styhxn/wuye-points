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
    /** 单元楼长（分成从物业公司分成中定向扣除，落库为扣经理抽成后的净额） */
    BUILDING_LEADER,
    /** 物业经理（抽成从楼长分成毛额中抽取；ownerId 为经理的 residentId，与钱包流水的主体键一致） */
    PROPERTY_MANAGER,
    /** 平台 */
    PLATFORM
}
