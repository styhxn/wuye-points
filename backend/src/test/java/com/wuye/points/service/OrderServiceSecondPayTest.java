package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.order.PayOrderRequest;
import com.wuye.points.dto.order.PayOrderResponse;
import com.wuye.points.entity.*;
import com.wuye.points.payment.*;
import com.wuye.points.repository.*;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 条目13 二次支付（用户取消微信支付后重新付款）回归测试。
 *
 * <p>微信侧的「同一业务单只能有一条待支付流水」由生产库唯一键
 * {@code uk_payment_records_biz_pending(biz_type, biz_id, active_flag)} 保证，
 * H2 用例库没有这个生成列，所以本类用 Mockito 断言链路行为：
 * 旧流水被作废（closeForRetry）、新建一条 pending、库存只预占一次。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("条目13：商城订单二次支付")
class OrderServiceSecondPayTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private MerchantRepository merchantRepository;
    @Mock private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductStockService productStockService;
    @Mock private ResidentRepository residentRepository;
    @Mock private PointService pointService;
    @Mock private PropertyCoinService propertyCoinService;
    @Mock private OrderConverter orderConverter;
    @Mock private RoleGuard roleGuard;
    @Mock private SnowflakeIdGenerator idGenerator;
    @Mock private DeliveryService deliveryService;
    @Mock private DistributionService distributionService;
    @Mock private FulfillmentAuditService fulfillmentAuditService;
    @Mock private DistributorProductRepository distributorProductRepository;
    @Mock private ResidentMerchantListingRepository listingRepository;
    @Mock private ResidentMerchantSettlementService residentMerchantSettlementService;
    @Mock private OrderCompletionService orderCompletionService;
    @Mock private PropertyCompanyRepository propertyCompanyRepository;
    @Mock private PropertyCoinMallRuleService propertyCoinMallRuleService;
    @Mock private CanteenService canteenService;
    @Mock private PlatformSettingService platformSettingService;
    @Mock private FamilyRepository familyRepository;
    @Mock private WechatPayService wechatPayService;
    @Mock private WechatSplitService wechatSplitService;
    @Mock private WechatSplitProperties wechatSplitProperties;
    @Mock private PaymentRecordRepository paymentRecordRepository;
    @Mock private EcommerceSplitProperties ecommerceSplitProperties;
    @Mock private MerchantWechatAccountRepository merchantWechatAccountRepository;
    @Mock private CouponService couponService;
    @Mock private BuildingLeaderRepository buildingLeaderRepository;
    @Mock private PropertyManagerRepository propertyManagerRepository;
    @Mock private ResidentMerchantApplicationRepository residentMerchantApplicationRepository;
    @Mock private ListingStockService listingStockService;

    @InjectMocks
    private OrderService orderService;

    private PaymentRecord stalePending;

    @BeforeEach
    void setUp() {
        UserContext.set(UserContext.builder().userId("res_1").role("resident").build());

        Resident resident = new Resident();
        resident.setId("res_1");
        resident.setStatus("active");
        resident.setPropertyCompanyId("pc_1");
        resident.setOpenid("openid_1");
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setPointBalance(0);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(resident));

        Order order = new Order();
        order.setId("ord_1");
        order.setOrderNo("NO_1");
        order.setResidentId("res_1");
        order.setMerchantId("mch_1");
        order.setPropertyCompanyId("pc_1");
        order.setOrderStatus("pending");
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        when(orderRepository.findById("ord_1")).thenReturn(Optional.of(order));

        Merchant merchant = new Merchant();
        merchant.setId("mch_1");
        merchant.setName("测试商家");
        merchant.setAuditStatus("approved");
        merchant.setDeliveryFee(BigDecimal.ZERO);
        when(merchantRepository.findById("mch_1")).thenReturn(Optional.of(merchant));

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId("mpc_1");
        config.setMerchantId("mch_1");
        config.setPropertyCompanyId("pc_1");
        config.setStatus("active");
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        when(merchantPropertyConfigRepository.findByMerchantIdAndPropertyCompanyId("mch_1", "pc_1"))
                .thenReturn(Optional.of(config));

        OrderItem item = new OrderItem();
        item.setId("oit_1");
        item.setOrderId("ord_1");
        item.setProductId("prd_1");
        item.setProductName("测试商品");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setTotalPrice(new BigDecimal("10.00"));
        when(orderItemRepository.findByOrderId("ord_1")).thenReturn(List.of(item));

        when(couponService.lockForOrder(any(), anyString(), anyString(), any(), anyString()))
                .thenReturn(BigDecimal.ZERO);
        when(wechatPayService.isEnabled()).thenReturn(true);
        when(wechatPayService.isDirectEnabled()).thenReturn(true);

        stalePending = new PaymentRecord();
        stalePending.setId("pr_old");
        stalePending.setBizType(PaymentBizType.ORDER);
        stalePending.setBizId("ord_1");
        stalePending.setStatus("pending");
        stalePending.setOutTradeNo("wxo_old");
        stalePending.setCashAmount(new BigDecimal("10.00"));
        stalePending.setCreatedAt(LocalDateTime.now().minusMinutes(2));

        PaymentRecord created = new PaymentRecord();
        created.setId("pr_new");
        created.setBizType(PaymentBizType.ORDER);
        created.setBizId("ord_1");
        created.setStatus("pending");
        created.setOutTradeNo("wxo_new");
        created.setCashAmount(new BigDecimal("10.00"));
        when(wechatPayService.createIntent(eq(PaymentBizType.ORDER), eq("ord_1"), anyString(), any(),
                anyString(), any(), any(Integer.class), any(), any())).thenReturn(created);
        when(idGenerator.generateId(anyString())).thenReturn("gen_1");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private PayOrderRequest wechatPay() {
        PayOrderRequest request = new PayOrderRequest();
        request.setPaymentMethod("wechat");
        return request;
    }

    @Test
    @DisplayName("首次支付：扣一次库存并新建待支付流水")
    void firstPay_deductsStockAndCreatesIntent() {
        when(paymentRecordRepository.findByBizTypeAndBizIdAndStatus(PaymentBizType.ORDER, "ord_1", "pending"))
                .thenReturn(List.of());

        PayOrderResponse response = orderService.payOrder("ord_1", wechatPay());

        assertEquals("pr_new", response.getPaymentRecordId());
        verify(productStockService, times(1)).deductAll(any());
        verify(wechatPayService, never()).closeForRetry(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("取消支付后再次支付：作废旧流水后照常调起，不再回 409「订单支付处理中」")
    void secondPay_closesStaleRecordAndCreatesNewIntent() {
        when(paymentRecordRepository.findByBizTypeAndBizIdAndStatus(PaymentBizType.ORDER, "ord_1", "pending"))
                .thenReturn(List.of(stalePending));

        PayOrderResponse response = assertDoesNotThrow(() -> orderService.payOrder("ord_1", wechatPay()));

        assertEquals("pr_new", response.getPaymentRecordId());
        verify(wechatPayService).closeForRetry(eq(stalePending), eq("商城订单"), anyString());
        verify(wechatPayService).miniPay(any(PaymentRecord.class), eq("openid_1"), anyString());
    }

    @Test
    @DisplayName("再次支付不重复扣库存：预占跟首次流水走")
    void secondPay_doesNotDeductStockAgain() {
        when(paymentRecordRepository.findByBizTypeAndBizIdAndStatus(PaymentBizType.ORDER, "ord_1", "pending"))
                .thenReturn(List.of(stalePending));

        orderService.payOrder("ord_1", wechatPay());

        verify(productStockService, never()).deductAll(any());
        verify(listingStockService, never()).deductForOrder(anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("旧流水实际已支付：直接提示到账中，不新建第二条流水")
    void secondPay_whenStaleActuallyPaid_throwsAndKeepsOrderUnpaid() {
        when(paymentRecordRepository.findByBizTypeAndBizIdAndStatus(PaymentBizType.ORDER, "ord_1", "pending"))
                .thenReturn(List.of(stalePending));
        org.mockito.Mockito.doThrow(new com.wuye.points.exception.BizException(
                        com.wuye.points.exception.ErrorCode.ORDER_CONFLICT,
                        "上一笔支付已成功，正在确认到账，请稍后刷新订单，无需重复支付"))
                .when(wechatPayService).closeForRetry(any(PaymentRecord.class), anyString(), anyString());

        com.wuye.points.exception.BizException ex = org.junit.jupiter.api.Assertions
                .assertThrows(com.wuye.points.exception.BizException.class,
                        () -> orderService.payOrder("ord_1", wechatPay()));

        assertEquals(com.wuye.points.exception.ErrorCode.ORDER_CONFLICT.getCode(), ex.getCode());
        verify(wechatPayService, never()).createIntent(anyString(), anyString(), anyString(), any(),
                anyString(), any(), any(Integer.class), any(), any());
    }
}
