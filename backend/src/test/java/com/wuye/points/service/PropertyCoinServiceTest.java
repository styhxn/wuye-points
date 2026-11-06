package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.coin.RechargeCoinRequest;
import com.wuye.points.dto.coin.RechargeCoinResponse;
import com.wuye.points.dto.coin.RechargeStatusResponse;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.repository.CoinFreezeRecordRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.FeatureSwitchGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 物业币充值 / 到账回归测试（A1/A2）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyCoinServiceTest {

    @Mock
    private PropertyCoinRecordRepository coinRecordRepository;
    @Mock
    private CoinFreezeRecordRepository coinFreezeRecordRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private PropertyFeeRepository propertyFeeRepository;
    @Mock
    private PropertyCoinConverter coinConverter;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private FeatureSwitchGuard featureSwitchGuard;
    @Mock
    private PayChannel payChannel;
    @Mock
    private PropertyCoinFundService propertyCoinFundService;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @InjectMocks
    private PropertyCoinService propertyCoinService;

    private Resident resident;

    @BeforeEach
    void setUp() {
        UserContext.set(UserContext.builder()
                .userId("res_1")
                .role("resident")
                .propertyCompanyId("pc_1")
                .build());
        resident = new Resident();
        resident.setId("res_1");
        resident.setPropertyCompanyId("pc_1");
        resident.setOpenid("openid_1");
        resident.setStatus("active");
        resident.setCoinBalance(BigDecimal.ZERO);

        when(residentRepository.findById("res_1")).thenReturn(Optional.of(resident));
        when(residentRepository.findByIdForUpdate(anyString())).thenAnswer(inv -> residentRepository.findById(inv.getArgument(0)));
        when(familyRepository.findByIdForUpdate(anyString())).thenAnswer(inv -> familyRepository.findById(inv.getArgument(0)));
        when(propertyCompanyRepository.findById("pc_1")).thenReturn(Optional.empty());
        when(coinConverter.defaultCoinBalance(any())).thenAnswer(inv -> inv.getArgument(0) == null ? BigDecimal.ZERO : inv.getArgument(0));
        when(coinRecordRepository.save(any(PropertyCoinRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRecordRepository.findByBizTypeAndResidentIdAndStatus(
                eq(PaymentBizType.COIN_RECHARGE), eq("res_1"), eq("pending"))).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("recharge: 微信支付未开通时 fail-closed，不直接加余额")
    void recharge_whenPayDisabled_throws() {
        when(payChannel.isEnabled()).thenReturn(false);
        when(payChannel.isDirectEnabled()).thenReturn(false);
        RechargeCoinRequest request = new RechargeCoinRequest();
        request.setAmount(new BigDecimal("100"));

        BizException ex = assertThrows(BizException.class, () -> propertyCoinService.recharge(request));

        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertEquals(BigDecimal.ZERO, resident.getCoinBalance());
        verify(coinRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("recharge: 返回 payParams/outTradeNo，预下单阶段不加余额")
    void recharge_returnsPayParamsWithoutCrediting() {
        when(payChannel.isEnabled()).thenReturn(true);
        when(payChannel.isDirectEnabled()).thenReturn(true);
        PaymentRecord record = paymentRecord("pr_1", "wxo_1", "100");
        when(payChannel.createIntent(eq(PaymentBizType.COIN_RECHARGE), eq("res_1"), eq("res_1"),
                eq("pc_1"), eq("wechat"), any(), eq(0), any(), any())).thenReturn(record);
        WechatJsPayParams params = new WechatJsPayParams();
        params.setOutTradeNo("wxo_1");
        params.setSignType("RSA");
        when(payChannel.miniPay(eq(record), eq("openid_1"), eq("物业币充值"))).thenReturn(params);

        RechargeCoinRequest request = new RechargeCoinRequest();
        request.setAmount(new BigDecimal("100"));
        RechargeCoinResponse response = propertyCoinService.recharge(request);

        assertEquals("pr_1", response.getPaymentRecordId());
        assertEquals("wxo_1", response.getOutTradeNo());
        assertEquals("pending", response.getStatus());
        assertNotNull(response.getPayParams());
        assertEquals("wxo_1", response.getPayParams().getOutTradeNo());
        assertEquals(BigDecimal.ZERO, resident.getCoinBalance());
        verify(coinRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("recharge: 取消支付后再次充值，先作废旧的待支付流水再新建（条目11）")
    void recharge_closesStalePendingThenCreatesNewIntent() {
        when(payChannel.isEnabled()).thenReturn(true);
        when(payChannel.isDirectEnabled()).thenReturn(true);
        PaymentRecord stale = paymentRecord("pr_old", "wxo_old", "100");
        when(paymentRecordRepository.findByBizTypeAndResidentIdAndStatus(
                PaymentBizType.COIN_RECHARGE, "res_1", "pending")).thenReturn(List.of(stale));
        PaymentRecord record = paymentRecord("pr_1", "wxo_1", "100");
        when(payChannel.createIntent(eq(PaymentBizType.COIN_RECHARGE), eq("res_1"), eq("res_1"),
                eq("pc_1"), eq("wechat"), any(), eq(0), any(), any())).thenReturn(record);
        when(payChannel.miniPay(eq(record), eq("openid_1"), eq("物业币充值"))).thenReturn(new WechatJsPayParams());

        RechargeCoinRequest request = new RechargeCoinRequest();
        request.setAmount(new BigDecimal("100"));
        RechargeCoinResponse response = propertyCoinService.recharge(request);

        assertEquals("pr_1", response.getPaymentRecordId());
        verify(payChannel).closeForRetry(eq(stale), eq("物业币充值"), anyString());
        verify(coinRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("recharge: 旧流水其实已支付成功时不再新建，提示稍后再充")
    void recharge_whenStaleAlreadyPaid_doesNotCreateNewIntent() {
        when(payChannel.isEnabled()).thenReturn(true);
        when(payChannel.isDirectEnabled()).thenReturn(true);
        PaymentRecord stale = paymentRecord("pr_old", "wxo_old", "100");
        when(paymentRecordRepository.findByBizTypeAndResidentIdAndStatus(
                PaymentBizType.COIN_RECHARGE, "res_1", "pending")).thenReturn(List.of(stale));
        org.mockito.Mockito.doThrow(new BizException(ErrorCode.ORDER_CONFLICT,
                        "上一笔充值已支付成功，正在到账，请稍后再发起新的充值"))
                .when(payChannel).closeForRetry(eq(stale), anyString(), anyString());

        RechargeCoinRequest request = new RechargeCoinRequest();
        request.setAmount(new BigDecimal("100"));
        BizException ex = assertThrows(BizException.class, () -> propertyCoinService.recharge(request));

        assertEquals(ErrorCode.ORDER_CONFLICT.getCode(), ex.getCode());
        verify(payChannel, never()).createIntent(anyString(), anyString(), anyString(), anyString(),
                anyString(), any(), any(Integer.class), any(), any());
    }

    @Test
    @DisplayName("completeChannelPayment: 同一支付流水重复投递只入账一次")
    void completeChannelPayment_isIdempotent() {
        PaymentRecord record = paymentRecord("pr_1", "wxo_1", "100");
        when(coinRecordRepository.existsByResidentIdAndSourceIdAndCoinType("res_1", "pr_1", "recharge"))
                .thenReturn(true);

        propertyCoinService.completeChannelPayment(record);

        assertEquals(BigDecimal.ZERO, resident.getCoinBalance());
        verify(coinRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("completeChannelPayment: 首次回调按金额入账")
    void completeChannelPayment_creditsOnce() {
        PaymentRecord record = paymentRecord("pr_1", "wxo_1", "100");
        when(coinRecordRepository.existsByResidentIdAndSourceIdAndCoinType("res_1", "pr_1", "recharge"))
                .thenReturn(false);

        propertyCoinService.completeChannelPayment(record);

        assertEquals(0, new BigDecimal("100").compareTo(resident.getCoinBalance()));
        verify(coinRecordRepository).save(any(PropertyCoinRecord.class));
    }

    @Test
    @DisplayName("earnCoins: 有家庭也进个人余额，家庭池不再入账")
    void earnCoins_withFamily_creditsPersonalBalance() {
        resident.setFamilyId("fam_1");
        resident.setCoinBalance(new BigDecimal("10.00"));

        PropertyCoinRecord record = propertyCoinService.earnCoins(
                resident, new BigDecimal("100.00"), "earn", "manual", null, "演示发放", null);

        assertEquals(0, new BigDecimal("110.00").compareTo(resident.getCoinBalance()));
        assertEquals(0, new BigDecimal("110.00").compareTo(record.getBalance()));
        assertNull(record.getFamilyId());
        assertEquals(0, BigDecimal.ZERO.compareTo(record.getFamilyAmount()));
        verify(familyRepository, never()).save(any());
    }

    @Test
    @DisplayName("completeChannelPayment: 有家庭也进个人余额")
    void completeChannelPayment_withFamily_creditsPersonalBalance() {
        resident.setFamilyId("fam_1");
        PaymentRecord record = paymentRecord("pr_1", "wxo_1", "100");
        when(coinRecordRepository.existsByResidentIdAndSourceIdAndCoinType("res_1", "pr_1", "recharge"))
                .thenReturn(false);

        propertyCoinService.completeChannelPayment(record);

        assertEquals(0, new BigDecimal("100").compareTo(resident.getCoinBalance()));
        verify(familyRepository, never()).save(any());
    }

    @Test
    @DisplayName("getRechargeStatus: 已到账返回 paid 与到账余额/时间")
    void getRechargeStatus_paid() {
        PaymentRecord record = paymentRecord("pr_1", "wxo_1", "100");
        record.setStatus("paid");
        PropertyCoinRecord coinRecord = new PropertyCoinRecord();
        coinRecord.setId("coin_1");
        coinRecord.setResidentId("res_1");
        coinRecord.setCoinType("recharge");
        coinRecord.setAmount(new BigDecimal("100"));
        coinRecord.setBalance(new BigDecimal("100"));
        coinRecord.setCreatedAt(LocalDateTime.now());
        when(paymentRecordRepository.findById("pr_1")).thenReturn(Optional.of(record));
        when(coinRecordRepository.findFirstByResidentIdAndSourceIdAndCoinTypeOrderByCreatedAtDesc(
                "res_1", "pr_1", "recharge")).thenReturn(Optional.of(coinRecord));

        RechargeStatusResponse response = propertyCoinService.getRechargeStatus("pr_1");

        assertEquals("paid", response.getStatus());
        assertEquals("wxo_1", response.getOutTradeNo());
        assertEquals(0, new BigDecimal("100").compareTo(response.getBalanceAfter()));
        assertNotNull(response.getRechargedAt());
    }

    private PaymentRecord paymentRecord(String id, String outTradeNo, String amount) {
        PaymentRecord record = new PaymentRecord();
        record.setId(id);
        record.setBizType(PaymentBizType.COIN_RECHARGE);
        record.setBizId("res_1");
        record.setResidentId("res_1");
        record.setPropertyCompanyId("pc_1");
        record.setOutTradeNo(outTradeNo);
        record.setAmount(new BigDecimal(amount));
        record.setCashAmount(new BigDecimal(amount));
        record.setStatus("pending");
        return record;
    }
}
