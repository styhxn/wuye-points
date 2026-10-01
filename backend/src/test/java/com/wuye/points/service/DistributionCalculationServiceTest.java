package com.wuye.points.service;

import com.wuye.points.dto.distribution.CalculationResult;
import com.wuye.points.dto.distribution.CalculationSnapshot;
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
        assertEquals("distribution_b_v1", r.getCalculationVersion());
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
}
