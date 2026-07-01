package com.wuye.points.enums;

/**
 * 提现状态枚举
 */
public enum WithdrawalStatus {
    pending_audit("待审核"),
    approved("已通过"),
    rejected("已拒绝"),
    completed("已完成"),
    failed("失败");

    private final String description;

    WithdrawalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
