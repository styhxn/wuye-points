package com.wuye.points.service;

import com.wuye.points.dto.distribution.DistributionRecordItemResponse;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 分账列表读侧 deficitAmount 口径回归：必须与试算 buildShareBreakdown 一致，
 * 即抽佣基础额先扣积分成本、再扣用户积分/奖励金两档留成，扣穿才算缺口。
 */
@DisplayName("DistributionConverter 缺口口径")
class DistributionConverterDeficitTest {

    private final DistributionConverter converter = new DistributionConverter();

    @Test
    @DisplayName("两档留成把佣金扣穿时，列表缺口须等于试算口径 0.50（旧口径漏算两档会报 0.00）")
    void deficitIncludesUserSideBudgets() {
        DistributionRecord record = record(new BigDecimal("0.05"), new BigDecimal("3.00"),
                new BigDecimal("1.50"), new BigDecimal("1.00"));
        // 抽佣基础额 5.00 − 积分成本 3.00 − 用户积分留成 1.50 − 奖励金留成 1.00 = −0.50
        record.setDistributableAmount(BigDecimal.ZERO);

        DistributionRecordItemResponse item = converter.toListItem(
                record, order(), null, null, null, Map.of());

        assertEquals(new BigDecimal("5.00"), item.getCommissionBaseAmount());
        assertEquals(new BigDecimal("0.50"), item.getDeficitAmount());
    }

    @Test
    @DisplayName("V36 前历史单两档为 null：按 0 计，退化为旧口径，且响应保持键缺失语义")
    void legacyRecordWithoutBudgetsFallsBackToZero() {
        DistributionRecord record = record(new BigDecimal("0.05"), new BigDecimal("3.00"), null, null);
        record.setDistributableAmount(new BigDecimal("2.00"));

        DistributionRecordItemResponse item = converter.toListItem(
                record, order(), null, null, null, Map.of());

        assertEquals(new BigDecimal("0.00"), item.getDeficitAmount());
        assertNull(item.getUserPointBudget(), "历史单不得把 null 渲染成 0.00");
        assertNull(item.getUserRewardBudget(), "历史单不得把 null 渲染成 0.00");
        assertNull(item.getUserPointShareRate());
        assertNull(item.getUserRewardShareRate());
    }

    @Test
    @DisplayName("佣金足以覆盖成本与两档留成时缺口为 0")
    void noDeficitWhenCommissionCoversAll() {
        DistributionRecord record = record(new BigDecimal("0.10"), new BigDecimal("3.00"),
                new BigDecimal("1.50"), new BigDecimal("1.00"));
        record.setDistributableAmount(new BigDecimal("4.50"));

        DistributionRecordItemResponse item = converter.toListItem(
                record, order(), null, null, null, Map.of());

        assertEquals(new BigDecimal("0.00"), item.getDeficitAmount());
    }

    @Test
    @DisplayName("冲正行不产生虚假缺口：金额为负时缺口仍夹到 0")
    void reversalRowKeepsDeficitAtZero() {
        DistributionRecord record = record(new BigDecimal("0.05"), new BigDecimal("-3.00"),
                new BigDecimal("-1.50"), new BigDecimal("-1.00"));
        record.setTotalAmount(new BigDecimal("-100.00"));
        record.setDistributableAmount(BigDecimal.ZERO);

        DistributionRecordItemResponse item = converter.toListItem(
                record, order(), null, null, null, Map.of());

        assertEquals(new BigDecimal("0.00"), item.getDeficitAmount());
        assertEquals("reversed", item.getStatus());
    }

    private DistributionRecord record(BigDecimal commissionRate, BigDecimal pointCost,
                                      BigDecimal userPointBudget, BigDecimal userRewardBudget) {
        DistributionRecord record = new DistributionRecord();
        record.setId("dr_001");
        record.setOrderId("ord_001");
        record.setMerchantId("mch_001");
        record.setPropertyCompanyId("pc_demo001");
        record.setResidentId("res_001");
        record.setTotalAmount(new BigDecimal("100.00"));
        record.setCommissionRate(commissionRate);
        record.setPointCost(pointCost);
        record.setCoinCost(new BigDecimal("0.00"));
        record.setPlatformShare(BigDecimal.ZERO);
        record.setPropertyShare(BigDecimal.ZERO);
        record.setUserPointShareRate(userPointBudget == null ? null : new BigDecimal("0.0300"));
        record.setUserRewardShareRate(userRewardBudget == null ? null : new BigDecimal("0.0200"));
        record.setUserPointBudget(userPointBudget);
        record.setUserRewardBudget(userRewardBudget);
        record.setCreatedAt(LocalDateTime.of(2026, 11, 3, 10, 0));
        return record;
    }

    private Order order() {
        Order order = new Order();
        order.setOrderNo("NO20261103001");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCouponAmount(BigDecimal.ZERO);
        return order;
    }
}
