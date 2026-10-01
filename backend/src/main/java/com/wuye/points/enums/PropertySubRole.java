package com.wuye.points.enums;

/**
 * 物业子角色枚举（存于 residents.property_sub_role）。
 * 主角色 UserRole / residents.role 固定为 property_admin。
 */
public enum PropertySubRole {
    property_leader("物业领导"),
    property_operator("物业管理员");

    private final String description;

    PropertySubRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
