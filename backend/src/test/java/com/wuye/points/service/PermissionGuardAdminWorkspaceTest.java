package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.Permission;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.RolePreset;
import com.wuye.points.entity.UserPermission;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PermissionRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.RolePresetPermissionRepository;
import com.wuye.points.repository.RolePresetRepository;
import com.wuye.points.repository.UserPermissionRepository;
import com.wuye.points.util.LeaderIdentityGuard;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 管理端工作台身份 + 权限码放行规则。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PermissionGuard 管理端工作台权限测试")
class PermissionGuardAdminWorkspaceTest {

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

    private PermissionGuard permissionGuard;

    @BeforeEach
    void setUp() {
        permissionGuard = new PermissionGuard(roleGuard, leaderIdentityGuard, permissionRepository,
                userPermissionRepository, rolePresetRepository, rolePresetPermissionRepository,
                residentRepository);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("活动组长持有 community:read 时可通过社区论坛读权限")
    void activityLeader_withCommunityRead_passes() {
        prepareUser("res_leader", "resident", "pc_001");
        Resident leader = resident("res_leader", "resident", "pc_001");
        Permission read = permission("perm_043", "community.read");

        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(false);
        when(residentRepository.findById("res_leader")).thenReturn(Optional.of(leader));
        when(permissionRepository.findByCode("community.read")).thenReturn(Optional.of(read));
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_leader", "pc_001"))
                .thenReturn(List.of(override("res_leader", "perm_043", true)));

        assertDoesNotThrow(() -> permissionGuard.requirePermission("community:read"));
    }

    @Test
    @DisplayName("活动组长被授予 merchant:audit 后可在管理端使用商家审核")
    void activityLeader_withGrantedMerchantAudit_passes() {
        prepareUser("res_leader", "resident", "pc_001");
        Resident leader = resident("res_leader", "resident", "pc_001");
        Permission audit = permission("perm_002", "merchant.audit");

        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(false);
        when(residentRepository.findById("res_leader")).thenReturn(Optional.of(leader));
        when(permissionRepository.findByCode("merchant.audit")).thenReturn(Optional.of(audit));
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_leader", "pc_001"))
                .thenReturn(List.of(override("res_leader", "perm_002", true)));

        assertDoesNotThrow(() -> permissionGuard.requirePermission("merchant:audit"));
    }

    @Test
    @DisplayName("标签身份未持有权限码时仍被拒绝")
    void activityLeader_withoutGrant_isDenied() {
        prepareUser("res_leader", "resident", "pc_001");
        Resident leader = resident("res_leader", "resident", "pc_001");
        Permission promotion = permission("perm_005", "promotion.push");

        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(false);
        when(residentRepository.findById("res_leader")).thenReturn(Optional.of(leader));
        when(permissionRepository.findByCode("promotion.push")).thenReturn(Optional.of(promotion));
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_leader", "pc_001"))
                .thenReturn(List.of());

        BizException ex = assertThrows(BizException.class,
                () -> permissionGuard.requirePermission("promotion:push"));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("物业领导专属码（config:set）即使授予标签身份也不放行")
    void activityLeader_withLeaderOnlyCode_isDenied() {
        prepareUser("res_leader", "resident", "pc_001");

        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> permissionGuard.requirePermission("config:set"));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("普通住户即使有 community:read 授权，也不能进管理端")
    void ordinaryResident_withCommunityRead_isDenied() {
        prepareUser("res_normal", "resident", "pc_001");
        when(roleGuard.isAdminWorkspaceUser()).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> permissionGuard.requirePermission("community:read"));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("社区管理员默认拥有社区论坛权限")
    void communityAdmin_hasCommunityPermissionsByDefault() {
        prepareUser("res_community", "community_admin", "pc_001");
        Resident communityAdmin = resident("res_community", "community_admin", "pc_001");
        Permission read = permission("perm_043", "community.read");

        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(false);
        when(residentRepository.findById("res_community")).thenReturn(Optional.of(communityAdmin));
        when(permissionRepository.findByCode("community.read")).thenReturn(Optional.of(read));

        assertDoesNotThrow(() -> permissionGuard.requirePermission("community:read"));
    }

    @Test
    @DisplayName("社区管理员不能越界使用报表权限")
    void communityAdmin_withReportsView_isDenied() {
        prepareUser("res_community", "community_admin", "pc_001");
        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> permissionGuard.requirePermission("reports:view"));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("物业管理员沿用原权限池，可通过社区论坛读权限")
    void propertyAdmin_canUseCommunityRead() {
        prepareUser("res_admin", "property_admin", "pc_001");
        Resident admin = resident("res_admin", "property_admin", "pc_001");
        Permission read = permission("perm_043", "community.read");

        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isPropertyAdmin()).thenReturn(true);
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(admin));
        when(permissionRepository.findByCode("community.read")).thenReturn(Optional.of(read));

        assertDoesNotThrow(() -> permissionGuard.requirePermission("community:read"));
    }

    @Test
    @DisplayName("标签身份的有效权限会剔除物业领导专属与平台专属码")
    void tagIdentity_effectivePermissions_excludesUnavailableCodes() {
        prepareUser("res_leader", "resident", "pc_001");
        Resident leader = resident("res_leader", "resident", "pc_001");
        Permission communityRead = permission("perm_043", "community.read");
        Permission configSet = permission("perm_016", "config.set");
        Permission merchantUpdate = permission("perm_033", "merchant.update");
        RolePreset preset = new RolePreset();
        preset.setId("rp_003");

        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(residentRepository.findById("res_leader")).thenReturn(Optional.of(leader));
        when(leaderIdentityGuard.leaderRoleCodes("res_leader", "pc_001"))
                .thenReturn(java.util.Set.of("activity_leader", "resident"));
        when(rolePresetRepository.findByCode("activity_leader")).thenReturn(Optional.of(preset));
        when(rolePresetPermissionRepository.findByPresetId("rp_003")).thenReturn(List.of(
                link("perm_043"), link("perm_016"), link("perm_033")));
        when(permissionRepository.findById("perm_043")).thenReturn(Optional.of(communityRead));
        when(permissionRepository.findById("perm_016")).thenReturn(Optional.of(configSet));
        when(permissionRepository.findById("perm_033")).thenReturn(Optional.of(merchantUpdate));
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_leader", "pc_001"))
                .thenReturn(List.of());

        java.util.Set<String> codes = permissionGuard.listEffectivePermissionCodes("res_leader", "pc_001");

        assertTrue(codes.contains("community.read"));
        assertFalse(codes.contains("config.set"));
        assertFalse(codes.contains("merchant.update"));
    }

    @Test
    @DisplayName("标签身份可授予除平台专属、物业领导专属外的全部权限")
    void tagIdentityGrantableCodes_excludesPlatformAndLeaderOnly() {
        assertTrue(permissionGuard.isTagIdentityGrantablePermission("community:read"));
        assertTrue(permissionGuard.isTagIdentityGrantablePermission("community.manage"));
        assertTrue(permissionGuard.isTagIdentityGrantablePermission("merchant:audit"));
        assertTrue(permissionGuard.isTagIdentityGrantablePermission("reports:view"));
        assertTrue(permissionGuard.isTagIdentityGrantablePermission("promotion:push"));
        assertTrue(permissionGuard.isTagIdentityGrantablePermission("delivery:grab"));

        assertFalse(permissionGuard.isTagIdentityGrantablePermission("config:set"));
        assertFalse(permissionGuard.isTagIdentityGrantablePermission("permission:manage"));
        assertFalse(permissionGuard.isTagIdentityGrantablePermission("permission:read"));
        assertFalse(permissionGuard.isTagIdentityGrantablePermission("property_operator:manage"));
        assertFalse(permissionGuard.isTagIdentityGrantablePermission("coin:display_control"));
        assertFalse(permissionGuard.isTagIdentityGrantablePermission("merchant:update"));
        assertFalse(permissionGuard.isTagIdentityGrantablePermission("property_company:manage"));
    }

    @Test
    @DisplayName("物业操作员子角色不具备 config:set / permission:read")
    void propertyOperatorSubRole_lacksLeaderOnlyCodes() {
        prepareUser("res_op", "property_admin", "pc_001");
        Resident operator = resident("res_op", "property_admin", "pc_001");
        operator.setPropertySubRole("property_operator");
        Permission configSet = permission("perm_016", "config.set");
        Permission permissionRead = permission("perm_021", "permission.read");
        Permission communityRead = permission("perm_043", "community.read");

        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(residentRepository.findById("res_op")).thenReturn(Optional.of(operator));
        when(permissionRepository.findByCode("config.set")).thenReturn(Optional.of(configSet));
        when(permissionRepository.findByCode("permission.read")).thenReturn(Optional.of(permissionRead));
        when(permissionRepository.findByCode("community.read")).thenReturn(Optional.of(communityRead));
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_op", "pc_001"))
                .thenReturn(List.of());

        assertFalse(permissionGuard.hasPermission("res_op", "config.set"));
        assertFalse(permissionGuard.hasPermission("res_op", "permission.read"));
        assertTrue(permissionGuard.hasPermission("res_op", "community.read"));
    }

    @Test
    @DisplayName("物业领导子角色（含旧 property_admin 空子角色）保留领导专属权限")
    void propertyLeaderSubRole_keepsLeaderOnlyCodes() {
        Resident leader = resident("res_leader02", "property_admin", "pc_001");
        leader.setPropertySubRole("property_leader");
        Resident legacyAdmin = resident("res_admin02", "property_admin", "pc_001");
        Permission configSet = permission("perm_016", "config.set");

        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(permissionRepository.findByCode("config.set")).thenReturn(Optional.of(configSet));
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_leader02", "pc_001"))
                .thenReturn(List.of());
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_admin02", "pc_001"))
                .thenReturn(List.of());
        when(residentRepository.findById("res_leader02")).thenReturn(Optional.of(leader));
        when(residentRepository.findById("res_admin02")).thenReturn(Optional.of(legacyAdmin));

        assertTrue(permissionGuard.hasPermission("res_leader02", "config.set"));
        assertTrue(permissionGuard.hasPermission("res_admin02", "config.set"));
    }

    @Test
    @DisplayName("操作员有效权限列表剔除领导专属码")
    void propertyOperatorSubRole_effectivePermissions_excludesLeaderOnlyCodes() {
        prepareUser("res_op", "property_admin", "pc_001");
        Resident operator = resident("res_op", "property_admin", "pc_001");
        operator.setPropertySubRole("property_operator");
        Permission configSet = permission("perm_016", "config.set");
        Permission communityRead = permission("perm_043", "community.read");

        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(residentRepository.findById("res_op")).thenReturn(Optional.of(operator));
        when(permissionRepository.findAll()).thenReturn(List.of(configSet, communityRead));
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_op", "pc_001"))
                .thenReturn(List.of());

        java.util.Set<String> codes = permissionGuard.listEffectivePermissionCodes("res_op", "pc_001");

        assertFalse(codes.contains("config.set"));
        assertTrue(codes.contains("community.read"));
    }

    private void prepareUser(String userId, String role, String propertyCompanyId) {
        UserContext.set(UserContext.builder()
                .userId(userId)
                .role(role)
                .propertyCompanyId(propertyCompanyId)
                .build());
    }

    private Resident resident(String id, String role, String propertyCompanyId) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setRole(role);
        resident.setPropertyCompanyId(propertyCompanyId);
        return resident;
    }

    private Permission permission(String id, String code) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        return permission;
    }

    private UserPermission override(String residentId, String permissionId, boolean granted) {
        UserPermission override = new UserPermission();
        override.setResidentId(residentId);
        override.setPermissionId(permissionId);
        override.setGranted(granted);
        return override;
    }

    private com.wuye.points.entity.RolePresetPermission link(String permissionId) {
        com.wuye.points.entity.RolePresetPermission link =
                new com.wuye.points.entity.RolePresetPermission();
        link.setPermissionId(permissionId);
        return link;
    }
}
