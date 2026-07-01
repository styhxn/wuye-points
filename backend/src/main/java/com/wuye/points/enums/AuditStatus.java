package com.wuye.points.enums;

/**
 * 审核状态枚举
 * 严格按照前后端对齐规范定义
 */
public enum AuditStatus {
    pending_audit("待审核"),
    approved("已通过"),
    rejected("已拒绝");

    private final String description;

    AuditStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
