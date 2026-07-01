package com.wuye.points.enums;

/**
 * 公告类型枚举
 * 严格按照前后端对齐规范定义
 */
public enum AnnouncementType {
    property_announcement("物业公告"),
    community_announcement("社区公告"),
    platform_announcement("统筹/官方推荐公告"),
    system_announcement("系统公告"),
    merchant_announcement("商家公告"),
    activity_announcement("活动组公告");

    private final String description;

    AnnouncementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
