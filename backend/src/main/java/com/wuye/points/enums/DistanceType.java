package com.wuye.points.enums;

/**
 * 距离类型枚举（对应 delivery_price_ranges.distance_type）
 */
public enum DistanceType {
    KM_1("1km", "1公里内"),
    KM_3("3km", "3公里内"),
    KM_5("5km", "5公里内"),
    CITYWIDE("citywide", "全城"),
    ANY("any", "不限距离");

    private final String code;
    private final String description;

    DistanceType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (DistanceType type : values()) {
            if (type.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
