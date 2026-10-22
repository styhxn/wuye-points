package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.residentmerchant.ApplyResidentMerchantRequest;
import com.wuye.points.dto.residentmerchant.UpdateResidentMerchantShopRequest;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantApplication;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.ResidentMerchantApplicationRepository;
import com.wuye.points.repository.ResidentMerchantDepositRepository;
import com.wuye.points.repository.ResidentMerchantListingRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 业主小店店铺资料（需求1：对齐普通商家）单元测试。
 * 覆盖：申请时携带店铺资料、店主改店接口、my 状态回显。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ResidentShopService 店铺资料测试")
class ResidentShopServiceTest {

    @Mock
    private RoleGuard roleGuard;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private ResidentMerchantApplicationRepository applicationRepository;
    @Mock
    private ResidentMerchantDepositRepository depositRepository;
    @Mock
    private ResidentMerchantListingRepository listingRepository;
    @Mock
    private DistributorProductRepository distributorProductRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private PayChannel wechatPayService;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;
    @Mock
    private PlatformSettingService platformSettingService;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private ResidentShopMerchantService residentShopMerchantService;

    @InjectMocks
    private ResidentShopService residentShopService;

    private Resident resident;
    private ResidentMerchantApplication application;

    @BeforeEach
    void setUp() {
        resident = new Resident();
        resident.setId("res_dev001");
        resident.setName("张阿姨");
        resident.setRole("resident");
        resident.setUserType("owner");
        resident.setStatus("active");
        resident.setPropertyCompanyId("pc_demo001");
        resident.setPhone("18861350048");
        resident.setCoinBalance(new BigDecimal("49.19"));

        application = new ResidentMerchantApplication();
        application.setId("rma_001");
        application.setResidentId("res_dev001");
        application.setPropertyCompanyId("pc_demo001");
        application.setStatus("active");
        application.setVisibility("public");
        application.setDepositAmount(new BigDecimal("500.00"));
        application.setShopName("张阿姨的小店");

        when(idGenerator.generateId("rma")).thenReturn("rma_new001");
        UserContext.set(UserContext.builder()
                .userId("res_dev001")
                .role("resident")
                .propertyCompanyId("pc_demo001")
                .build());
        when(residentRepository.findById("res_dev001")).thenReturn(Optional.of(resident));
        when(residentRepository.findById("res_dev001")).thenReturn(Optional.of(resident));
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    @DisplayName("申请：店铺资料（简介/营业时间/联系电话/封面）随申请落库")
    void apply_shouldPersistShopProfile() {
        when(applicationRepository.findByResidentId("res_dev001")).thenReturn(Optional.empty());

        ApplyResidentMerchantRequest request = new ApplyResidentMerchantRequest();
        request.setAgreedTerms(true);
        request.setShopName("张阿姨的小店");
        request.setDescription("手工零食，产地直发");
        request.setBusinessHours("08:00-22:00");
        request.setContactPhone("13800138000");
        request.setCoverUrl("https://example.com/cover.jpg");

        var resp = residentShopService.apply(request);

        assertEquals("rma_new001", resp.getApplicationId());
        org.mockito.ArgumentCaptor<ResidentMerchantApplication> captor =
                org.mockito.ArgumentCaptor.forClass(ResidentMerchantApplication.class);
        verify(applicationRepository).save(captor.capture());
        ResidentMerchantApplication saved = captor.getValue();
        assertEquals("手工零食，产地直发", saved.getDescription());
        assertEquals("08:00-22:00", saved.getBusinessHours());
        assertEquals("13800138000", saved.getContactPhone());
        assertEquals("https://example.com/cover.jpg", saved.getCoverUrl());
        assertEquals("张阿姨的小店", saved.getShopName());
    }

    @Test
    @DisplayName("改店：更新资料并同步店铺主体")
    void updateShop_shouldUpdateAndSync() {
        when(applicationRepository.findByResidentId("res_dev001")).thenReturn(Optional.of(application));
        when(depositRepository.findByApplicationId("rma_001")).thenReturn(Optional.empty());
        when(listingRepository.countByResidentIdAndEnabledTrue("res_dev001")).thenReturn(3L);

        UpdateResidentMerchantShopRequest request = new UpdateResidentMerchantShopRequest();
        request.setShopName("阿姨的手作小铺");
        request.setDescription("现做糕点，每日限量");
        request.setBusinessHours("09:00-20:00");
        request.setContactPhone("13900139000");
        request.setCoverUrl("https://example.com/cover2.jpg");

        var resp = residentShopService.updateShop(request);

        assertEquals("阿姨的手作小铺", application.getShopName());
        assertEquals("现做糕点，每日限量", application.getDescription());
        verify(applicationRepository).save(application);
        verify(residentShopMerchantService).syncShopProfile(application);
        // myStatus 回显
        assertEquals("阿姨的手作小铺", resp.getShopName());
        assertEquals("现做糕点，每日限量", resp.getDescription());
        assertEquals("09:00-20:00", resp.getBusinessHours());
        assertEquals("13900139000", resp.getContactPhone());
        assertEquals("https://example.com/cover2.jpg", resp.getCoverUrl());
    }

    @Test
    @DisplayName("改店：未传字段不修改（部分更新）")
    void updateShop_shouldOnlyUpdateProvidedFields() {
        application.setDescription("原始简介");
        application.setBusinessHours("08:00-22:00");
        when(applicationRepository.findByResidentId("res_dev001")).thenReturn(Optional.of(application));
        when(depositRepository.findByApplicationId("rma_001")).thenReturn(Optional.empty());
        when(listingRepository.countByResidentIdAndEnabledTrue("res_dev001")).thenReturn(0L);

        UpdateResidentMerchantShopRequest request = new UpdateResidentMerchantShopRequest();
        request.setContactPhone("13700137000");

        residentShopService.updateShop(request);

        assertEquals("原始简介", application.getDescription());
        assertEquals("08:00-22:00", application.getBusinessHours());
        assertEquals("13700137000", application.getContactPhone());
    }
}
