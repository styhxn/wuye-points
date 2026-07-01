package com.wuye.points.enums;

/**
 * 积分来源枚举
 */
public enum PointSource {
    order("订单"),
    activity("活动"),
    sign_in("签到"),
    manual("手动"),
    merchant_grant("商家录入"),
    fee_deduct("物业费抵扣"),
    pool_diff("积分池差额"),
    transfer("家庭转移");

    private final String description;

    PointSource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
