package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.propertycompany.CoinDisplaySettingResponse;
import com.wuye.points.dto.propertycompany.UpdateCoinDisplayRequest;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.AfterEach;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("物业币显示开关测试")
class PropertyCompanyCoinDisplayTest {

    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private PermissionGuard permissionGuard;

    @InjectMocks
    private PropertyCompanyService service;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("物业管理员可关闭本物业的物业币显示")
    void propertyAdmin_ownProperty_succeeds() {
        currentUser("res_admin", "property_admin", "pc_001");
        PropertyCompany company = company("pc_001");
        when(propertyCompanyRepository.findById("pc_001")).thenReturn(Optional.of(company));
        when(roleGuard.isPlatformAdmin()).thenReturn(false);

        CoinDisplaySettingResponse response = service.updateCoinDisplay("pc_001", request(false));

        verify(permissionGuard).requirePermission("coin.display_control");
        assertFalse(response.getCoinDisplayEnabled());
        assertFalse(company.getCoinDisplayEnabled());
    }

    @Test
    @DisplayName("标签身份只能操作本物业")
    void tagIdentity_otherProperty_isDenied() {
        currentUser("res_leader", "resident", "pc_002");
        when(propertyCompanyRepository.findById("pc_001"))
                .thenReturn(Optional.of(company("pc_001")));
        when(roleGuard.isPlatformAdmin()).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.updateCoinDisplay("pc_001", request(false)));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("平台管理员可操作任意物业")
    void platformAdmin_anyProperty_succeeds() {
        currentUser("res_platform", "platform_admin", "pc_001");
        PropertyCompany company = company("pc_002");
        when(propertyCompanyRepository.findById("pc_002")).thenReturn(Optional.of(company));
        when(roleGuard.isPlatformAdmin()).thenReturn(true);

        CoinDisplaySettingResponse response = service.updateCoinDisplay("pc_002", request(true));

        assertEquals("pc_002", response.getPropertyCompanyId());
        assertEquals(Boolean.TRUE, company.getCoinDisplayEnabled());
    }

    private void currentUser(String userId, String role, String propertyCompanyId) {
        UserContext.set(UserContext.builder()
                .userId(userId)
                .role(role)
                .propertyCompanyId(propertyCompanyId)
                .build());
    }

    private PropertyCompany company(String id) {
        PropertyCompany company = new PropertyCompany();
        company.setId(id);
        company.setCoinDisplayEnabled(Boolean.TRUE);
        return company;
    }

    private UpdateCoinDisplayRequest request(boolean enabled) {
        UpdateCoinDisplayRequest request = new UpdateCoinDisplayRequest();
        request.setEnabled(enabled);
        return request;
    }
}
