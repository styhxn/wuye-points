package com.wuye.points.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * API 时间格式化（本地时间 Asia/Shanghai）
 */
public final class DateTimeUtil {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
    }

    public static String toIsoUtc(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(FORMATTER);
    }

    public static String nowIsoUtc() {
        return LocalDateTime.now(ZONE_SHANGHAI).format(FORMATTER);
    }

    public static String toLocalDateTimeString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(FORMATTER);
    }
}
