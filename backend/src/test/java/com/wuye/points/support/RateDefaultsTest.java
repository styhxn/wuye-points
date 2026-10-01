package com.wuye.points.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 费率默认值测试。
 *
 * <p>核心目的：锁死「分账比例」与「负责人佣金率」是两组不同语义的常量。
 * 历史上二者共用 DEFAULT_SECTOR_LEADER_RATE 这个名字，极易在后续维护中被误统一，
 * 一旦统一就会造成分账金额错配。
 */
class RateDefaultsTest {

    @Test
    @DisplayName("配置值有效时优先使用配置值")
    void configuredValueWins() {
        BigDecimal configured = new BigDecimal("0.3000");
        assertEquals(0, configured.compareTo(RateDefaults.orDefault(configured, RateDefaults.SECTOR_LEADER_RATE)));
    }

    @Test
    @DisplayName("配置为 null 时使用兜底值")
    void nullFallsBackToDefault() {
        assertEquals(0, RateDefaults.POINT_EXCHANGE_RATE.compareTo(
                RateDefaults.orDefault(null, RateDefaults.POINT_EXCHANGE_RATE)));
    }

    @Test
    @DisplayName("配置为 0 或负数时视为未配置，使用兜底值")
    void nonPositiveFallsBackToDefault() {
        assertEquals(0, RateDefaults.SECTOR_LEADER_RATE.compareTo(
                RateDefaults.orDefault(BigDecimal.ZERO, RateDefaults.SECTOR_LEADER_RATE)));
        assertEquals(0, RateDefaults.INDIVIDUAL_LEADER_RATE.compareTo(
                RateDefaults.orDefault(new BigDecimal("-0.1"), RateDefaults.INDIVIDUAL_LEADER_RATE)));
    }

    @Test
    @DisplayName("分账比例与负责人佣金率必须是不同的值（防止被误统一）")
    void shareRateDiffersFromCommissionRate() {
        assertNotEquals(0,
                RateDefaults.SECTOR_LEADER_RATE.compareTo(RateDefaults.SECTOR_LEADER_COMMISSION_RATE),
                "分账比例与佣金率若被统一，会导致分账金额错配");
        assertNotEquals(0,
                RateDefaults.INDIVIDUAL_LEADER_RATE.compareTo(RateDefaults.INDIVIDUAL_LEADER_COMMISSION_RATE));
    }

    @Test
    @DisplayName("关键费率数值保持稳定（变更需同步通知业务方）")
    void keyRatesAreStable() {
        assertEquals(0, new BigDecimal("0.6000").compareTo(RateDefaults.SECTOR_LEADER_RATE));
        assertEquals(0, new BigDecimal("0.5000").compareTo(RateDefaults.INDIVIDUAL_LEADER_RATE));
        assertEquals(0, new BigDecimal("0.1000").compareTo(RateDefaults.SECTOR_LEADER_COMMISSION_RATE));
        assertEquals(0, new BigDecimal("0.0060").compareTo(RateDefaults.WITHDRAWAL_FEE_RATE));
        assertEquals(0, new BigDecimal("0.0100").compareTo(RateDefaults.POINT_TO_FEE_RATE));
        assertEquals(0, new BigDecimal("100.0000").compareTo(RateDefaults.POINT_EXCHANGE_RATE));
    }

    @Test
    @DisplayName("容差常量用于比例合计校验")
    void toleranceIsDefined() {
        assertEquals(0, new BigDecimal("0.0001").compareTo(RateDefaults.RATE_TOLERANCE));
    }
}
