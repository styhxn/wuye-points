package com.wuye.points.enums;

/**
 * 用户类型枚举
 */
public enum UserType {
    owner("业主"),
    tenant("租户");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
