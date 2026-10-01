package com.wuye.points.enums;

/**
 * 物业币使用条件枚举（三选一）
 */
public enum CoinUseCondition {
    NONE("none", "无限制"),
    POINT_THRESHOLD("point_threshold", "积分门槛"),
    PROPERTY_FEE_PAID("property_fee_paid", "物业费已缴清"),
    MANUAL_ISSUE("manual_issue", "手动配发");

    private final String code;
    private final String description;

    CoinUseCondition(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CoinUseCondition fromCode(String code) {
        if (code == null) {
            return NONE;
        }
        for (CoinUseCondition condition : values()) {
            if (condition.code.equals(code)) {
                return condition;
            }
        }
        return NONE;
    }
}
