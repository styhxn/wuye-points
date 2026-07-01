package com.wuye.points.enums;

/**
 * 支付方式枚举
 */
public enum PaymentMethod {
    point("积分"),
    coin("物业币"),
    wechat("微信"),
    mixed("混合");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
