package com.wuye.points.service;

import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.RoleGuard;
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

/**
 * 物业币商城规则回归（A8 服务端校验依赖）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyCoinMallRuleServiceTest {

    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private RoleGuard roleGuard;

    @InjectMocks
    private PropertyCoinMallRuleService service;

    private PropertyCompany company(boolean enabled, String ratio, String minAmount) {
        PropertyCompany company = new PropertyCompany();
        company.setCoinMallEnabled(enabled);
        company.setCoinMallMaxRatio(new BigDecimal(ratio));
        company.setCoinMallMinAmount(new BigDecimal(minAmount));
        return company;
    }

    @Test
    @DisplayName("未开启物业币抵扣 → COIN_MALL_DISABLED")
    void disabled() {
        BizException ex = assertThrows(BizException.class, () -> service.assertCoinAllowed(
                company(false, "1.0", "0.01"), new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("100")));
        assertEquals(ErrorCode.COIN_MALL_DISABLED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("订单金额低于最低使用金额 → COIN_BELOW_MIN_AMOUNT")
    void belowMinAmount() {
        BizException ex = assertThrows(BizException.class, () -> service.assertCoinAllowed(
                company(true, "1.0", "50"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("100")));
        assertEquals(ErrorCode.COIN_BELOW_MIN_AMOUNT.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("超过最大抵扣比例 → COIN_EXCEED_MAX_RATIO")
    void exceedRatio() {
        BizException ex = assertThrows(BizException.class, () -> service.assertCoinAllowed(
                company(true, "0.5", "0.01"), new BigDecimal("100"), new BigDecimal("60"), new BigDecimal("100")));
        assertEquals(ErrorCode.COIN_EXCEED_MAX_RATIO.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("超过真实余额 → COIN_INSUFFICIENT")
    void exceedBalance() {
        BizException ex = assertThrows(BizException.class, () -> service.assertCoinAllowed(
                company(true, "1.0", "0.01"), new BigDecimal("100"), new BigDecimal("60"), new BigDecimal("50")));
        assertEquals(ErrorCode.COIN_INSUFFICIENT.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("规则内金额通过并返回归一化值")
    void allowed() {
        BigDecimal used = service.assertCoinAllowed(
                company(true, "0.5", "0.01"), new BigDecimal("100"), new BigDecimal("40"), new BigDecimal("50"));
        assertEquals(0, new BigDecimal("40.00").compareTo(used));
    }
}
