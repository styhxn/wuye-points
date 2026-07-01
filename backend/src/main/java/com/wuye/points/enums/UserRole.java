package com.wuye.points.enums;

/**
 * 用户角色枚举
 * 严格按照前后端对齐规范定义
 */
public enum UserRole {
    resident("业主/住户"),
    property_admin("物业管理员"),
    platform_admin("平台管理员"),
    merchant("商家"),
    courier("快递员"),
    activity_leader("活动组组长"),
    technician("技工");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
