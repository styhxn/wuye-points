package com.wuye.points.service;

import com.wuye.points.dto.distribution.CalculationResult;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.WalletFlow;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 钱包流水账本测试（规范§7.4 / §9.5 / §10.5）：入账、配送员收入、冲正、幂等。
 */
@ExtendWith(MockitoExtension.class)
class WalletFlowServiceTest {

    @Mock
    private WalletFlowRepository walletFlowRepository;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    private WalletFlowService service;

    @BeforeEach
    void setUp() {
        // WalletFlowService 不再直接持有 idGenerator：单条流水的写入已下沉到 WalletFlowWriter
        // （独立事务，用于隔离并发幂等冲突）。这里用真实 Writer 实例 + mock 的 Repository，
        // 既能验证落库内容，又不必启动 Spring 上下文。
        service = new WalletFlowService(walletFlowRepository,
                new WalletFlowWriter(walletFlowRepository, idGenerator));
        lenient().when(idGenerator.generateId(anyString())).thenReturn("wf_test");
        lenient().when(walletFlowRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
    }

    private CalculationResult result() {
        // 对齐规范§8.5.2 示例：50 商品 / 2 配送费减免（商家承担）
        return CalculationResult.builder()
                .merchantGoodsShare(new BigDecimal("40.00"))
                .merchantDeliverySubsidy(new BigDecimal("2.00"))
                .merchantShare(new BigDecimal("38.00"))
                .platformShare(new BigDecimal("1.00"))
                .platformDeliveryShare(new BigDecimal("0.20"))
                .propertyShare(new BigDecimal("6.00"))
                .coordinatorShare(new BigDecimal("1.20"))
                .sectorLeaderShare(new BigDecimal("0.90"))
                .individualLeaderShare(new BigDecimal("0.90"))
                .deficitAmount(new BigDecimal("0.00"))
                .deficitSponsor(null)
                .build();
    }

    private DistributionRecord record() {
        DistributionRecord r = new DistributionRecord();
        r.setId("dr_1");
        r.setOrderId("ord_1");
        r.setMerchantId("mch_1");
        r.setPropertyCompanyId("pc_1");
        r.setCoordinatorId("coo_1");
        r.setSectorLeaderId("sl_1");
        r.setIndividualLeaderId("il_1");
        return r;
    }

    private Order order() {
        Order o = new Order();
        o.setId("ord_1");
        return o;
    }

    @Test
    @DisplayName("recordDistribution: 商家/公司/物业/管理盘三级入账，商家净额=merchantShare")
    void recordDistribution_writesAllOwners() {
        service.recordDistribution(result(), record(), order());

        ArgumentCaptor<WalletFlow> captor = ArgumentCaptor.forClass(WalletFlow.class);
        verify(walletFlowRepository, org.mockito.Mockito.times(8)).saveAndFlush(captor.capture());
        List<WalletFlow> flows = captor.getAllValues();

        assertEquals(8, flows.size());
        WalletFlow merchantIncome = flows.stream()
                .filter(f -> "merchant_goods_income".equals(f.getEntryType())).findFirst().orElseThrow();
        assertEquals("merchant", merchantIncome.getOwnerType());
        assertEquals("mch_1", merchantIncome.getOwnerId());
        assertEquals(0, new BigDecimal("40.00").compareTo(merchantIncome.getAmount()));

        WalletFlow merchantSubsidy = flows.stream()
                .filter(f -> "merchant_delivery_subsidy".equals(f.getEntryType())).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("-2.00").compareTo(merchantSubsidy.getAmount()));

        WalletFlow platformDelivery = flows.stream()
                .filter(f -> "platform_delivery_share".equals(f.getEntryType())).findFirst().orElseThrow();
        assertEquals(WalletFlowService.PLATFORM_OWNER_ID, platformDelivery.getOwnerId());
        assertEquals(0, new BigDecimal("0.20").compareTo(platformDelivery.getAmount()));

        WalletFlow coordinator = flows.stream()
                .filter(f -> "coordinator_share".equals(f.getEntryType())).findFirst().orElseThrow();
        assertEquals("coo_1", coordinator.getOwnerId());
        assertTrue(coordinator.getIdempotencyKey().startsWith("distribution:settle:ord_1:"));
    }

    @Test
    @DisplayName("recordDistribution: 物业承担配送补贴时记录 delivery_subsidy_cost 成本流水（§2.5）")
    void recordDistribution_propertySponsorWritesCostFlow() {
        CalculationResult r = result();
        r.setDeliverySubsidySponsor("property");
        r.setDeliverySubsidyAmount(new BigDecimal("2.00"));

        service.recordDistribution(r, record(), order());

        ArgumentCaptor<WalletFlow> captor = ArgumentCaptor.forClass(WalletFlow.class);
        verify(walletFlowRepository, org.mockito.Mockito.times(9)).saveAndFlush(captor.capture());
        WalletFlow cost = captor.getAllValues().stream()
                .filter(f -> "delivery_subsidy_cost".equals(f.getEntryType()))
                .findFirst().orElseThrow();
        assertEquals("property", cost.getOwnerType());
        assertEquals("pc_1", cost.getOwnerId());
        assertEquals(0, new BigDecimal("-2.00").compareTo(cost.getAmount()));
        assertTrue(cost.getIdempotencyKey().startsWith("distribution:settle:ord_1:"));
    }

    @Test
    @DisplayName("recordDistribution: 未挂接负责人的角色不产生流水")
    void recordDistribution_skipsMissingLeaders() {
        DistributionRecord rec = record();
        rec.setCoordinatorId(null);
        rec.setSectorLeaderId(null);
        rec.setIndividualLeaderId(null);

        service.recordDistribution(result(), rec, order());

        ArgumentCaptor<WalletFlow> captor = ArgumentCaptor.forClass(WalletFlow.class);
        verify(walletFlowRepository, org.mockito.Mockito.times(5)).saveAndFlush(captor.capture());
        List<WalletFlow> flows = captor.getAllValues();
        // 商家2条 + 公司2条 + 物业1条 = 5 条，无管理盘三级
        assertEquals(5, flows.size());
    }

    @Test
    @DisplayName("recordCourierDelivery: 无接单员时跳过")
    void recordCourierDelivery_skipsWithoutCourier() {
        com.wuye.points.entity.Delivery d = new com.wuye.points.entity.Delivery();
        d.setId("dlv_1");
        d.setOrderId("ord_1");
        d.setCourierId(null);
        d.setCourierEarning(new BigDecimal("1.80"));

        service.recordCourierDelivery(d);

        verify(walletFlowRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("recordCourierDelivery: 接单后写入配送员收入，幂等键 delivery:settle:{id}")
    void recordCourierDelivery_writesCourierIncome() {
        com.wuye.points.entity.Delivery d = new com.wuye.points.entity.Delivery();
        d.setId("dlv_1");
        d.setOrderId("ord_1");
        d.setCourierId("res_courier");
        d.setCourierEarning(new BigDecimal("1.80"));

        service.recordCourierDelivery(d);

        ArgumentCaptor<WalletFlow> captor = ArgumentCaptor.forClass(WalletFlow.class);
        verify(walletFlowRepository).saveAndFlush(captor.capture());
        WalletFlow flow = captor.getValue();
        assertEquals("courier", flow.getOwnerType());
        assertEquals("res_courier", flow.getOwnerId());
        assertEquals("courier_delivery_income", flow.getEntryType());
        assertEquals(0, new BigDecimal("1.80").compareTo(flow.getAmount()));
        assertEquals("delivery:settle:dlv_1", flow.getIdempotencyKey());
    }

    @Test
    @DisplayName("recordReversal: 对原流水逐条生成负数冲正，引用原流水ID")
    void recordReversal_writesNegativeFlows() {
        WalletFlow original = new WalletFlow();
        original.setId("wf_original");
        original.setOwnerType("merchant");
        original.setOwnerId("mch_1");
        original.setEntryType("merchant_goods_income");
        original.setAmount(new BigDecimal("40.00"));
        original.setBalanceType("withdrawable");
        when(walletFlowRepository.findByDistributionRecordId("dr_1")).thenReturn(List.of(original));

        DistributionRecord originalRecord = record();
        service.recordReversal("ord_1", originalRecord);

        ArgumentCaptor<WalletFlow> captor = ArgumentCaptor.forClass(WalletFlow.class);
        verify(walletFlowRepository).saveAndFlush(captor.capture());
        WalletFlow reversal = captor.getValue();
        assertEquals("distribution_reversal", reversal.getEntryType());
        assertEquals(0, new BigDecimal("-40.00").compareTo(reversal.getAmount()));
        assertEquals("wf_original", reversal.getRefRecordId());
        assertTrue(reversal.getIdempotencyKey().startsWith("distribution:reverse:ord_1:"));
    }

    @Test
    @DisplayName("幂等: 已存在幂等键时跳过写入（§9.5）")
    void flow_skipsWhenIdempotencyKeyExists() {
        when(walletFlowRepository.existsByIdempotencyKey(anyString())).thenReturn(true);

        service.recordDistribution(result(), record(), order());

        verify(walletFlowRepository, never()).saveAndFlush(any());
    }
}
