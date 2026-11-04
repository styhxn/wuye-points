package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.resident.MyCoinsResponse;
import com.wuye.points.dto.resident.MyPointsResponse;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.BuildingChangeRequestRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("住户端功能开关下发测试")
class ResidentFeatureSwitchTest {

    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PointRecordRepository pointRecordRepository;
    @Mock
    private PropertyCoinRecordRepository propertyCoinRecordRepository;
    @Mock
    private ResidentConverter residentConverter;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private PropertyFeeRepository propertyFeeRepository;
    @Mock
    private WebSocketService webSocketService;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private BuildingChangeRequestRepository buildingChangeRequestRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private ResidentIdentityTags residentIdentityTags;

    @InjectMocks
    private ResidentService service;

    @BeforeEach
    void setUp() {
        UserContext.set(UserContext.builder()
                .userId("res_001")
                .role("resident")
                .propertyCompanyId("pc_001")
                .build());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("我的积分返回本小区 pointEnabled=false")
    void myPoints_returnsPointEnabled() {
        Resident resident = resident();
        PropertyCompany company = company();
        company.setPointEnabled(false);
        when(residentRepository.findById("res_001")).thenReturn(Optional.of(resident));
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        when(pointRecordRepository.sumEarnedBetween(anyString(), any(), any())).thenReturn(0);
        when(pointRecordRepository.sumSpentBetween(anyString(), any(), any())).thenReturn(0);

        MyPointsResponse response = service.getMyPoints();

        assertFalse(response.getPointEnabled());
    }

    @Test
    @DisplayName("我的物业币返回本小区 coinEnabled=false")
    void myCoins_returnsCoinEnabled() {
        Resident resident = resident();
        PropertyCompany company = company();
        company.setCoinEnabled(false);
        company.setCoinDisplayEnabled(true);
        when(residentRepository.findById("res_001")).thenReturn(Optional.of(resident));
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        when(propertyCoinRecordRepository.sumEarnedBetween(anyString(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(propertyCoinRecordRepository.sumSpentBetween(anyString(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        MyCoinsResponse response = service.getMyCoins();

        assertFalse(response.getCoinEnabled());
        assertTrue(response.getCoinDisplayEnabled());
    }

    private Resident resident() {
        Resident resident = new Resident();
        resident.setId("res_001");
        resident.setPropertyCompanyId("pc_001");
        resident.setStatus("active");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        return resident;
    }

    private PropertyCompany company() {
        PropertyCompany company = new PropertyCompany();
        company.setId("pc_001");
        company.setStatus("active");
        return company;
    }
}
