package com.wuye.points.enums;

/**
 * 商家状态枚举
 */
public enum MerchantStatus {
    pending_audit("待审核"),
    approved("已通过"),
    rejected("已拒绝");

    private final String description;

    MerchantStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
