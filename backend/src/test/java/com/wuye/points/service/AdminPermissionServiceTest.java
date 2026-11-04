package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.permission.AccountListResponse;
import com.wuye.points.dto.admin.permission.GrantPermissionsRequest;
import com.wuye.points.dto.admin.permission.GrantPermissionsResponse;
import com.wuye.points.dto.admin.permission.UserPermissionsResponse;
import com.wuye.points.entity.Permission;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.RolePreset;
import com.wuye.points.entity.UserPermission;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PermissionChangeLogRepository;
import com.wuye.points.repository.PermissionRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.RolePresetPermissionRepository;
import com.wuye.points.repository.RolePresetRepository;
import com.wuye.points.repository.UserPermissionRepository;
import com.wuye.points.util.AdminSupport;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AdminPermissionService 权限配置测试")
class AdminPermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RolePresetRepository rolePresetRepository;
    @Mock
    private RolePresetPermissionRepository rolePresetPermissionRepository;
    @Mock
    private UserPermissionRepository userPermissionRepository;
    @Mock
    private PermissionChangeLogRepository permissionChangeLogRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private PermissionGuard permissionGuard;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private AdminSupport adminSupport;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private AdminPermissionService service;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("活动组长授予 community:read 成功")
    void grant_activityLeader_communityRead_succeeds() {
        Resident leader = resident("res_leader", "resident", "pc_001");
        Permission read = permission("perm_043", "community.read");
        prepareTarget(leader);
        when(permissionRepository.findByCode("community.read")).thenReturn(Optional.of(read));
        when(permissionGuard.isTagIdentityGrantablePermission("community.read")).thenReturn(true);
        when(idGenerator.generateId("up")).thenReturn("up_001");
        when(idGenerator.generateId("pcl")).thenReturn("pcl_001");

        GrantPermissionsRequest request = new GrantPermissionsRequest();
        request.setPermissionCodes(List.of("community:read"));

        GrantPermissionsResponse response = service.grantPermissions("res_leader", request);
        assertEquals(1, response.getGrantedCount());
        assertEquals(List.of("community:read"), response.getGrantedCodes());
        verify(userPermissionRepository).save(any(UserPermission.class));
        verify(permissionChangeLogRepository).save(any());
    }

    @Test
    @DisplayName("活动组长授予 merchant:audit 成功")
    void grant_activityLeader_merchantAudit_succeeds() {
        Resident leader = resident("res_leader", "resident", "pc_001");
        Permission audit = permission("perm_002", "merchant.audit");
        prepareTarget(leader);
        when(permissionRepository.findByCode("merchant.audit")).thenReturn(Optional.of(audit));
        when(permissionGuard.isTagIdentityGrantablePermission("merchant.audit")).thenReturn(true);
        when(idGenerator.generateId("up")).thenReturn("up_001");
        when(idGenerator.generateId("pcl")).thenReturn("pcl_001");

        GrantPermissionsRequest request = new GrantPermissionsRequest();
        request.setPermissionCodes(List.of("merchant:audit"));

        GrantPermissionsResponse response = service.grantPermissions("res_leader", request);
        assertEquals(1, response.getGrantedCount());
        assertEquals(List.of("merchant:audit"), response.getGrantedCodes());
    }

    @Test
    @DisplayName("活动组长授予物业领导专属的 config:set 被拒绝")
    void grant_activityLeader_leaderOnlyCode_rejected() {
        Resident leader = resident("res_leader", "resident", "pc_001");
        Permission configSet = permission("perm_016", "config.set");
        prepareTarget(leader);
        when(permissionRepository.findByCode("config.set")).thenReturn(Optional.of(configSet));
        when(permissionGuard.isTagIdentityGrantablePermission("config.set")).thenReturn(false);

        GrantPermissionsRequest request = new GrantPermissionsRequest();
        request.setPermissionCodes(List.of("config:set"));

        BizException ex = assertThrows(BizException.class,
                () -> service.grantPermissions("res_leader", request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("普通住户不能被配置管理端权限")
    void grant_ordinaryResident_rejected() {
        Resident resident = resident("res_normal", "resident", "pc_001");
        when(residentRepository.findById("res_normal")).thenReturn(Optional.of(resident));
        when(roleGuard.isAdminWorkspaceUser(resident)).thenReturn(false);

        GrantPermissionsRequest request = new GrantPermissionsRequest();
        request.setPermissionCodes(List.of("community:read"));

        BizException ex = assertThrows(BizException.class,
                () -> service.grantPermissions("res_normal", request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("活动组长权限详情命中 rp_003，并返回有效角色码")
    void getUserPermissions_activityLeader_usesActivityPreset() {
        Resident leader = resident("res_leader", "resident", "pc_001");
        leader.setUpdatedAt(LocalDateTime.now());
        when(residentRepository.findById("res_leader")).thenReturn(Optional.of(leader));
        when(permissionGuard.listEffectiveRoleCodes("res_leader", "pc_001"))
                .thenReturn(Set.of("resident", "activity_leader"));
        when(permissionGuard.listEffectivePermissionCodes("res_leader", "pc_001"))
                .thenReturn(Set.of("community.read"));
        RolePreset preset = new RolePreset();
        preset.setId("rp_003");
        preset.setName("活动组组长");
        preset.setCode("activity_leader");
        when(permissionGuard.findPrimaryPreset(anySet())).thenReturn(Optional.of(preset));
        when(userPermissionRepository.findByResidentIdAndPropertyCompanyId("res_leader", "pc_001"))
                .thenReturn(List.of());

        UserPermissionsResponse response = service.getUserPermissions("res_leader");

        assertEquals("rp_003", response.getAppliedPreset().getId());
        assertTrue(response.getRoleCodes().contains("activity_leader"));
        assertEquals(List.of("community:read"), response.getEffectivePermissions());
    }

    @Test
    @DisplayName("账号列表默认只返回管理端工作台账号，并带身份标签")
    void listAccounts_adminOnly_filtersAndLabels() {
        UserContext.set(UserContext.builder()
                .userId("res_admin")
                .role("property_admin")
                .propertyCompanyId("pc_001")
                .build());
        Resident admin = resident("res_admin", "property_admin", "pc_001");
        Resident leader = resident("res_leader", "resident", "pc_001");
        Resident normal = resident("res_normal", "resident", "pc_001");
        when(residentRepository.findByPropertyCompanyId("pc_001"))
                .thenReturn(List.of(admin, leader, normal));
        when(roleGuard.isPlatformAdmin()).thenReturn(false);
        when(roleGuard.isAdminWorkspaceUser(admin)).thenReturn(true);
        when(roleGuard.isAdminWorkspaceUser(leader)).thenReturn(true);
        when(roleGuard.isAdminWorkspaceUser(normal)).thenReturn(false);
        when(permissionGuard.listEffectivePermissionCodes(anyString(), anyString()))
                .thenReturn(Set.of("community.read"));
        when(permissionGuard.listEffectiveRoleCodes("res_admin", "pc_001"))
                .thenReturn(Set.of("property_admin"));
        when(permissionGuard.listEffectiveRoleCodes("res_leader", "pc_001"))
                .thenReturn(Set.of("resident", "activity_leader"));
        when(permissionGuard.listEffectiveRoleCodes("res_normal", "pc_001"))
                .thenReturn(Set.of("resident"));

        AccountListResponse response = service.listAccounts(null, true);

        assertEquals(2, response.getTotal());
        assertTrue(response.getAccounts().stream()
                .anyMatch(a -> "res_leader".equals(a.getId())
                        && Boolean.TRUE.equals(a.getAdminWorkspace())
                        && a.getRoleCodes().contains("activity_leader")));
        assertFalse(response.getAccounts().stream().anyMatch(a -> "res_normal".equals(a.getId())));
    }

    private void prepareTarget(Resident target) {
        when(residentRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(roleGuard.isAdminWorkspaceUser(target)).thenReturn(true);
    }

    private Resident resident(String id, String role, String propertyCompanyId) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName(id);
        resident.setPhone("13800000000");
        resident.setRole(role);
        resident.setPropertyCompanyId(propertyCompanyId);
        resident.setStatus("active");
        return resident;
    }

    private Permission permission(String id, String code) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setStatus("active");
        return permission;
    }
}
