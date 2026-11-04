package com.wuye.points.service;

import com.wuye.points.dto.propertycompany.AdminAccountResponse;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.PasswordUtil;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PropertyCompanyService 管理员密码重置测试")
class PropertyCompanyPasswordResetTest {

    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private PropertyCompanyConverter propertyCompanyConverter;
    @Mock
    private RoleGuard roleGuard;

    @InjectMocks
    private PropertyCompanyService propertyCompanyService;

    private PropertyCompany company() {
        PropertyCompany company = new PropertyCompany();
        company.setId("pc_001");
        company.setName("测试物业");
        return company;
    }

    private Resident propertyAdmin() {
        Resident admin = new Resident();
        admin.setId("res_admin");
        admin.setRole("property_admin");
        admin.setPropertyCompanyId("pc_001");
        admin.setTokenVersion(2);
        admin.setPassword(PasswordUtil.hashPassword("old-password"));
        return admin;
    }

    @Test
    @DisplayName("平台管理员可重置物业管理员密码，旧登录态失效且新密码仅返回一次")
    void platformAdmin_canResetPropertyAdminPassword() {
        Resident admin = propertyAdmin();
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company()));
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(admin));
        when(propertyCompanyConverter.toAdminAccount(eq(admin), isNull(), anyString()))
                .thenAnswer(invocation -> AdminAccountResponse.builder()
                        .id(admin.getId())
                        .role(admin.getRole())
                        .initialPassword(invocation.getArgument(2))
                        .build());

        AdminAccountResponse response =
                propertyCompanyService.resetPropertyAdminPassword("pc_001", "res_admin");

        assertNotNull(response.getInitialPassword());
        assertTrue(PasswordUtil.checkPassword(response.getInitialPassword(), admin.getPassword()));
        assertEquals(3, admin.getTokenVersion());
        verify(residentRepository).save(admin);
    }

    @Test
    @DisplayName("非平台管理员不能重置物业管理员密码")
    void nonPlatformAdmin_cannotResetPassword() {
        doThrow(new BizException(ErrorCode.PERMISSION_DENIED))
                .when(roleGuard).requireAnyRole("platform_admin");

        BizException ex = assertThrows(BizException.class,
                () -> propertyCompanyService.resetPropertyAdminPassword("pc_001", "res_admin"));

        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
        verify(residentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("不能重置其他物业的管理员密码")
    void cannotResetAdminOfAnotherProperty() {
        Resident admin = propertyAdmin();
        admin.setPropertyCompanyId("pc_002");
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company()));
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(admin));

        BizException ex = assertThrows(BizException.class,
                () -> propertyCompanyService.resetPropertyAdminPassword("pc_001", "res_admin"));

        assertEquals(ErrorCode.RESIDENT_NOT_FOUND.getCode(), ex.getCode());
        verify(residentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
