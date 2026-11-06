package com.wuye.points.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * API 时间格式化（本地时间 Asia/Shanghai）
 */
public final class DateTimeUtil {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
     *
     * <p>刻意保持原有的宽松度：优惠券有效期、公告定时发布时间都按「解析不出来就当没传」处理，
     * 放宽这里会静默改变这两条链路的语义。需要兼容更多写法的新接口请显式调用
     * {@link #parseApiDateTime(String, boolean)}。</p>
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

    /**
     * 解析请求体/查询参数里的「时间点」，兼容各端实际会发的写法（整改清单条目26/29）。
     *
     * <p>支持：{@code yyyy-MM-dd HH:mm[:ss]}、{@code yyyy-MM-ddTHH:mm[:ss][.SSS]}、
     * 带 {@code Z} 或 {@code ±HH:mm} 偏移的 ISO 串（按 Asia/Shanghai 换算，不做裸 UTC 截断）、
     * 以及纯日期 {@code yyyy-MM-dd}。
     *
     * @param endOfDayForDateOnly 纯日期时取 23:59:59（用于「截止/到期」类字段），false 取 00:00
     * @return 无法解析返回 null，由调用方决定报错文案
     */
    public static LocalDateTime parseApiDateTime(String value, boolean endOfDayForDateOnly) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();

        try {
            return LocalDateTime.parse(text, FORMATTER);
        } catch (Exception ignored) {
            // 继续尝试其余格式
        }
        try {
            return LocalDateTime.parse(text, MINUTE_FORMATTER);
        } catch (Exception ignored) {
            // 继续尝试其余格式
        }
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignored) {
            // 继续尝试带偏移的写法
        }
        // datetime-local + new Date().toISOString() 会发出 2026-11-10T06:30:00.000Z
        try {
            return OffsetDateTime.parse(text).atZoneSameInstant(ZONE_SHANGHAI).toLocalDateTime();
        } catch (Exception ignored) {
            // 继续尝试 ZonedDateTime
        }
        try {
            return ZonedDateTime.parse(text).withZoneSameInstant(ZONE_SHANGHAI).toLocalDateTime();
        } catch (Exception ignored) {
            // 继续尝试纯日期
        }
        try {
            LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            return endOfDayForDateOnly ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (Exception ignored) {
            return null;
        }
    }
}
