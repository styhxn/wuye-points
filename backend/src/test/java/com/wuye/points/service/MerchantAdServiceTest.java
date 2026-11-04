package com.wuye.points.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.repository.AgeBracketConfigRepository;
import com.wuye.points.repository.MerchantAdQuotaRepository;
import com.wuye.points.repository.MerchantAdRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.ResidentRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 商家广告微信支付开关与报价字段回归。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MerchantAdServiceTest {

    @Mock
    private RoleGuard roleGuard;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private MerchantAdRepository merchantAdRepository;
    @Mock
    private MerchantAdQuotaRepository merchantAdQuotaRepository;
    @Mock
    private AgeBracketConfigRepository ageBracketConfigRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private PlatformSettingService platformSettingService;
    @Mock
    private WebSocketService webSocketService;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;
    @Mock
    private PayChannel wechatPayService;

    @InjectMocks
    private MerchantAdService merchantAdService;

    @BeforeEach
    void setUp() {
        UserContext.set(UserContext.builder()
                .userId("res_merchant01")
                .role("merchant")
                .propertyCompanyId("pc_1")
                .build());

        Resident merchantUser = new Resident();
        merchantUser.setId("res_merchant01");
        merchantUser.setPhone("13800000001");
        merchantUser.setPropertyCompanyId("pc_1");
        merchantUser.setRole("merchant");
        merchantUser.setStatus("active");

        Merchant merchant = new Merchant();
        merchant.setId("mch_1");
        merchant.setContactPhone("13800000001");
        merchant.setPropertyCompanyId("pc_1");

        when(residentRepository.findById("res_merchant01")).thenReturn(Optional.of(merchantUser));
        when(merchantRepository.findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                "13800000001", "pc_1")).thenReturn(Optional.of(merchant));
        when(platformSettingService.isAdFreeQuotaEnabled()).thenReturn(false);
        when(platformSettingService.getAdDayPrice()).thenReturn(new BigDecimal("10.00"));
        when(merchantAdQuotaRepository.sumFreeUsedCountByMerchantId(anyString())).thenReturn(0L);
        when(idGenerator.generateId(anyString())).thenReturn("id_test");
        when(wechatPayService.isEnabled()).thenReturn(true);
        when(wechatPayService.isDirectEnabled()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("广告报价返回微信支付可用标志（开启）")
    void quote_exposesWechatPaymentEnabled_whenEnabled() {
        ReflectionTestUtils.setField(merchantAdService, "adWechatPaymentEnabled", true);

        var quote = merchantAdService.quote(3);

        assertEquals(3, quote.getDurationDays());
        assertEquals(new BigDecimal("30.00"), quote.getPayAmount());
        assertEquals(true, quote.getWechatPaymentEnabled());
    }

    @Test
    @DisplayName("广告报价返回微信支付可用标志（关闭）")
    void quote_exposesWechatPaymentEnabled_whenDisabled() {
        ReflectionTestUtils.setField(merchantAdService, "adWechatPaymentEnabled", false);

        var quote = merchantAdService.quote(1);

        assertEquals(new BigDecimal("10.00"), quote.getPayAmount());
        assertEquals(false, quote.getWechatPaymentEnabled());
    }
}
