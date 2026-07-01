package com.wuye.points.enums;

/**
 * 物业费状态枚举
 */
public enum FeeStatus {
    unpaid("未支付"),
    partial("部分支付"),
    paid("已支付"),
    overdue("逾期");

    private final String description;

    FeeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
