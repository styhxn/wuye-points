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

    /**
     * 解析前端传入的时间字符串，支持 yyyy-MM-dd HH:mm:ss 与 ISO 本地时间两种格式；
     * 为空或无法解析时返回 null（调用方应回退为当前时间）。
     */
    public static LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, FORMATTER);
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
