package com.wuye.points.service;

import com.wuye.points.dto.coinwithdrawal.ApplyCoinWithdrawalRequest;
import com.wuye.points.dto.coinwithdrawal.CoinWithdrawalItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoinWithdrawalRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.MerchantSupport;
import com.wuye.points.util.SnowflakeIdGenerator;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 物业币兑换（提现）申请单元测试（Mockito，不加载 Spring 上下文）。
 * 聚焦兑换金额/手续费/实到金额计算与风控（冻结、重复申请）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CoinWithdrawalServiceTest {

    @Mock
    private CoinWithdrawalRepository coinWithdrawalRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private PropertyCoinService propertyCoinService;
    @Mock
    private MerchantSupport merchantSupport;
    @Mock
    private PlatformSettingService platformSetting;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private CoinWithdrawalService coinWithdrawalService;

    private Merchant merchant;
    private Resident merchantUser;
    private PropertyCompany propertyCompany;

    @BeforeEach
    void setUp() {
        merchant = new Merchant();
        merchant.setId("m1");
        merchant.setPropertyCompanyId("pc1");

        merchantUser = new Resident();
        merchantUser.setId("u1");

        propertyCompany = new PropertyCompany();

        when(merchantSupport.requireCurrentMerchant()).thenReturn(merchant);
        when(merchantSupport.requireCurrentMerchantUser()).thenReturn(merchantUser);
        when(coinWithdrawalRepository.existsByMerchantIdAndAuditStatus("m1", "pending")).thenReturn(false);
        when(propertyCompanyRepository.findById("pc1")).thenReturn(java.util.Optional.of(propertyCompany));
        when(idGenerator.generateId("cw")).thenReturn("cw_testid");
        // 平台提现手续费解析：物业自定义费率优先，未设置走全局默认 0.006
        when(platformSetting.resolveWithdrawalFeeRate(any(), anyString())).thenAnswer(inv -> {
            BigDecimal override = inv.getArgument(0);
            return override != null ? override : new BigDecimal("0.0060");
        });
    }

    private void assertAmount(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }

    @Test
    @DisplayName("apply: 默认费率 0.0060，手续费/实到金额计算正确")
    void apply_defaultFeeRate() {
        ApplyCoinWithdrawalRequest req = new ApplyCoinWithdrawalRequest();
        req.setCoinAmount(new BigDecimal("100"));

        CoinWithdrawalItemResponse resp = coinWithdrawalService.apply(req);

        assertAmount(new BigDecimal("100"), resp.getCashAmount());
        assertAmount(new BigDecimal("0.60"), resp.getFeeAmount());
        assertAmount(new BigDecimal("99.40"), resp.getActualAmount());
        assertEquals("pending", resp.getAuditStatus());
    }

    @Test
    @DisplayName("apply: 自定义费率 0.01，手续费=1.00，实到=99.00")
    void apply_customFeeRate() {
        propertyCompany.setWithdrawalFeeRate(new BigDecimal("0.0100"));
        ApplyCoinWithdrawalRequest req = new ApplyCoinWithdrawalRequest();
        req.setCoinAmount(new BigDecimal("100"));

        CoinWithdrawalItemResponse resp = coinWithdrawalService.apply(req);

        assertAmount(new BigDecimal("1.00"), resp.getFeeAmount());
        assertAmount(new BigDecimal("99.00"), resp.getActualAmount());
    }

    @Test
    @DisplayName("apply: 存在待审核申请应抛 MERCHANT_WITHDRAWAL_INVALID")
    void apply_pendingExists() {
        when(coinWithdrawalRepository.existsByMerchantIdAndAuditStatus("m1", "pending")).thenReturn(true);
        ApplyCoinWithdrawalRequest req = new ApplyCoinWithdrawalRequest();
        req.setCoinAmount(new BigDecimal("100"));

        BizException ex = assertThrows(BizException.class, () -> coinWithdrawalService.apply(req));
        assertEquals(ErrorCode.MERCHANT_WITHDRAWAL_INVALID.getCode(), ex.getCode());
    }
}
