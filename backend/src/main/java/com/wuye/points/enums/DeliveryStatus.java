package com.wuye.points.enums;

/**
 * 配送状态枚举（与 schema / docs/10 对齐）
 */
public enum DeliveryStatus {
    pending("待抢单"),
    accepted("已接单"),
    delivering("配送中"),
    completed("已完成"),
    failed("配送失败"),
    property_fallback("物业兜底");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
