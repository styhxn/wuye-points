package com.wuye.points.enums;

/**
 * 商家积分购买审核状态枚举
 */
public enum PurchaseStatus {
    pending_audit("待审核"),
    approved("已通过"),
    rejected("已拒绝");

    private final String description;

    PurchaseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
