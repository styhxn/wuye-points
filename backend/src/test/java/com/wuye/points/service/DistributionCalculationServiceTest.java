package com.wuye.points.service;

import com.wuye.points.dto.distribution.CalculationResult;
import com.wuye.points.dto.distribution.CalculationSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分账计算引擎测试：对齐《商品分账与满额配送费减免-移动端对齐规范》§12 完整计算示例。
 */
class DistributionCalculationServiceTest {

    private final DistributionCalculationService service = new DistributionCalculationService();

    private CalculationSnapshot baseSnapshot() {
        CalculationSnapshot s = new CalculationSnapshot();
        s.setMerchantId("mch_demo001");
        s.setPropertyCompanyId("pc_demo001");
        // 一级比例：平台 10% / 物业 60% / 管理盘 30%（规范§12.3）
        s.setPlatformShareRate(new BigDecimal("0.1000"));
        s.setPropertyRate(new BigDecimal("0.6000"));
        s.setCoordinatorRate(new BigDecimal("0.3000"));
        // 级联：板块毛额 60%，个体负责人从板块毛额抽 50%（规范§4.3 → 统筹40%/板块30%/个体30%）
        s.setSectorLeaderRate(new BigDecimal("0.6000"));
        s.setIndividualLeaderRate(new BigDecimal("0.5000"));
        s.setPlatformDeliveryShareRate(new BigDecimal("0.1000"));
        s.setPointExchangeRate(new BigDecimal("100"));
        // 用户侧先扣档置 0：§12 的规范示例早于 V36，置 0 才能继续单独校验一级三档与级联。
        // 先扣档本身由下面 userTier* 三条用例覆盖。
        s.setUserPointShareRate(BigDecimal.ZERO);
        s.setUserRewardShareRate(BigDecimal.ZERO);
        return s;
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertTrue(new BigDecimal(expected).compareTo(actual) == 0,
                () -> "expected " + expected + " but was " + actual);
    }

    @Test
    void ordinaryOrderWithoutWaiver_matchesSpecExample12_1() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("48.00"));
        s.setOriginalDeliveryFee(new BigDecimal("2.00"));
        s.setDeliveryFee(new BigDecimal("2.00"));
        s.setDeliverySubsidySponsor("none");
        s.setCommissionRate(new BigDecimal("0.2000"));
        s.setPointUsed(BigDecimal.ZERO);
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("50.00"));

        CalculationResult r = service.calculate(s);

        assertMoney("48.00", r.getProductAmount());
        assertMoney("0.00", r.getDeliveryWaiverAmount());
        assertMoney("2.00", r.getDeliveryFee());
        assertMoney("38.40", r.getMerchantGoodsShare());
        assertMoney("2.00", r.getDeliverySettlementBase());
        assertMoney("0.20", r.getPlatformDeliveryShare());
        assertMoney("1.80", r.getCourierEarning());
        assertMoney("0.00", r.getMerchantDeliverySubsidy());
        assertMoney("38.40", r.getMerchantShare());
    }

    @Test
    void fullWaiverMerchantSponsor_matchesSpecExample12_2And8_5_2() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("50.00"));
        s.setOriginalDeliveryFee(new BigDecimal("2.00"));
        s.setDeliveryFee(new BigDecimal("0.00"));
        s.setDeliverySubsidySponsor("merchant");
        s.setCommissionRate(new BigDecimal("0.2000"));
        s.setPointUsed(BigDecimal.ZERO);
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("50.00"));

        CalculationResult r = service.calculate(s);

        assertMoney("50.00", r.getProductAmount());
        assertMoney("2.00", r.getDeliveryWaiverAmount());
        assertMoney("0.00", r.getDeliveryFee());
        assertMoney("10.00", r.getCommissionBaseAmount());
        assertMoney("40.00", r.getMerchantGoodsShare());
        // 一级分配
        assertMoney("1.00", r.getPlatformShare());
        assertMoney("6.00", r.getPropertyShare());
        assertMoney("3.00", r.getManagementPoolAmount());
        // 管理盘级联
        assertMoney("1.20", r.getCoordinatorShare());
        assertMoney("0.90", r.getSectorLeaderShare());
        assertMoney("0.90", r.getIndividualLeaderShare());
        // 配送链
        assertMoney("2.00", r.getDeliverySubsidyAmount());
        assertMoney("2.00", r.getDeliverySettlementBase());
        assertMoney("0.20", r.getPlatformDeliveryShare());
        assertMoney("1.80", r.getCourierEarning());
        // 商家
        assertMoney("2.00", r.getMerchantDeliverySubsidy());
        assertMoney("38.00", r.getMerchantShare());
        assertEquals("distribution_b_v2", r.getCalculationVersion());
    }

    @Test
    void platformPoolExample_matchesSpecExample12_3() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("100.00"));
        s.setOriginalDeliveryFee(new BigDecimal("1.00"));
        s.setDeliveryFee(new BigDecimal("1.00"));
        s.setDeliverySubsidySponsor("none");
        s.setCommissionRate(new BigDecimal("0.2000"));
        s.setPointUsed(new BigDecimal("300")); // 300 积分 / 100 = 3 元成本
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("101.00"));

        CalculationResult r = service.calculate(s);

        assertMoney("80.00", r.getMerchantGoodsShare());
        assertMoney("20.00", r.getCommissionBaseAmount());
        assertMoney("3.00", r.getPointCost());
        assertMoney("17.00", r.getDistributableAmount());
        assertMoney("1.70", r.getPlatformShare());
        assertMoney("10.20", r.getPropertyShare());
        assertMoney("5.10", r.getManagementPoolAmount());
        assertMoney("3.06", r.getSectorGrossAmount());
        assertMoney("1.53", r.getIndividualLeaderShare());
        assertMoney("1.53", r.getSectorLeaderShare());
        assertMoney("2.04", r.getCoordinatorShare());
        assertMoney("0.10", r.getPlatformDeliveryShare());
        assertMoney("0.90", r.getCourierEarning());
    }

    @Test
    void subsidyIsInjectedToDeliveryBaseEvenWhenMerchantSponsor() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("50.00"));
        s.setOriginalDeliveryFee(new BigDecimal("2.00"));
        s.setDeliveryFee(new BigDecimal("0.00"));
        s.setDeliverySubsidySponsor("merchant");
        s.setCommissionRate(new BigDecimal("0.2000"));
        s.setPointUsed(BigDecimal.ZERO);
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("50.00"));

        CalculationResult r = service.calculate(s);

        // 配送劳动不因减免而免费（规范§2.4 / §5.2）：基数 = 0 + 2 = 2
        assertMoney("2.00", r.getDeliverySettlementBase());
        assertMoney("1.80", r.getCourierEarning());
    }

    @Test
    void costBreakthrough_doesNotErodeMerchantShare() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("50.00"));
        s.setOriginalDeliveryFee(new BigDecimal("2.00"));
        s.setDeliveryFee(new BigDecimal("2.00"));
        s.setDeliverySubsidySponsor("none");
        s.setCommissionRate(new BigDecimal("0.2000"));
        s.setPointUsed(new BigDecimal("2000")); // 积分成本 20 元 > 平台盘 10 元
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("52.00"));

        CalculationResult r = service.calculate(s);

        assertMoney("0.00", r.getDistributableAmount());
        assertMoney("10.00", r.getDeficitAmount());
        assertEquals("property", r.getDeficitSponsor());
        assertMoney("40.00", r.getMerchantGoodsShare());
        assertMoney("40.00", r.getMerchantShare());
        assertMoney("0.00", r.getPlatformShare());
        assertMoney("0.00", r.getPropertyShare());
        assertMoney("0.00", r.getManagementPoolAmount());
    }

    @Test
    void invalidPrimaryRateTotal_rejected() {
        CalculationSnapshot s = baseSnapshot();
        s.setPlatformShareRate(new BigDecimal("0.1000"));
        s.setPropertyRate(new BigDecimal("0.5000"));
        s.setCoordinatorRate(new BigDecimal("0.3000")); // 合计 0.90 ≠ 1
        s.setProductAmount(new BigDecimal("50.00"));
        s.setOriginalDeliveryFee(new BigDecimal("2.00"));
        s.setDeliveryFee(new BigDecimal("2.00"));
        s.setCommissionRate(new BigDecimal("0.2000"));

        assertThrows(com.wuye.points.exception.BizException.class, () -> service.calculate(s));
    }

    @Test
    void unknownLegacySponsor_treatedAsNone() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("50.00"));
        s.setOriginalDeliveryFee(new BigDecimal("2.00"));
        s.setDeliveryFee(new BigDecimal("0.00"));
        s.setDeliverySubsidySponsor("unknown"); // 规范§17.2
        s.setCommissionRate(new BigDecimal("0.2000"));
        s.setTotalAmount(new BigDecimal("50.00"));

        CalculationResult r = service.calculate(s);

        assertEquals("none", r.getDeliverySubsidySponsor());
        assertMoney("0.00", r.getMerchantDeliverySubsidy());
        assertMoney("40.00", r.getMerchantShare());
    }

    @Test
    void coinPayment_isFundingNotCost() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("100.00"));
        s.setOriginalDeliveryFee(new BigDecimal("0.00"));
        s.setDeliveryFee(new BigDecimal("0.00"));
        s.setDeliverySubsidySponsor("none");
        s.setCommissionRate(new BigDecimal("0.2000"));
        s.setPointUsed(BigDecimal.ZERO);
        s.setCoinUsed(new BigDecimal("60.00"));
        s.setTotalAmount(new BigDecimal("100.00"));

        CalculationResult r = service.calculate(s);

        // 物业币是充值时就已收到现金的支付工具，不再从可分配佣金中扣减
        assertMoney("20.00", r.getDistributableAmount());
        assertMoney("20.00", r.getCommissionBaseAmount());
        assertMoney("80.00", r.getMerchantGoodsShare());
        assertMoney("80.00", r.getMerchantShare());
        assertMoney("0.00", r.getDeficitAmount());
        assertMoney("60.00", r.getCoinCost());
    }

    // ==================== V36 用户侧先扣档 ====================

    /**
     * 复现老板 2026-11-02 分账逻辑图的 100 元样例：
     * 抽佣 5 元 → 先切用户积分 10%（0.5）+ 奖励金 10%（0.5）→ 剩 4 元按 物业50%/统筹50% 分，
     * 统筹盘内 板块毛额 50%、个体从板块毛额抽 60%。
     */
    @Test
    @DisplayName("V36 先扣档：100 元订单按老板逻辑图逐档落数")
    void userTierRates_matchBossDistributionDiagramExample() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("100.00"));
        s.setOriginalDeliveryFee(BigDecimal.ZERO);
        s.setDeliveryFee(BigDecimal.ZERO);
        s.setDeliverySubsidySponsor("none");
        s.setCommissionRate(new BigDecimal("0.0500"));
        s.setPointUsed(BigDecimal.ZERO);
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("100.00"));
        // 平台不留成（图里平台是纯通道）
        s.setPlatformShareRate(BigDecimal.ZERO);
        s.setPropertyRate(new BigDecimal("0.5000"));
        s.setCoordinatorRate(new BigDecimal("0.5000"));
        s.setSectorLeaderRate(new BigDecimal("0.5000"));
        s.setIndividualLeaderRate(new BigDecimal("0.6000"));
        s.setUserPointShareRate(new BigDecimal("0.1000"));
        s.setUserRewardShareRate(new BigDecimal("0.1000"));

        CalculationResult r = service.calculate(s);

        assertMoney("5.00", r.getCommissionBaseAmount());
        assertMoney("0.50", r.getUserPointBudget());
        assertMoney("0.50", r.getUserRewardBudget());
        assertMoney("4.00", r.getDistributableAmount());
        assertMoney("0.00", r.getPlatformShare());
        assertMoney("2.00", r.getPropertyShare());
        assertMoney("2.00", r.getManagementPoolAmount());
        assertMoney("1.00", r.getSectorGrossAmount());
        assertMoney("0.60", r.getIndividualLeaderShare());
        assertMoney("0.40", r.getSectorLeaderShare());
        assertMoney("1.00", r.getCoordinatorShare());
        // 守恒：用户两档 + 平台 + 物业 + 统筹 + 板块 + 个体 = 抽佣基础额
        assertMoney("5.00", r.getUserPointBudget().add(r.getUserRewardBudget())
                .add(r.getPlatformShare()).add(r.getPropertyShare()).add(r.getCoordinatorShare())
                .add(r.getSectorLeaderShare()).add(r.getIndividualLeaderShare()));
    }

    @Test
    @DisplayName("V36 先扣档：未配置比例时按默认 10%/10% 兜底")
    void userTierRates_defaultToTenPercentWhenUnset() {
        CalculationSnapshot s = baseSnapshot();
        s.setUserPointShareRate(null);
        s.setUserRewardShareRate(null);
        s.setProductAmount(new BigDecimal("100.00"));
        s.setOriginalDeliveryFee(BigDecimal.ZERO);
        s.setDeliveryFee(BigDecimal.ZERO);
        s.setDeliverySubsidySponsor("none");
        s.setCommissionRate(new BigDecimal("0.0500"));
        s.setPointUsed(BigDecimal.ZERO);
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("100.00"));

        CalculationResult r = service.calculate(s);

        assertMoney("0.50", r.getUserPointBudget());
        assertMoney("0.50", r.getUserRewardBudget());
        assertMoney("4.00", r.getDistributableAmount());
    }

    @Test
    @DisplayName("V36 先扣档：两档合计超过抽佣基础额时记击穿差额由物业承担，不扣商家")
    void userTierRates_beyondCommission_recordsDeficitNotMerchantCut() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("100.00"));
        s.setOriginalDeliveryFee(BigDecimal.ZERO);
        s.setDeliveryFee(BigDecimal.ZERO);
        s.setDeliverySubsidySponsor("none");
        s.setCommissionRate(new BigDecimal("0.0500"));
        s.setPointUsed(BigDecimal.ZERO);
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("100.00"));
        s.setUserPointShareRate(new BigDecimal("0.6000"));
        s.setUserRewardShareRate(new BigDecimal("0.6000"));

        CalculationResult r = service.calculate(s);

        assertMoney("3.00", r.getUserPointBudget());
        assertMoney("3.00", r.getUserRewardBudget());
        assertMoney("0.00", r.getDistributableAmount());
        assertMoney("1.00", r.getDeficitAmount());
        assertEquals("property", r.getDeficitSponsor());
        // 商家实得 = 100 − 抽佣 5 = 95：击穿差额一律不转嫁给商家
        assertMoney("95.00", r.getMerchantShare());
    }

    @Test
    @DisplayName("V36 积分预算与实发积分数一分不差：0.335 元按 33 积分（0.33 元）扣，不得四舍五入成 0.34")
    void userPointBudget_truncatesToGrantedPointCount() {
        CalculationSnapshot s = baseSnapshot();
        s.setProductAmount(new BigDecimal("67.00"));
        s.setCommissionRate(new BigDecimal("0.0500"));   // 抽佣基础额 = 3.35
        s.setOriginalDeliveryFee(BigDecimal.ZERO);
        s.setDeliveryFee(BigDecimal.ZERO);
        s.setDeliverySubsidySponsor("none");
        s.setPointUsed(BigDecimal.ZERO);
        s.setCoinUsed(BigDecimal.ZERO);
        s.setTotalAmount(new BigDecimal("67.00"));
        s.setUserPointShareRate(new BigDecimal("0.1000")); // 3.35 × 10% = 0.335 元 = 33.5 积分
        s.setUserRewardShareRate(new BigDecimal("0.1000"));

        CalculationResult r = service.calculate(s);

        assertMoney("3.35", r.getCommissionBaseAmount());
        assertMoney("0.33", r.getUserPointBudget());
        // 奖励金按元直接取整（与 OrderService 同为 HALF_UP），0.335 → 0.34
        assertMoney("0.34", r.getUserRewardBudget());
        assertMoney("2.68", r.getDistributableAmount());
    }

    @Test
    @DisplayName("V36 先扣档：比例越界（>1）直接拒绝")
    void userTierRates_outOfRange_rejected() {
        CalculationSnapshot s = baseSnapshot();
        s.setUserPointShareRate(new BigDecimal("1.5000"));
        s.setProductAmount(new BigDecimal("100.00"));
        s.setOriginalDeliveryFee(BigDecimal.ZERO);
        s.setDeliveryFee(BigDecimal.ZERO);
        s.setCommissionRate(new BigDecimal("0.0500"));
        s.setTotalAmount(new BigDecimal("100.00"));

        assertThrows(com.wuye.points.exception.BizException.class, () -> service.calculate(s));
    }
}
