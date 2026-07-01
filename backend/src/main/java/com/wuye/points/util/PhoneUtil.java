package com.wuye.points.util;

/**
 * 手机号脱敏工具
 */
public final class PhoneUtil {

    private PhoneUtil() {
    }

    /**
     * 13812345678 -> 138****5678
     */
    public static String mask(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        int visiblePrefix = Math.min(3, phone.length() / 3);
        int visibleSuffix = Math.min(4, phone.length() / 3);
        return phone.substring(0, visiblePrefix)
                + "****"
                + phone.substring(phone.length() - visibleSuffix);
    }
}
