package com.wuye.points.service;

import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.withdrawal.ApplyWithdrawalRequest;
import com.wuye.points.dto.merchant.withdrawal.WithdrawalItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantWithdrawal;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.repository.MerchantWithdrawalRepository;
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

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 商家现金提现申请/列表单元测试（Mockito，不加载 Spring 上下文）。
 * 聚焦金额计算（手续费/实到金额）与按角色定位提现店铺（merchant / activity_leader / technician）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MerchantWithdrawalServiceTest {

    @Mock
    private MerchantWithdrawalRepository merchantWithdrawalRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private MerchantSupport merchantSupport;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private MerchantWithdrawalService withdrawalService;

    private Merchant merchant;
    private PropertyCompany propertyCompany;

    @BeforeEach
    void setUp() {
        merchant = new Merchant();
        merchant.setId("m1");
        merchant.setPropertyCompanyId("pc1");
        merchant.setName("技工刘师傅店");

        propertyCompany = new PropertyCompany();

        when(merchantSupport.requireCurrentWithdrawableMerchant()).thenReturn(merchant);
        when(propertyCompanyRepository.findById("pc1")).thenReturn(Optional.of(propertyCompany));
        when(idGenerator.generateId("mw")).thenReturn("mw_testid");
        when(merchantWithdrawalRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
    }

    private void assertAmount(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }

    @Test
    @DisplayName("apply: 默认费率 0.0060，手续费与实到金额计算正确，状态为 pending，merchantName 已填充")
    void apply_defaultFeeRate() {
        ApplyWithdrawalRequest req = new ApplyWithdrawalRequest();
        req.setAmount(new BigDecimal("100"));

        WithdrawalItemResponse resp = withdrawalService.apply(req);

        assertAmount(new BigDecimal("0.60"), resp.getFeeAmount());
        assertAmount(new BigDecimal("99.40"), resp.getActualAmount());
        assertEquals("pending", resp.getStatus());
        assertEquals("m1", resp.getMerchantId());
        assertEquals("技工刘师傅店", resp.getMerchantName());
    }

    @Test
    @DisplayName("apply: 自定义费率 0.01，手续费=1.00，实到=99.00")
    void apply_customFeeRate() {
        propertyCompany.setWithdrawalFeeRate(new BigDecimal("0.0100"));

        ApplyWithdrawalRequest req = new ApplyWithdrawalRequest();
        req.setAmount(new BigDecimal("100"));

        WithdrawalItemResponse resp = withdrawalService.apply(req);

        assertAmount(new BigDecimal("1.00"), resp.getFeeAmount());
        assertAmount(new BigDecimal("99.00"), resp.getActualAmount());
    }

    @Test
    @DisplayName("listWithdrawals: 按当前店铺过滤并填充 merchantName")
    void listWithdrawals_withRecords() {
        MerchantWithdrawal w1 = new MerchantWithdrawal();
        w1.setId("mw1");
        w1.setMerchantId("m1");
        w1.setPropertyCompanyId("pc1");
        w1.setAmount(new BigDecimal("50"));
        w1.setFeeRate(new BigDecimal("0.0060"));
        w1.setFeeAmount(new BigDecimal("0.30"));
        w1.setActualAmount(new BigDecimal("49.70"));
        w1.setAuditStatus("pending");

        when(merchantWithdrawalRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(w1)));

        PageResponse<WithdrawalItemResponse> resp = withdrawalService.listWithdrawals(1, 20, null, null, null);

        assertEquals(1, resp.getList().size());
        WithdrawalItemResponse item = resp.getList().get(0);
        assertEquals("mw1", item.getId());
        assertEquals("技工刘师傅店", item.getMerchantName());
        assertEquals("pending", item.getStatus());
    }
}
