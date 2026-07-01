package com.wuye.points.enums;

/**
 * 消息类型枚举
 * 严格按照前后端对齐规范定义
 */
public enum MessageType {
    property_message("物业消息"),
    platform_message("统筹消息"),
    system_message("系统消息"),
    community_message("社区消息"),
    resident_message("业主消息（私聊）"),
    technician_message("技工消息（群发）");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
