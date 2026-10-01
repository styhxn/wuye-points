package com.wuye.points.enums;

/**
 * 配送范围枚举（对应 delivery_rules.delivery_scope / delivery_price_ranges.delivery_scope）
 */
public enum DeliveryScope {
    IN_COMMUNITY("in_community", "小区内配送"),
    OUT_COMMUNITY("out_community", "小区外配送"),
    BOTH("both", "小区内外均配送");

    private final String code;
    private final String description;

    DeliveryScope(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (DeliveryScope scope : values()) {
            if (scope.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
