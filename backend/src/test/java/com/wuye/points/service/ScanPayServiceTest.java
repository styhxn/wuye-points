package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.scanpay.ScanPayCreateRequest;
import com.wuye.points.dto.scanpay.ScanPayCreateResponse;
import com.wuye.points.dto.scanpay.ScanPayStatusResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.MerchantWechatAccount;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.payment.EcommerceSplitProperties;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.payment.WechatPayService;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 扫码即支付回归：订单落库字段、通道选择、失败前置、回调幂等与积分公式。 */
class ScanPayServiceTest {

    private static final String RESIDENT_ID = "res_1";
    private static final String MERCHANT_ID = "mch_1";
    private static final String PROPERTY_ID = "pc_1";
    private static final String ORDER_ID = "ord_scan_1";

    private OrderRepository orderRepository;
    private MerchantRepository merchantRepository;
    private ResidentRepository residentRepository;
    private MerchantPropertyConfigRepository configRepository;
    private PropertyCompanyRepository propertyCompanyRepository;
    private MerchantWechatAccountRepository wechatAccountRepository;
    private PaymentRecordRepository paymentRecordRepository;
    private WechatPayService wechatPayService;
    private WechatSplitService wechatSplitService;
    private EcommerceSplitProperties ecommerceSplitProperties;
    private PointService pointService;
    private OrderCompletionService orderCompletionService;
    private RoleGuard roleGuard;
    private SnowflakeIdGenerator idGenerator;
    private ScanPayService service;

    private Resident resident;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        merchantRepository = mock(MerchantRepository.class);
        residentRepository = mock(ResidentRepository.class);
        configRepository = mock(MerchantPropertyConfigRepository.class);
        propertyCompanyRepository = mock(PropertyCompanyRepository.class);
        wechatAccountRepository = mock(MerchantWechatAccountRepository.class);
        paymentRecordRepository = mock(PaymentRecordRepository.class);
        wechatPayService = mock(WechatPayService.class);
        wechatSplitService = mock(WechatSplitService.class);
        ecommerceSplitProperties = mock(EcommerceSplitProperties.class);
        pointService = mock(PointService.class);
        orderCompletionService = mock(OrderCompletionService.class);
        roleGuard = mock(RoleGuard.class);
        idGenerator = mock(SnowflakeIdGenerator.class);

        service = new ScanPayService(orderRepository, merchantRepository, residentRepository,
                configRepository, propertyCompanyRepository, wechatAccountRepository,
                paymentRecordRepository, wechatPayService, wechatSplitService,
                ecommerceSplitProperties, pointService, orderCompletionService, roleGuard, idGenerator);

        UserContext.set(UserContext.builder().userId(RESIDENT_ID).build());

        resident = new Resident();
        resident.setId(RESIDENT_ID);
        resident.setOpenid("openid_1");
        resident.setPhone("13800000000");
        resident.setPropertyCompanyId(PROPERTY_ID);
        resident.setFamilyId("fam_1");
        resident.setStatus("active");

        merchant = new Merchant();
        merchant.setId(MERCHANT_ID);
        merchant.setName("便民超市");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");

        when(residentRepository.findById(RESIDENT_ID)).thenReturn(Optional.of(resident));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(wechatPayService.isEnabled()).thenReturn(true);
        when(wechatPayService.isDirectEnabled()).thenReturn(true);
        when(paymentRecordRepository.findByBizTypeAndResidentIdAndStatus(
                PaymentBizType.SCAN_PAY, RESIDENT_ID, "pending")).thenReturn(List.of());
        when(ecommerceSplitProperties.isEnabled()).thenReturn(false);
        when(wechatSplitService.isEnabledForProperty(PROPERTY_ID)).thenReturn(false);
        when(idGenerator.generateId("ord")).thenReturn(ORDER_ID);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private ScanPayCreateRequest request(String amount) {
        ScanPayCreateRequest r = new ScanPayCreateRequest();
        r.setMerchantId(MERCHANT_ID);
        r.setAmount(new BigDecimal(amount));
        return r;
    }

    private PaymentRecord paidRecord(String orderId) {
        PaymentRecord rec = new PaymentRecord();
        rec.setId("pr_1");
        rec.setOutTradeNo("wxo_1");
        rec.setCashAmount(new BigDecimal("100.00"));
        rec.setBizType(PaymentBizType.SCAN_PAY);
        rec.setBizId(orderId);
        rec.setPayMode("direct");
        return rec;
    }

    @Test
    @DisplayName("直连通道：一次返回 payParams，订单落成无商品、无配送的扫码单")
    void createDirectChannel() {
        PaymentRecord rec = paidRecord(ORDER_ID);
        when(wechatPayService.createIntent(any(), any(), any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(rec);
        WechatJsPayParams params = new WechatJsPayParams();
        params.setPkg("prepay_id=wx_1");
        when(wechatPayService.miniPay(any(PaymentRecord.class), anyString(), anyString())).thenReturn(params);

        ScanPayCreateResponse resp = service.create(request("28.50"));

        assertEquals(ORDER_ID, resp.getScanPayId());
        assertEquals("pending", resp.getStatus());
        assertEquals(params, resp.getPayParams());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();
        assertEquals("scan_pay", saved.getSourceType());
        assertEquals("pending", saved.getOrderStatus());
        assertEquals(new BigDecimal("28.50"), saved.getTotalAmount());
        assertFalse(saved.getRequiresDelivery());
        assertEquals(BigDecimal.ZERO, saved.getCouponAmount());
        assertEquals(BigDecimal.ZERO, saved.getDeliveryFee());
        assertEquals("none", saved.getFulfillmentMode());
        assertEquals(0, saved.getPointUsed());
    }

    @Test
    @DisplayName("分账启用：走服务商通道并带商家 sub_mchid")
    void createPartnerChannel() {
        when(wechatSplitService.isEnabledForProperty(PROPERTY_ID)).thenReturn(true);
        when(wechatPayService.isPartnerEnabled()).thenReturn(true);
        MerchantWechatAccount account = new MerchantWechatAccount();
        account.setSubMchid("1234567890");
        when(wechatAccountRepository.findByMerchantIdAndStatus(MERCHANT_ID, "active"))
                .thenReturn(Optional.of(account));
        when(wechatPayService.createIntent(any(), any(), any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(paidRecord(ORDER_ID));
        when(wechatPayService.miniPay(any(PaymentRecord.class), anyString(), anyString(), anyString()))
                .thenReturn(new WechatJsPayParams());

        service.create(request("10.00"));

        verify(wechatPayService).miniPay(any(PaymentRecord.class), eq("openid_1"),
                anyString(), eq("1234567890"));
    }

    @Test
    @DisplayName("缺 openid / 商家未进件：下单前拒绝，不生成废单")
    void rejectsBeforeCreatingOrder() {
        resident.setOpenid(null);
        assertThrows(BizException.class, () -> service.create(request("10.00")));

        resident.setOpenid("openid_1");
        when(wechatSplitService.isEnabledForProperty(PROPERTY_ID)).thenReturn(true);
        when(wechatPayService.isPartnerEnabled()).thenReturn(true);
        when(wechatAccountRepository.findByMerchantIdAndStatus(MERCHANT_ID, "active"))
                .thenReturn(Optional.empty());
        BizException e = assertThrows(BizException.class, () -> service.create(request("10.00")));
        assertTrue(e.getMessage().contains("微信进件"));
    }

    @Test
    @DisplayName("回调幂等：重复投递只发一次积分、只结算一次")
    void completeIsIdempotent() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setResidentId(RESIDENT_ID);
        order.setMerchantId(MERCHANT_ID);
        order.setPropertyCompanyId(PROPERTY_ID);
        order.setOrderStatus("pending");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setSourceType("scan_pay");
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(configRepository.findByMerchantIdAndPropertyCompanyId(MERCHANT_ID, PROPERTY_ID))
                .thenReturn(Optional.of(config(new BigDecimal("0.1000"))));
        when(propertyCompanyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyCompany()));
        PaymentRecord rec = paidRecord(ORDER_ID);

        service.completeChannelPayment(rec);
        service.completeChannelPayment(rec);

        verify(pointService, times(1)).earnPoints(eq(resident), eq(100), eq("scan_pay"),
                eq(ORDER_ID), anyString(), any());
        verify(orderCompletionService, times(1)).settleOnCompleted(order);
        assertEquals("completed", order.getOrderStatus());
        assertEquals(100, order.getPointEarned());
    }

    @Test
    @DisplayName("积分公式与商品订单一致：金额×佣金率×用户积分比例×兑换比，向下取整")
    void pointsFollowExistingFormula() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setResidentId(RESIDENT_ID);
        order.setMerchantId(MERCHANT_ID);
        order.setPropertyCompanyId(PROPERTY_ID);
        order.setOrderStatus("pending");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setSourceType("scan_pay");
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(configRepository.findByMerchantIdAndPropertyCompanyId(MERCHANT_ID, PROPERTY_ID))
                .thenReturn(Optional.of(config(new BigDecimal("0.1000"))));
        when(propertyCompanyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(propertyCompany()));

        service.completeChannelPayment(paidRecord(ORDER_ID));

        // 100 元 × 10% 佣金 × 10% 用户积分比例 × 100 积分/元 = 100 积分
        verify(pointService).earnPoints(eq(resident), eq(100), eq("scan_pay"),
                eq(ORDER_ID), anyString(), any());
    }

    @Test
    @DisplayName("轮询：completed 才视为 paid，并返回本次积分")
    void statusOnlyPaidWhenCompleted() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setResidentId(RESIDENT_ID);
        order.setMerchantId(MERCHANT_ID);
        order.setOrderStatus("completed");
        order.setTotalAmount(new BigDecimal("28.50"));
        order.setSourceType("scan_pay");
        order.setPointEarned(12);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentRecordRepository.findFirstByBizIdAndStatusOrderByCreatedAtDesc(ORDER_ID, "paid"))
                .thenReturn(Optional.of(paidRecord(ORDER_ID)));

        ScanPayStatusResponse resp = service.getStatus(ORDER_ID);

        assertEquals("paid", resp.getStatus());
        assertEquals(12, resp.getPointEarned());
        assertEquals("wxo_1", resp.getOutTradeNo());
    }

    private MerchantPropertyConfig config(BigDecimal commissionRate) {
        MerchantPropertyConfig c = new MerchantPropertyConfig();
        c.setStatus("active");
        c.setCommissionRate(commissionRate);
        c.setPointExchangeRate(new BigDecimal("100.0000"));
        return c;
    }

    private PropertyCompany propertyCompany() {
        PropertyCompany pc = new PropertyCompany();
        pc.setUserPointShareRate(new BigDecimal("0.1000"));
        return pc;
    }
}
