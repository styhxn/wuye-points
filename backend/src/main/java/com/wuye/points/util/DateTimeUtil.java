package com.wuye.points.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * API 时间格式化（ISO 8601 UTC）
 */
public final class DateTimeUtil {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");

    private DateTimeUtil() {
    }

    public static String toIsoUtc(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZONE_SHANGHAI).toInstant().toString();
    }

    public static String nowIsoUtc() {
        return Instant.now().toString();
    }
}
