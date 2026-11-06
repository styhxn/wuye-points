package com.wuye.points.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 整改清单条目26（禁言 expiresAt）与条目29（预约 serviceTime）的时间解析回归。
 *
 * <p>两端实际发出来的写法比后端原来接受的更杂：Web 管理端 datetime-local 经
 * {@code new Date().toISOString()} 发出 {@code 2026-11-10T06:30:00.000Z}（UTC），
 * 小程序发不带偏移的本地 ISO {@code 2026-11-05T14:00:00}。二者都要能吃下，
 * 且带 Z 的必须按 Asia/Shanghai 换算，不能像裸 UTC 截断那样偏 8 小时。</p>
 */
@DisplayName("API 时间参数解析（条目26/29）")
class DateTimeUtilApiParsingTest {

    @Test
    @DisplayName("带毫秒与 Z 的 UTC 串换算成上海本地时间")
    void parsesUtcMillisIntoShanghaiLocal() {
        assertEquals(LocalDateTime.of(2026, 11, 10, 14, 30, 0),
                DateTimeUtil.parseApiDateTime("2026-11-10T06:30:00.000Z", false));
    }

    @Test
    @DisplayName("不带偏移的 ISO 本地时间按本地时间理解（小程序预约口径）")
    void parsesLocalIsoWithoutOffset() {
        assertEquals(LocalDateTime.of(2026, 11, 5, 14, 0, 0),
                DateTimeUtil.parseApiDateTime("2026-11-05T14:00:00", false));
        assertEquals(LocalDateTime.of(2026, 11, 5, 14, 0, 0),
                DateTimeUtil.parseApiDateTime("2026-11-05T14:00", false));
    }

    @Test
    @DisplayName("空格分隔的本地时间仍支持")
    void parsesSpaceSeparatedDateTime() {
        assertEquals(LocalDateTime.of(2026, 11, 5, 9, 5, 0),
                DateTimeUtil.parseApiDateTime("2026-11-05 09:05:00", false));
    }

    @Test
    @DisplayName("显式偏移量换算成上海本地时间")
    void parsesOffsetDateTime() {
        assertEquals(LocalDateTime.of(2026, 11, 10, 14, 30, 0),
                DateTimeUtil.parseApiDateTime("2026-11-10T14:30:00+08:00", false));
    }

    @Test
    @DisplayName("纯日期：截止时间取当天 23:59:59，起始口径取 00:00")
    void parsesDateOnlyWithEndOfDayFlag() {
        assertEquals(LocalDateTime.of(2026, 11, 10, 23, 59, 59),
                DateTimeUtil.parseApiDateTime("2026-11-10", true));
        assertEquals(LocalDateTime.of(2026, 11, 10, 0, 0, 0),
                DateTimeUtil.parseApiDateTime("2026-11-10", false));
    }

    @Test
    @DisplayName("无法识别的写法返回 null，由调用方给出可自助修复的报错")
    void rejectsGarbage() {
        assertNull(DateTimeUtil.parseApiDateTime("10/11/2026 14:30", true));
        assertNull(DateTimeUtil.parseApiDateTime("   ", true));
        assertNull(DateTimeUtil.parseApiDateTime(null, true));
    }

    @Test
    @DisplayName("parseLocalDateTime 的旧口径保持不变（优惠券/公告定时发布依赖「解析不出即为空」）")
    void legacyParserUnchanged() {
        assertNull(DateTimeUtil.parseLocalDateTime("2026-11-10"), "纯日期不应被旧方法接受");
        assertNull(DateTimeUtil.parseLocalDateTime("2026-11-10T06:30:00.000Z"),
                "带 Z 的串不应被旧方法接受");
        assertEquals(LocalDateTime.of(2026, 11, 10, 6, 30),
                DateTimeUtil.parseLocalDateTime("2026-11-10T06:30:00"));
    }
}
