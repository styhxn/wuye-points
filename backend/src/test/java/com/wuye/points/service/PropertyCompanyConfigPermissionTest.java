package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.propertycompany.PropertyCompanyDetailResponse;
import com.wuye.points.dto.propertycompany.UpdateCoinUseConditionRequest;
import com.wuye.points.dto.propertycompany.UpdatePropertyCompanyRequest;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.DeliveryRuleRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PointPoolRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("物业公司参数配置权限测试")
class PropertyCompanyConfigPermissionTest {

    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private PointPoolRepository pointPoolRepository;
    @Mock
    private DeliveryRuleRepository deliveryRuleRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PropertyCompanyConverter propertyCompanyConverter;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private PermissionGuard permissionGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private PropertyCompanyService service;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("物业领导可开关本物业积分/物业币功能")
    void propertyLeader_updatesFeatureSwitches() {
        currentUser("res_leader", "property_admin", "pc_001");
        PropertyCompany company = company();
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        stubDetail();

        UpdatePropertyCompanyRequest request = new UpdatePropertyCompanyRequest();
        request.setPointEnabled(false);
        request.setCoinEnabled(false);

        service.updatePropertyCompany("pc_001", request);

        verify(permissionGuard).requirePermission("config.set");
        assertFalse(company.getPointEnabled());
        assertFalse(company.getCoinEnabled());
        verify(propertyCompanyRepository).save(company);
    }

    @Test
    @DisplayName("物业领导不能直接修改物业分成比例")
    void propertyLeader_cannotUpdatePropertyShareRate() {
        currentUser("res_leader", "property_admin", "pc_001");
        PropertyCompany company = company();
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        stubDetail();

        UpdatePropertyCompanyRequest request = new UpdatePropertyCompanyRequest();
        request.setPropertyShareRate(new BigDecimal("0.5000"));

        BizException ex = assertThrows(BizException.class,
                () -> service.updatePropertyCompany("pc_001", request));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
        assertEquals(new BigDecimal("0.6000"), company.getPropertyShareRate());
    }

    @Test
    @DisplayName("物业领导不能直接修改提现手续费率")
    void propertyLeader_cannotUpdateWithdrawalFeeRate() {
        currentUser("res_leader", "property_admin", "pc_001");
        PropertyCompany company = company();
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        stubDetail();

        UpdatePropertyCompanyRequest request = new UpdatePropertyCompanyRequest();
        request.setWithdrawalFeeRate(new BigDecimal("0.0100"));

        BizException ex = assertThrows(BizException.class,
                () -> service.updatePropertyCompany("pc_001", request));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("平台管理员可修改一级三档分成比例")
    void platformAdmin_updatesShareRates() {
        currentUser("res_platform", "platform_admin", "pc_001");
        PropertyCompany company = company();
        when(roleGuard.isPlatformAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        stubDetail();

        UpdatePropertyCompanyRequest request = new UpdatePropertyCompanyRequest();
        request.setPropertyShareRate(new BigDecimal("0.6000"));
        request.setCoordinatorShareRate(new BigDecimal("0.3000"));

        service.updatePropertyCompany("pc_001", request);

        assertEquals(new BigDecimal("0.6000"), company.getPropertyShareRate());
        assertEquals(new BigDecimal("0.3000"), company.getCoordinatorShareRate());
        verify(propertyCompanyRepository).save(company);
    }

    @Test
    @DisplayName("物业币使用条件写入口要求 config:set")
    void coinUseCondition_requiresConfigSet() {
        currentUser("res_leader", "property_admin", "pc_001");
        PropertyCompany company = company();
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        stubDetail();

        UpdateCoinUseConditionRequest request = new UpdateCoinUseConditionRequest();
        request.setCondition("none");

        service.updateCoinUseCondition("pc_001", request);

        verify(permissionGuard).requirePermission("config.set");
    }

    @Test
    @DisplayName("详情返回 pointEnabled/coinEnabled，空值按开启处理")
    void detail_containsFeatureSwitches() {
        PropertyCompanyConverter converter = new PropertyCompanyConverter();

        PropertyCompany disabled = company();
        disabled.setPointEnabled(false);
        disabled.setCoinEnabled(false);
        PropertyCompanyDetailResponse disabledDetail = converter.toDetail(disabled, List.of(), List.of());
        assertFalse(disabledDetail.getPointEnabled());
        assertFalse(disabledDetail.getCoinEnabled());

        PropertyCompany defaulted = new PropertyCompany();
        defaulted.setId("pc_002");
        PropertyCompanyDetailResponse defaultedDetail = converter.toDetail(defaulted, List.of(), List.of());
        assertTrue(defaultedDetail.getPointEnabled());
        assertTrue(defaultedDetail.getCoinEnabled());
    }

    private void stubDetail() {
        when(communityRepository.findByPropertyCompanyId(anyString())).thenReturn(List.of());
        when(residentRepository.findByPropertyCompanyIdAndRole(anyString(), anyString())).thenReturn(List.of());
        when(propertyCompanyConverter.toDetail(any(), any(), any()))
                .thenReturn(PropertyCompanyDetailResponse.builder().build());
    }

    private void currentUser(String userId, String role, String propertyCompanyId) {
        UserContext.set(UserContext.builder()
                .userId(userId)
                .role(role)
                .propertyCompanyId(propertyCompanyId)
                .build());
    }

    @Test
    @DisplayName("V36 用户侧两档合计超过 100% 时拒绝：否则平台盘被清零、全额由物业兜底")
    void platformAdmin_rejectsUserTierTotalAboveOneHundredPercent() {
        currentUser("res_platform", "platform_admin", "pc_001");
        PropertyCompany company = company();
        when(roleGuard.isPlatformAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        stubDetail();

        UpdatePropertyCompanyRequest request = new UpdatePropertyCompanyRequest();
        request.setUserPointShareRate(new BigDecimal("0.6000"));
        request.setUserRewardShareRate(new BigDecimal("0.6000"));

        BizException ex = assertThrows(BizException.class,
                () -> service.updatePropertyCompany("pc_001", request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("合计不得超过100%"), ex.getMessage());
    }

    @Test
    @DisplayName("V36 用户侧比例可配 0（表示不发），不被当成「未配置」回落成 10%")
    void platformAdmin_canSetUserTierRateToZero() {
        currentUser("res_platform", "platform_admin", "pc_001");
        PropertyCompany company = company();
        when(roleGuard.isPlatformAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        stubDetail();

        UpdatePropertyCompanyRequest request = new UpdatePropertyCompanyRequest();
        request.setUserPointShareRate(BigDecimal.ZERO);
        request.setUserRewardShareRate(new BigDecimal("0.1000"));

        service.updatePropertyCompany("pc_001", request);

        assertEquals(0, BigDecimal.ZERO.compareTo(company.getUserPointShareRate()));
        assertEquals(new BigDecimal("0.1000"), company.getUserRewardShareRate());
    }

    @Test
    @DisplayName("V36 物业领导不能修改用户侧预算比例")
    void propertyLeader_cannotUpdateUserTierRates() {
        currentUser("res_leader", "property_admin", "pc_001");
        PropertyCompany company = company();
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        stubDetail();

        UpdatePropertyCompanyRequest request = new UpdatePropertyCompanyRequest();
        request.setUserRewardShareRate(new BigDecimal("0.3000"));

        BizException ex = assertThrows(BizException.class,
                () -> service.updatePropertyCompany("pc_001", request));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    private PropertyCompany company() {
        PropertyCompany company = new PropertyCompany();
        company.setId("pc_001");
        company.setStatus("active");
        company.setPlatformShareRate(new BigDecimal("0.1000"));
        company.setPropertyShareRate(new BigDecimal("0.6000"));
        company.setCoordinatorShareRate(new BigDecimal("0.3000"));
        company.setPointEnabled(true);
        company.setCoinEnabled(true);
        return company;
    }
}
