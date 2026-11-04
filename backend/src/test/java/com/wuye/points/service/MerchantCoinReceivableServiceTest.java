package com.wuye.points.service;

import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantWithdrawalRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.WechatSplitRecordRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("商家物业币应收：现金/物业币拆账")
class MerchantCoinReceivableServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DistributionRecordRepository distributionRecordRepository;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;
    @Mock
    private WechatSplitRecordRepository splitRecordRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private MerchantWithdrawalRepository merchantWithdrawalRepository;

    @InjectMocks
    private MerchantCoinReceivableService service;

    private Merchant merchant;
    private Order order;

    @BeforeEach
    void setUp() {
        merchant = new Merchant();
        merchant.setId("mch_1");
        merchant.setMerchantSource("platform");

        order = new Order();
        order.setId("ord_1");
        order.setOrderNo("NO1");
        order.setMerchantId("mch_1");
        order.setOrderStatus("completed");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCouponAmount(BigDecimal.ZERO);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setCompletedAt(LocalDateTime.now());
        order.setDeliverySubsidyAmount(BigDecimal.ZERO);

        when(orderRepository.findByMerchantIdAndOrderStatusOrderByCompletedAtDesc("mch_1", "completed"))
                .thenReturn(List.of(order));
        DistributionRecord dist = new DistributionRecord();
        dist.setOrderId("ord_1");
        dist.setCommissionRate(new BigDecimal("0.2000"));
        when(distributionRecordRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(dist));
        when(paymentRecordRepository.findByBizTypeAndBizIdInOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(PaymentBizType.ORDER), anyCollection()))
                .thenReturn(List.of());
        when(splitRecordRepository.findByOrderIdInAndStatusIn(anyCollection(), anyList()))
                .thenReturn(List.of());
        when(deliveryRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of());
        when(merchantWithdrawalRepository.sumAmountByMerchantIdAndStatusesAndSource(
                anyString(), anyCollection(), anyString())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("纯物业币订单：商家应得全部由物业币兑付")
    void pureCoin() {
        order.setCoinUsed(new BigDecimal("100.00"));
        order.setCashAmount(BigDecimal.ZERO);

        MerchantCoinReceivableService.MerchantIncome income = service.calc(merchant);

        assertAmount("80.00", income.totalIncomeAmount());
        assertAmount("80.00", income.coinReceivableAmount());
        assertAmount("0.00", income.internalCashAmount());
        assertAmount("0.00", income.wechatCashAmount());
    }

    @Test
    @DisplayName("混合支付+服务商分账已执行：现金先抵抽佣，剩余为物业币应收")
    void mixedPartnerWithSplit() {
        order.setCoinUsed(new BigDecimal("60.00"));
        order.setCashAmount(new BigDecimal("40.00"));
        PaymentRecord paid = new PaymentRecord();
        paid.setBizId("ord_1");
        paid.setStatus("paid");
        paid.setPayMode("partner");
        when(paymentRecordRepository.findByBizTypeAndBizIdInOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(PaymentBizType.ORDER), anyCollection()))
                .thenReturn(List.of(paid));
        WechatSplitRecord split = new WechatSplitRecord();
        split.setOrderId("ord_1");
        split.setFreezeAmount(new BigDecimal("20.00"));
        when(splitRecordRepository.findByOrderIdInAndStatusIn(anyCollection(), anyList()))
                .thenReturn(List.of(split));

        MerchantCoinReceivableService.MerchantIncome income = service.calc(merchant);

        assertAmount("80.00", income.totalIncomeAmount());
        assertAmount("60.00", income.coinReceivableAmount());
        assertAmount("20.00", income.wechatCashAmount());
        assertAmount("0.00", income.internalCashAmount());
    }

    @Test
    @DisplayName("纯现金订单：全部由微信侧结算，不进入内部提现")
    void cashPartner() {
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(new BigDecimal("100.00"));
        PaymentRecord paid = new PaymentRecord();
        paid.setBizId("ord_1");
        paid.setStatus("paid");
        paid.setPayMode("partner");
        when(paymentRecordRepository.findByBizTypeAndBizIdInOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(PaymentBizType.ORDER), anyCollection()))
                .thenReturn(List.of(paid));
        WechatSplitRecord split = new WechatSplitRecord();
        split.setOrderId("ord_1");
        split.setFreezeAmount(new BigDecimal("20.00"));
        when(splitRecordRepository.findByOrderIdInAndStatusIn(anyCollection(), anyList()))
                .thenReturn(List.of(split));

        MerchantCoinReceivableService.MerchantIncome income = service.calc(merchant);

        assertAmount("80.00", income.totalIncomeAmount());
        assertAmount("0.00", income.coinReceivableAmount());
        assertAmount("80.00", income.wechatCashAmount());
        assertAmount("0.00", income.internalCashAmount());
    }

    @Test
    @DisplayName("直连代收现金：现金在平台手里，计入内部应付")
    void directCash() {
        order.setCoinUsed(new BigDecimal("60.00"));
        order.setCashAmount(new BigDecimal("40.00"));
        PaymentRecord paid = new PaymentRecord();
        paid.setBizId("ord_1");
        paid.setStatus("paid");
        paid.setPayMode("direct");
        when(paymentRecordRepository.findByBizTypeAndBizIdInOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(PaymentBizType.ORDER), anyCollection()))
                .thenReturn(List.of(paid));

        MerchantCoinReceivableService.MerchantIncome income = service.calc(merchant);

        assertAmount("80.00", income.totalIncomeAmount());
        assertAmount("40.00", income.coinReceivableAmount());
        assertAmount("40.00", income.internalCashAmount());
        assertAmount("0.00", income.wechatCashAmount());
    }

    @Test
    @DisplayName("内部收益店铺（技师店）：不拆物业币，保留内部总收益")
    void internalMerchantKeepsLegacy() {
        merchant.setMerchantSource("technician");
        order.setCoinUsed(new BigDecimal("100.00"));
        order.setCashAmount(BigDecimal.ZERO);

        assertTrue(service.isInternalEarningsMerchant(merchant));
        MerchantCoinReceivableService.MerchantIncome income = service.calc(merchant);

        assertAmount("80.00", income.totalIncomeAmount());
        assertAmount("0.00", income.coinReceivableAmount());
        assertAmount("80.00", income.internalCashAmount());
    }

    @Test
    @DisplayName("标准商家不走内部收益口径")
    void standardMerchantNotInternal() {
        assertFalse(service.isInternalEarningsMerchant(merchant));
    }

    private void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }
}
