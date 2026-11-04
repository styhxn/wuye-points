package com.wuye.points.service;

import com.wuye.points.dto.coinfund.TransferPropertyCoinFundRequest;
import com.wuye.points.entity.MerchantWithdrawal;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyCoinFundAccount;
import com.wuye.points.entity.PropertyCoinFundLedger;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.repository.PropertyCoinFundAccountRepository;
import com.wuye.points.repository.PropertyCoinFundLedgerRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("物业币现金资金归属台账")
class PropertyCoinFundServiceTest {

    @Mock
    private PropertyCoinFundAccountRepository accountRepository;
    @Mock
    private PropertyCoinFundLedgerRepository ledgerRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private RoleGuard roleGuard;

    @InjectMocks
    private PropertyCoinFundService service;

    private PropertyCoinFundAccount account;

    @BeforeEach
    void setUp() {
        account = new PropertyCoinFundAccount();
        account.setPropertyCompanyId("pc_1");
        account.setBalance(BigDecimal.ZERO);
        account.setTotalIn(BigDecimal.ZERO);
        account.setTotalOut(BigDecimal.ZERO);
        when(accountRepository.findByIdForUpdate("pc_1")).thenReturn(Optional.of(account));
        when(idGenerator.generateId(anyString())).thenReturn("pcf_test");
        when(ledgerRepository.findByBizTypeAndBizIdAndEntryType(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("充值到账：记一笔 credit，账户余额增加")
    void recordRecharge_credits() {
        PaymentRecord record = new PaymentRecord();
        record.setId("pay_1");
        record.setPropertyCompanyId("pc_1");
        record.setResidentId("res_1");
        record.setAmount(new BigDecimal("100.00"));

        service.recordRecharge(record);

        ArgumentCaptor<PropertyCoinFundLedger> captor =
                ArgumentCaptor.forClass(PropertyCoinFundLedger.class);
        verify(ledgerRepository).save(captor.capture());
        assertEquals("recharge_in", captor.getValue().getEntryType());
        assertEquals("credit", captor.getValue().getDirection());
        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));
        assertEquals(0, new BigDecimal("100.00").compareTo(account.getTotalIn()));
    }

    @Test
    @DisplayName("充值重复回调：唯一键命中后不再重复记账")
    void recordRecharge_idempotent() {
        PaymentRecord record = new PaymentRecord();
        record.setId("pay_1");
        record.setPropertyCompanyId("pc_1");
        record.setAmount(new BigDecimal("100.00"));
        when(ledgerRepository.findByBizTypeAndBizIdAndEntryType(
                "coin_recharge", "pay_1", "recharge_in"))
                .thenReturn(Optional.of(new PropertyCoinFundLedger()));

        service.recordRecharge(record);

        verify(ledgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("商家物业币结算完成：记一笔 debit，按提现单全额出账")
    void recordMerchantSettlement_debitsActualAmount() {
        account.setBalance(new BigDecimal("100.00"));
        MerchantWithdrawal withdrawal = new MerchantWithdrawal();
        withdrawal.setId("mw_1");
        withdrawal.setPropertyCompanyId("pc_1");
        withdrawal.setMerchantId("mch_1");
        withdrawal.setAmount(new BigDecimal("50.00"));
        withdrawal.setActualAmount(new BigDecimal("49.70"));

        service.recordMerchantSettlement(withdrawal);

        assertEquals(0, new BigDecimal("50.00").compareTo(account.getBalance()));
        assertEquals(0, new BigDecimal("50.00").compareTo(account.getTotalOut()));
        ArgumentCaptor<PropertyCoinFundLedger> captor =
                ArgumentCaptor.forClass(PropertyCoinFundLedger.class);
        verify(ledgerRepository).save(captor.capture());
        assertEquals("merchant_settlement", captor.getValue().getEntryType());
        assertEquals("debit", captor.getValue().getDirection());
    }

    @Test
    @DisplayName("平台转物业：余额不足时拒绝")
    void transfer_insufficient() {
        when(roleGuard.isPlatformAdmin()).thenReturn(true);
        account.setBalance(new BigDecimal("10.00"));
        PropertyCompany company = new PropertyCompany();
        company.setId("pc_1");
        when(propertyCompanyRepository.findById("pc_1")).thenReturn(Optional.of(company));

        TransferPropertyCoinFundRequest request = new TransferPropertyCoinFundRequest();
        request.setPropertyCompanyId("pc_1");
        request.setAmount(new BigDecimal("20.00"));

        assertThrows(BizException.class, () -> service.transferToProperty(request));
        verify(ledgerRepository, never()).save(any());
    }
}
