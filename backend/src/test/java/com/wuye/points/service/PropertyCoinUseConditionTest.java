package com.wuye.points.service;

import com.wuye.points.enums.CoinType;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoinFreezeRecordRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * §79.2「消费物业币时校验使用条件」：条件此前只在 GET /property-coins/use-condition 里算，
 * 下单抵扣不校验，物业设了门槛也照样能花。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("物业币使用条件消费校验")
class PropertyCoinUseConditionTest {

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

    @InjectMocks
    private PropertyCoinService propertyCoinService;

    private Resident resident(String pointBalance) {
        Resident resident = new Resident();
        resident.setId("res_1");
        resident.setPropertyCompanyId("pc_1");
        resident.setPointBalance(pointBalance == null ? 0 : Integer.parseInt(pointBalance));
        resident.setCoinBalance(new BigDecimal("100.00"));
        return resident;
    }

    private void givenCondition(String condition, Integer pointThreshold) {
        PropertyCompany company = new PropertyCompany();
        company.setId("pc_1");
        company.setCoinUseCondition(condition);
        company.setCoinPointThreshold(pointThreshold);
        when(propertyCompanyRepository.findById("pc_1")).thenReturn(Optional.of(company));
    }

    @Test
    @DisplayName("未配置条件（none）：照常可抵扣")
    void noneCondition_allowsSpend() {
        givenCondition(null, null);

        propertyCoinService.assertUseConditionSatisfied(resident("10"));
        assertTrue(propertyCoinService.isUseConditionSatisfied(resident("0")));
    }

    @Test
    @DisplayName("积分门槛未达：拒绝消费")
    void pointThreshold_belowThreshold_rejects() {
        givenCondition("point_threshold", 1000);

        BizException ex = assertThrows(BizException.class,
                () -> propertyCoinService.assertUseConditionSatisfied(resident("500")));

        assertEquals(ErrorCode.COIN_USE_CONDITION_NOT_SATISFIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("积分门槛已达：放行")
    void pointThreshold_reached_allows() {
        givenCondition("point_threshold", 1000);

        propertyCoinService.assertUseConditionSatisfied(resident("1200"));
    }

    @Test
    @DisplayName("要求物业费缴清但仍有欠费：拒绝消费")
    void propertyFeePaid_withArrears_rejects() {
        givenCondition("property_fee_paid", 0);
        when(propertyFeeRepository.countByResidentIdAndPaymentStatusNotIn(anyString()))
                .thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> propertyCoinService.assertUseConditionSatisfied(resident("0")));

        assertEquals(ErrorCode.COIN_USE_CONDITION_NOT_SATISFIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("仅限物业配发的物业币：无配发流水则拒绝")
    void manualIssue_withoutGrant_rejects() {
        givenCondition("manual_issue", 0);
        when(coinRecordRepository.existsByResidentIdAndSourceAndCoinType(
                eq("res_1"), eq("manual"), eq(CoinType.earn.name()))).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> propertyCoinService.assertUseConditionSatisfied(resident("0")));

        assertEquals(ErrorCode.COIN_USE_CONDITION_NOT_SATISFIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("脏条件值按 none 处理，不放大成 500")
    void unknownCondition_fallsBackToNone() {
        givenCondition("not_a_condition", 0);

        assertTrue(propertyCoinService.isUseConditionSatisfied(resident("0")));
    }
}
