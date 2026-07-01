package com.wuye.points.enums;

/**
 * 积分类型枚举
 */
public enum PointType {
    earn("获得"),
    spend("消费"),
    transfer("转移"),
    expire("过期"),
    pool_in("进入积分池");

    private final String description;

    PointType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
