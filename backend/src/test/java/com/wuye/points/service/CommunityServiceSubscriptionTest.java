package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.service.SubscribeServiceResponse;
import com.wuye.points.entity.CommunityService;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ServiceSubscription;
import com.wuye.points.exception.BizException;
import com.wuye.points.repository.CommunityServiceRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.ServiceSubscriptionRepository;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.AfterEach;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 服务订阅的取消与到期：
 * 线上同一 (service, resident) 存在多条 active 时，单实体查询会抛 IncorrectResultSize 变成 500，
 * 而过期行不落 expired 是这些重复行的来源。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("服务订阅取消与到期")
class CommunityServiceSubscriptionTest {

    @Mock
    private RoleGuard roleGuard;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private CommunityServiceRepository communityServiceRepository;
    @Mock
    private ServiceSubscriptionRepository serviceSubscriptionRepository;

    @InjectMocks
    private CommunityServiceService communityServiceService;

    private Resident resident;
    private CommunityService service;

    private ServiceSubscription subscription(String id, LocalDate endDate, String status) {
        ServiceSubscription sub = new ServiceSubscription();
        sub.setId(id);
        sub.setServiceId("svc_004");
        sub.setResidentId("res_dev001");
        sub.setPropertyCompanyId("pc_demo001");
        sub.setSubType("monthly");
        sub.setAmount(new BigDecimal("12.00"));
        sub.setStartDate(endDate.minusMonths(1));
        sub.setEndDate(endDate);
        sub.setStatus(status);
        sub.setAutoRenew(false);
        sub.setCreatedAt(LocalDateTime.now());
        return sub;
    }

    @BeforeEach
    void setUp() {
        resident = new Resident();
        resident.setId("res_dev001");
        resident.setName("张阿姨");
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPropertyCompanyId("pc_demo001");

        service = new CommunityService();
        service.setId("svc_004");
        service.setName("社区食堂午餐");
        service.setStatus("active");
        service.setProviderId("res_mch01");
        service.setPropertyCompanyId("pc_demo001");
        service.setIsSubscription(true);

        when(residentRepository.findById("res_dev001")).thenReturn(Optional.of(resident));
        when(communityServiceRepository.findById("svc_004")).thenReturn(Optional.of(service));
        UserContext.set(UserContext.builder()
                .userId("res_dev001")
                .role("resident")
                .propertyCompanyId("pc_demo001")
                .build());
    }

    @AfterEach
    void cleanUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("两条 active 订阅一起取消，不再抛 IncorrectResultSize")
    void unsubscribe_cancelsEveryActiveRow() {
        List<ServiceSubscription> actives = List.of(
                subscription("ss_3545b8e48e1000", LocalDate.of(2026, 9, 17), "active"),
                subscription("ss_380dd019181000", LocalDate.of(2026, 11, 25), "active"));
        when(serviceSubscriptionRepository.findAllByServiceIdAndResidentIdAndStatus("svc_004", "res_dev001", "active"))
                .thenReturn(actives);

        SubscribeServiceResponse response = communityServiceService.unsubscribe("svc_004");

        assertEquals("cancelled", response.getStatus());
        assertEquals(2, actives.size());
        assertTrue(actives.stream().allMatch(s -> "cancelled".equals(s.getStatus())));
        verify(serviceSubscriptionRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("没有有效订阅时给业务提示，不是 500")
    void unsubscribe_withoutActive_throwsBiz() {
        when(serviceSubscriptionRepository.findAllByServiceIdAndResidentIdAndStatus("svc_004", "res_dev001", "active"))
                .thenReturn(List.of());

        BizException ex = assertThrows(BizException.class, () -> communityServiceService.unsubscribe("svc_004"));

        assertTrue(ex.getMessage().contains("未找到有效订阅"));
        verify(serviceSubscriptionRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("到期兜底把过期 active 批量置 expired")
    void expireOverdue_callsBulkUpdate() {
        when(serviceSubscriptionRepository.expireOverdue(any(LocalDate.class))).thenReturn(3);

        int expired = communityServiceService.expireOverdueSubscriptions();

        assertEquals(3, expired);
        verify(serviceSubscriptionRepository).expireOverdue(eq(LocalDate.now()));
    }
}
