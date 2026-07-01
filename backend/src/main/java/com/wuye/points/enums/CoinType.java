package com.wuye.points.enums;

/**
 * 物业币类型枚举
 */
public enum CoinType {
    earn("获得"),
    spend("消费"),
    recharge("充值"),
    freeze("冻结"),
    unfreeze("解冻"),
    expire("过期"),
    withdraw("提现");

    private final String description;

    CoinType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
