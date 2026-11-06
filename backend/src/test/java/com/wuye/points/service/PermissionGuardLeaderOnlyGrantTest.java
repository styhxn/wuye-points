package com.wuye.points.service;

import com.wuye.points.entity.Permission;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.UserPermission;
import com.wuye.points.repository.PermissionRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.RolePresetPermissionRepository;
import com.wuye.points.repository.RolePresetRepository;
import com.wuye.points.repository.UserPermissionRepository;
import com.wuye.points.util.LeaderIdentityGuard;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 领导专属权限码的下发口径必须与接口放行口径一致：
 * hasPermission 认用户级授权，/auth/profile 的 effectivePermissions 就不能把它抹掉，
 * 否则表现为「权限配置里赋予了，菜单里还是看不到」。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("领导专属权限下发一致性")
class PermissionGuardLeaderOnlyGrantTest {

    private static final String USER_ID = "res_operator01";
    private static final String PC = "pc_demo001";

    @Mock
    private RoleGuard roleGuard;
    @Mock
    private LeaderIdentityGuard leaderIdentityGuard;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserPermissionRepository userPermissionRepository;
    @Mock
    private RolePresetRepository rolePresetRepository;
    @Mock
    private RolePresetPermissionRepository rolePresetPermissionRepository;
    @Mock
    private ResidentRepository residentRepository;

    private PermissionGuard guard;

    @BeforeEach
    void setUp() {
        guard = new PermissionGuard(roleGuard, leaderIdentityGuard, permissionRepository,
                userPermissionRepository, rolePresetRepository, rolePresetPermissionRepository,
                residentRepository);
        givenPermissions(permission("perm_016", "config.set"),
                permission("perm_021", "permission.read"),
                permission("perm_030", "user.manage"),
                permission("perm_022", "merchant.read"));
        when(leaderIdentityGuard.leaderRoleCodes(anyString(), any())).thenReturn(Set.of());
    }

    private Permission permission(String id, String code) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setName(code);
        permission.setStatus("active");
        return permission;
    }

    private void givenPermissions(Permission... permissions) {
        when(permissionRepository.findAll()).thenReturn(List.of(permissions));
        for (Permission permission : permissions) {
            when(permissionRepository.findById(permission.getId())).thenReturn(Optional.of(permission));
        }
    }

    private void givenResident(String role, String propertySubRole) {
        Resident resident = new Resident();
        resident.setId(USER_ID);
        resident.setRole(role);
        resident.setPropertySubRole(propertySubRole);
        resident.setPropertyCompanyId(PC);
        when(residentRepository.findById(USER_ID)).thenReturn(Optional.of(resident));
    }

    private void givenOverride(String permissionId, boolean granted) {
        givenOverrides(Map.of(permissionId, granted));
    }

    private void givenOverrides(Map<String, Boolean> grantedByPermissionId) {
        List<UserPermission> overrides = new java.util.ArrayList<>();
        for (Map.Entry<String, Boolean> entry : grantedByPermissionId.entrySet()) {
            UserPermission override = new UserPermission();
            override.setId("up_" + entry.getKey());
            override.setResidentId(USER_ID);
            override.setPermissionId(entry.getKey());
            override.setGranted(entry.getValue());
            override.setPropertyCompanyId(PC);
            override.setOperatorId("res_admin");
            overrides.add(override);
        }
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId(USER_ID, PC)).thenReturn(overrides);
    }

    private Set<String> effective() {
        return guard.listEffectivePermissionCodes(USER_ID, PC);
    }

    @Test
    @DisplayName("操作员未授予：领导专属码不下发")
    void operatorWithoutGrant_hasNoConfigSet() {
        givenResident("property_admin", "property_operator");

        assertTrue(effective().contains("user.manage"));
        assertFalse(effective().contains("config.set"));
    }

    @Test
    @DisplayName("操作员被显式授予：领导专属码必须下发（接口已放行）")
    void operatorWithExplicitGrant_keepsConfigSet() {
        givenResident("property_admin", "property_operator");
        givenOverride("perm_016", true);

        assertTrue(effective().contains("config.set"));
    }

    @Test
    @DisplayName("显式授予后再撤销：回到不下发")
    void operatorWithRevokedGrant_dropsConfigSet() {
        givenResident("property_admin", "property_operator");
        givenOverride("perm_016", false);

        assertFalse(effective().contains("config.set"));
    }

    @Test
    @DisplayName("物业领导：默认下发领导专属码")
    void propertyLeader_keepsLeaderOnlyCodes() {
        givenResident("property_admin", "property_leader");

        assertTrue(effective().contains("config.set"));
        assertTrue(effective().contains("permission.read"));
    }

    @Test
    @DisplayName("标签身份即使库里残留授予行也不下发领导专属码（requirePermission 一律拒绝）")
    void tagIdentity_neverGetsLeaderOnlyCodes() {
        givenResident("resident", null);
        givenOverrides(Map.of("perm_016", true, "perm_030", true));

        assertFalse(effective().contains("config.set"));
        assertTrue(effective().contains("user.manage"));
    }

    @Test
    @DisplayName("平台专属码永不下发给物业侧")
    void platformOnlyCode_neverGrantedToPropertyAccount() {
        givenResident("property_admin", "property_operator");
        givenOverride("perm_022", true);

        assertFalse(effective().contains("merchant.read"));
    }
}
