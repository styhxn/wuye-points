package com.wuye.points.enums;

/**
 * 积分池记录类型枚举
 */
public enum PoolRecordType {
    diff("兑换差额"),
    expiry_clear("过期清零"),
    fee_clear("物业费清零"),
    manual_adjust("手动调整"),
    manual_out("手动流出");

    private final String description;

    PoolRecordType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
