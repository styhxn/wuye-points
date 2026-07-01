package com.wuye.points.enums;

public enum MerchantLevel {
    official_certified("官方认证"),
    property_certified("物业认证"),
    normal("普通");

    private final String description;

    MerchantLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
