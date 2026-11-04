package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantPointPurchaseRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MerchantService /merchants/my 工作台字段")
class MerchantServiceTotalRevenueTest {

    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private PlatformMerchantRepository platformMerchantRepository;
    @Mock
    private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private IndividualLeaderRepository individualLeaderRepository;
    @Mock
    private MerchantConverter merchantConverter;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private WithdrawableService withdrawableService;
    @Mock
    private UnifiedEarningsService unifiedEarningsService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MerchantPointPurchaseRepository merchantPointPurchaseRepository;
    @Mock
    private MerchantCoinReceivableService merchantCoinReceivableService;

    @InjectMocks
    private MerchantService merchantService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("getMyMerchant 下发 totalRevenue / totalOrders / pointBalance")
    void getMyMerchant_populatesDashboardFields() {
        UserContext.set(UserContext.builder()
                .userId("res_owner")
                .role("merchant")
                .propertyCompanyId("pc_001")
                .build());

        Resident owner = new Resident();
        owner.setId("res_owner");
        owner.setPhone("13800138000");
        owner.setPropertyCompanyId("pc_001");

        Merchant merchant = new Merchant();
        merchant.setId("mch_001");
        merchant.setContactPhone("13800138000");
        merchant.setPropertyCompanyId("pc_001");
        merchant.setResidentId("res_owner");

        MerchantDetailResponse detail = MerchantDetailResponse.builder()
                .id("mch_001")
                .build();

        when(residentRepository.findById("res_owner")).thenReturn(Optional.of(owner));
        when(merchantRepository.findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                "13800138000", "pc_001")).thenReturn(Optional.of(merchant));
        when(merchantPropertyConfigRepository.findByMerchantIdAndPropertyCompanyId(
                "mch_001", "pc_001")).thenReturn(Optional.empty());
        when(productRepository.findByMerchantId("mch_001")).thenReturn(List.of());
        when(merchantConverter.toDetail(eq(merchant), isNull(), anyList(), eq(true))).thenReturn(detail);
        when(withdrawableService.calcMerchantWithdrawable(merchant)).thenReturn(
                new WithdrawableService.MerchantWithdrawable(
                        new BigDecimal("1000.00"),
                        BigDecimal.ZERO,
                        false,
                        new BigDecimal("12500.00"),
                        30L,
                        new BigDecimal("2500.00"),
                        BigDecimal.ZERO));
        when(unifiedEarningsService.calc("res_owner")).thenReturn(
                new UnifiedEarningsService.UnifiedEarnings(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        when(orderRepository.countAllByMerchantId("mch_001")).thenReturn(128L);
        when(merchantPointPurchaseRepository.sumRemainingPointsByMerchantId("mch_001")).thenReturn(5000L);
        when(merchantCoinReceivableService.isInternalEarningsMerchant(merchant)).thenReturn(false);
        when(merchantCoinReceivableService.calc(merchant)).thenReturn(
                new MerchantCoinReceivableService.MerchantIncome(
                        new BigDecimal("12500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of()));

        MerchantDetailResponse result = merchantService.getMyMerchant();

        assertEquals(new BigDecimal("12500.00"), result.getTotalRevenue());
        assertEquals(128L, result.getTotalOrders());
        assertEquals(5000, result.getPointBalance());
    }
}
