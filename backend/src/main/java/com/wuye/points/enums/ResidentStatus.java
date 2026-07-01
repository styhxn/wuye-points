package com.wuye.points.enums;

/**
 * 住户状态枚举
 */
public enum ResidentStatus {
    active("正常"),
    frozen("冻结"),
    disabled("禁用");

    private final String description;

    ResidentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
