package com.wuye.points.service;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantWithdrawalRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.ServiceOrderRepository;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * 可提现余额聚合计算单元测试（Mockito，不加载 Spring 上下文）。
 * 覆盖：空商家、被冻结归零、正常计算、负值截断、技师（服务单）路径。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WithdrawableServiceTest {

    @Mock
    private MerchantWithdrawalRepository merchantWithdrawalRepository;
    @Mock
    private DistributionRecordRepository distributionRecordRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WithdrawalRecordRepository withdrawalRecordRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private com.wuye.points.repository.DeliveryRepository deliveryRepository;
    @Mock
    private WalletFlowRepository walletFlowRepository;

    @InjectMocks
    private WithdrawableService withdrawableService;

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        merchant = new Merchant();
        merchant.setId("m1");
        merchant.setPropertyCompanyId("pc1");

        // 默认（非技师）路径的营收与提现聚合
        when(orderRepository.sumCompletedRevenueByMerchantId("m1")).thenReturn(new BigDecimal("500"));
        when(orderRepository.countCompletedByMerchantId("m1")).thenReturn(3L);
        when(merchantWithdrawalRepository.sumAmountByMerchantId(anyString(), eq("completed")))
                .thenReturn(new BigDecimal("100"));
        when(merchantWithdrawalRepository.sumAmountByMerchantId(anyString(), isNull()))
                .thenReturn(new BigDecimal("200"));
        // 默认无抽佣，便于单独验证其它分支
        when(distributionRecordRepository.sumCommissionByMerchantIdForCompletedOrders(anyString()))
                .thenReturn(BigDecimal.ZERO);
        // B 方案：商家收入 = Σ merchantShare（完成订单）；默认等于营收，便于验证其它分支
        when(distributionRecordRepository.sumMerchantIncomeForCompletedOrders(anyString()))
                .thenReturn(BigDecimal.ZERO);
        when(distributionRecordRepository.sumMerchantReceivableForCompletedOrders(anyString()))
                .thenReturn(BigDecimal.ZERO);
        // 钱包流水（已结算入账）默认为空，各用例按需覆盖
        when(walletFlowRepository.sumAmountByOwnerIdAndEntryType(anyString(), anyString()))
                .thenReturn(BigDecimal.ZERO);
    }

    private void assertAmount(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }

    @Test
    @DisplayName("calcMerchantWithdrawable: 商家为 null 时返回全零")
    void nullMerchant_returnsZeros() {
        WithdrawableService.MerchantWithdrawable r = withdrawableService.calcMerchantWithdrawable(null);
        assertAmount(BigDecimal.ZERO, r.withdrawableAmount());
        assertAmount(BigDecimal.ZERO, r.totalRevenue());
        assertEquals(0L, r.totalOrders());
    }

    @Test
    @DisplayName("calcMerchantWithdrawable: 被冻结时 withdrawable 归零")
    void blocked_returnsZero() {
        merchant.setWithdrawalBlocked(true);
        WithdrawableService.MerchantWithdrawable r = withdrawableService.calcMerchantWithdrawable(merchant);
        assertAmount(BigDecimal.ZERO, r.withdrawableAmount());
        assertEquals(true, r.withdrawalBlocked());
        // 营收统计不受影响
        assertAmount(new BigDecimal("500"), r.totalRevenue());
    }

    @Test
    @DisplayName("calcMerchantWithdrawable: 正常可提现 = 商家收入 - 占用(含pending/approved/completed)")
    void normal_calculation() {
        // 商家收入 500 - 占用 200 = 300
        when(distributionRecordRepository.sumMerchantIncomeForCompletedOrders("m1"))
                .thenReturn(new BigDecimal("500"));
        WithdrawableService.MerchantWithdrawable r = withdrawableService.calcMerchantWithdrawable(merchant);
        assertAmount(new BigDecimal("300"), r.withdrawableAmount());
        assertAmount(new BigDecimal("100"), r.totalWithdrawn());
        assertAmount(new BigDecimal("500"), r.totalRevenue());
        assertEquals(3L, r.totalOrders());
    }

    @Test
    @DisplayName("calcMerchantWithdrawable: 平台抽佣需从商家可提现中扣除（100元订单抽佣20%时商家得80）")
    void deductsPlatformCommission() {
        when(orderRepository.sumCompletedRevenueByMerchantId("m1")).thenReturn(new BigDecimal("100"));
        when(orderRepository.countCompletedByMerchantId("m1")).thenReturn(1L);
        when(merchantWithdrawalRepository.sumAmountByMerchantId(anyString(), isNull()))
                .thenReturn(BigDecimal.ZERO);
        when(distributionRecordRepository.sumCommissionByMerchantIdForCompletedOrders("m1"))
                .thenReturn(new BigDecimal("20"));
        // B 方案：商家收入取分账记录 merchantShare（已扣抽佣）
        when(distributionRecordRepository.sumMerchantIncomeForCompletedOrders("m1"))
                .thenReturn(new BigDecimal("80"));

        WithdrawableService.MerchantWithdrawable r = withdrawableService.calcMerchantWithdrawable(merchant);
        assertAmount(new BigDecimal("80"), r.withdrawableAmount());
        assertAmount(new BigDecimal("20"), r.platformCommission());
        // 营业额口径仍为订单全额
        assertAmount(new BigDecimal("100"), r.totalRevenue());
    }

    @Test
    @DisplayName("calcMerchantWithdrawable: 抽佣+占用超过营收时截断为 0（不为负）")
    void commissionOverRevenue_clampToZero() {
        when(orderRepository.sumCompletedRevenueByMerchantId("m1")).thenReturn(new BigDecimal("100"));
        when(merchantWithdrawalRepository.sumAmountByMerchantId(anyString(), isNull()))
                .thenReturn(new BigDecimal("90"));
        when(distributionRecordRepository.sumCommissionByMerchantIdForCompletedOrders("m1"))
                .thenReturn(new BigDecimal("20"));
        when(distributionRecordRepository.sumMerchantIncomeForCompletedOrders("m1"))
                .thenReturn(new BigDecimal("80"));

        WithdrawableService.MerchantWithdrawable r = withdrawableService.calcMerchantWithdrawable(merchant);
        assertAmount(BigDecimal.ZERO, r.withdrawableAmount());
    }

    @Test
    @DisplayName("calcMerchantWithdrawable: 占用超过营收时截断为 0（不为负）")
    void negativeClampToZero() {
        when(orderRepository.sumCompletedRevenueByMerchantId("m1")).thenReturn(new BigDecimal("100"));
        when(merchantWithdrawalRepository.sumAmountByMerchantId(anyString(), isNull()))
                .thenReturn(new BigDecimal("200"));

        WithdrawableService.MerchantWithdrawable r = withdrawableService.calcMerchantWithdrawable(merchant);
        assertAmount(BigDecimal.ZERO, r.withdrawableAmount());
    }

    @Test
    @DisplayName("calcMerchantWithdrawable: 技师商家走服务单营收路径")
    void technicianSource_usesServiceOrderRevenue() {
        merchant.setMerchantSource("technician");
        merchant.setResidentId("r1");

        Resident provider = new Resident();
        provider.setId("r1");
        when(residentRepository.findById("r1")).thenReturn(Optional.of(provider));
        when(serviceOrderRepository.sumCompletedAmountByProviderId("r1")).thenReturn(new BigDecimal("500"));
        when(serviceOrderRepository.countCompletedByProviderId("r1")).thenReturn(2L);
        // 技师路径仍会计提已占用（completed=100, all=100）
        when(merchantWithdrawalRepository.sumAmountByMerchantId(anyString(), eq("completed")))
                .thenReturn(new BigDecimal("100"));
        when(merchantWithdrawalRepository.sumAmountByMerchantId(anyString(), isNull()))
                .thenReturn(new BigDecimal("100"));
        when(distributionRecordRepository.sumMerchantIncomeForCompletedOrders("m1"))
                .thenReturn(new BigDecimal("500"));

        WithdrawableService.MerchantWithdrawable r = withdrawableService.calcMerchantWithdrawable(merchant);
        // 500 - 100 = 400
        assertAmount(new BigDecimal("400"), r.withdrawableAmount());
        assertAmount(new BigDecimal("500"), r.totalRevenue());
        assertEquals(2L, r.totalOrders());
    }
}
