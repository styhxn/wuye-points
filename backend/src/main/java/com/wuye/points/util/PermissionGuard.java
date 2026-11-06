package com.wuye.points.util;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.Permission;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.RolePreset;
import com.wuye.points.entity.RolePresetPermission;
import com.wuye.points.entity.UserPermission;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PermissionRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.RolePresetPermissionRepository;
import com.wuye.points.repository.RolePresetRepository;
import com.wuye.points.repository.UserPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 管理端权限码校验（platform_admin 全通；property_admin 按权限池 + 用户覆盖；
 * 社区管理员/负责人/活动组长等标签身份按“预设 ∪ 用户授权”放行，
 * 但平台专属、物业领导专属码始终不对标签身份开放）。
 *
 * <p>标签身份可授予范围由 {@link #isTagIdentityGrantablePermission(String)} 统一判断，
 * 与权限配置页的 grantableToTagIdentities 字段保持一致。</p>
 */
@Component
@RequiredArgsConstructor
public class PermissionGuard {

    private static final Set<String> PLATFORM_ONLY_CODES = Set.of(
            "property_company.create", "property_company.update", "property_company.manage",
            "merchant.create", "merchant.read", "merchant.update");

    private static final Set<String> PROPERTY_LEADER_ONLY_CODES = Set.of(
            "config.set", "permission.manage", "permission.read", "property_operator.manage",
            // 物业币展示是租户级开关，只给物业主账号/领导与平台；标签身份不可授予。
            "coin.display_control");

    /**
     * 社区管理员默认拥有的权限码（无需额外授权）。
     *
     * <p>标签身份可被授予的权限范围由 {@link #isTagIdentityGrantablePermission(String)} 动态判断：
     * 除平台专属、物业领导专属外均可授予；社区管理员只是“默认带社区论坛两项”，
     * 其余权限仍需在权限配置里显式授予。</p>
     */
    private static final Set<String> COMMUNITY_ADMIN_DEFAULT_CODES = Set.of(
            "community.read", "community.manage");

    private static final String COMMUNITY_ADMIN = "community_admin";

    /** 角色预设展示优先级：同一账号有多个标签身份时，取最贴近业务入口的预设。 */
    private static final List<String> PRESET_PRIORITY = List.of(
            "activity_leader", "coordinator", "sector_leader", "individual_leader",
            "community_admin", "courier", "property_admin", "platform_admin");

    private final RoleGuard roleGuard;
    private final LeaderIdentityGuard leaderIdentityGuard;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final RolePresetRepository rolePresetRepository;
    private final RolePresetPermissionRepository rolePresetPermissionRepository;
    private final ResidentRepository residentRepository;

    public void requireAdmin() {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
    }

    /** 管理端工作台门：平台/物业/社区管理员，或持负责人/组长标签的住户。 */
    public void requireAdminWorkspace() {
        if (!roleGuard.isAdminWorkspaceUser()) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
    }

    public void requirePlatformAdmin() {
        roleGuard.requireAnyRole("platform_admin");
    }

    public void requirePermission(String permissionCode) {
        requireAdminWorkspace();
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        String normalized = normalizeCode(permissionCode);
        if (PLATFORM_ONLY_CODES.contains(normalized)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        // 物业管理员沿用原权限池；标签身份可持有除平台专属、物业领导专属外的权限码，
        // 但仍需通过预设或用户授权真正持有该码（下面 hasPermission 校验）。
        if (!roleGuard.isPropertyAdmin() && !isTagIdentityGrantablePermission(normalized)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        if (hasPermission(UserContext.currentUserId(), normalized)) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    /**
     * 主权限码未命中时依次尝试备用权限码（权限码未入库时 property_admin 仍可通过）
     */
    public void requirePermissionWithFallback(String primary, String... fallbacks) {
        requireAdmin();
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        String userId = UserContext.currentUserId();
        if (hasPermission(userId, normalizeCode(primary))) {
            return;
        }
        for (String fallback : fallbacks) {
            if (hasPermission(userId, normalizeCode(fallback))) {
                return;
            }
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    /**
     * 权限码未入库时回退为 platform_admin / property_admin 角色校验
     */
    public void requirePermissionOrAdmin(String permissionCode) {
        String normalized = normalizeCode(permissionCode);
        if (permissionRepository.findByCode(normalized).isEmpty()) {
            requireAdmin();
            return;
        }
        requirePermission(permissionCode);
    }

    /**
     * 只读权限校验（2026-09-07 前端问题 C）：
     * coordinator 为只读下级角色，只要其角色预设/用户覆盖中持有该权限码即放行；
     * 其余角色仍走 requirePermissionOrAdmin（platform_admin 全通，property_admin 按权限池）。
     */
    public void requireReadPermissionOrAdmin(String permissionCode) {
        requirePermissionOrAdminAllowingCoordinator(permissionCode);
    }

    /**
     * 权限码校验并放行持有该权限码的 coordinator（2026-09-08 前端问题：统筹写操作 403）。
     *
     * <p>coordinator 是物业下属的半写角色（统筹/管理盘负责人），按文档 §25 / §39 可创建特惠、
     * 管理本人名下的板块负责人。此处不再用 requireAdmin 一刀切，改为校验其角色预设/用户覆盖中
     * 是否持有该权限码（如 {@code sector.manage}、{@code promotion.push}）；
     * 数据范围（本物业 / 本人名下）由各 Service 自行收敛，避免越权。
     *
     * <p>非 coordinator 角色保持原逻辑：platform_admin 全通，property_admin 按权限池校验。
     */
    public void requirePermissionOrAdminAllowingCoordinator(String permissionCode) {
        if (roleGuard.isCoordinator()) {
            String normalized = normalizeCode(permissionCode);
            if (permissionRepository.findByCode(normalized).isEmpty()
                    || !hasPermission(UserContext.currentUserId(), normalized)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            return;
        }
        requirePermissionOrAdmin(permissionCode);
    }

    public boolean isPlatformOnlyPermission(String permissionCode) {
        return PLATFORM_ONLY_CODES.contains(normalizeCode(permissionCode));
    }

    public boolean isPropertyLeaderOnlyPermission(String permissionCode) {
        return PROPERTY_LEADER_ONLY_CODES.contains(normalizeCode(permissionCode));
    }

    /**
     * 该权限码是否允许授予活动组长/负责人/社区管理员等标签身份。
     *
     * <p>标签身份可以持有除平台专属、物业领导专属以外的全部权限码；
     * 具体能否使用仍取决于是否已通过角色预设或用户授权持有该码。</p>
     */
    public boolean isTagIdentityGrantablePermission(String permissionCode) {
        String normalized = normalizeCode(permissionCode);
        return !PLATFORM_ONLY_CODES.contains(normalized)
                && !PROPERTY_LEADER_ONLY_CODES.contains(normalized);
    }

    /** 从有效角色码集合中选取一个确定性的主预设，供 profile / 权限详情展示。 */
    public Optional<RolePreset> findPrimaryPreset(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Optional.empty();
        }
        for (String roleCode : PRESET_PRIORITY) {
            if (!roleCodes.contains(roleCode)) {
                continue;
            }
            Optional<RolePreset> preset = rolePresetRepository.findByCode(roleCode);
            if (preset.isPresent()) {
                return preset;
            }
        }
        return Optional.empty();
    }

    public boolean hasPermission(String userId, String permissionCode) {
        String normalized = normalizeCode(permissionCode);
        Optional<Permission> permissionOpt = permissionRepository.findByCode(normalized);
        if (permissionOpt.isEmpty()) {
            return roleGuard.isPropertyAdmin();
        }
        String permissionId = permissionOpt.get().getId();

        List<UserPermission> overrides = userPermissionRepository.findByResidentIdAndPropertyCompanyId(
                userId, UserContext.currentPropertyCompanyId());
        for (UserPermission override : overrides) {
            if (permissionId.equals(override.getPermissionId())) {
                return Boolean.TRUE.equals(override.getGranted());
            }
        }

        Resident resident = residentRepository.findById(userId).orElse(null);
        if (resident == null || !StringUtils.hasText(resident.getRole())) {
            return false;
        }
        String baseRole = normalizeBaseRole(resident.getRole());
        if ("property_admin".equals(baseRole)) {
            // 物业操作员（历史 role=property_operator，或 role=property_admin + propertySubRole=property_operator）
            // 不具备物业领导专属权限（配置/权限管理/子账号管理）；用户级授权已在上面优先命中。
            return !(isOperatorAccount(resident) && PROPERTY_LEADER_ONLY_CODES.contains(normalized));
        }

        // 2026-10-20 负责人标签化：住户角色保持 resident，负责人身份由业务表 active 记录判定。
        // 权限预设按「有效角色集合」取并集：库内角色 + 全部负责人身份角色码，
        // 保证统筹/板块/个体负责人标签化后不丢原有角色预设权限（兼容存量 role 仍为负责人角色值的数据）。
        for (String roleCode : effectiveRoleCodes(resident)) {
            Optional<RolePreset> presetOpt = rolePresetRepository.findByCode(roleCode);
            if (presetOpt.isEmpty()) {
                continue;
            }
            List<RolePresetPermission> links = rolePresetPermissionRepository.findByPresetId(presetOpt.get().getId());
            if (links.stream().anyMatch(link -> permissionId.equals(link.getPermissionId()))) {
                return true;
            }
        }
        // 社区管理员是管理端工作台身份，默认拥有社区论坛权限；其他管理权限仍需单独授权。
        return COMMUNITY_ADMIN.equals(baseRole) && COMMUNITY_ADMIN_DEFAULT_CODES.contains(normalized);
    }

    /**
     * 有效角色码集合：库内 residents.role + 该住户持有的全部负责人身份角色码。
     * 负责人标签化后 role 为 resident，此处把 coordinators/sector_leaders/individual_leaders
     * 的 active 记录还原为角色码，供角色预设（role_presets）权限解析使用。
     */
    private Set<String> effectiveRoleCodes(Resident resident) {
        Set<String> codes = new HashSet<>();
        codes.add(normalizeBaseRole(resident.getRole()));
        codes.addAll(leaderIdentityGuard.leaderRoleCodes(
                resident.getId(), resident.getPropertyCompanyId()));
        return codes;
    }

    /**
     * 物业操作员账号：兼容历史 role=property_operator，以及现行
     * role=property_admin + propertySubRole=property_operator 的模型。
     * 操作员不具备物业领导专属权限（config.set、permission.read/manage、property_operator.manage）。
     */
    private boolean isOperatorAccount(Resident resident) {
        if ("property_operator".equals(resident.getRole())) {
            return true;
        }
        return "property_admin".equals(resident.getRole())
                && "property_operator".equals(resident.getPropertySubRole());
    }

    /**
     * 历史物业子账号 role 归一：property_leader/property_operator 视为 property_admin，
     * 避免存量账号在权限码体系下全部 403。
     */
    public static String normalizeBaseRole(String role) {
        if ("property_leader".equals(role) || "property_operator".equals(role)) {
            return "property_admin";
        }
        return role;
    }

    /** 有效角色码集合（库内角色归一 + 负责人标签身份），供 /auth/profile 下发。 */
    public Set<String> listEffectiveRoleCodes(String userId, String propertyCompanyId) {
        Resident resident = residentRepository.findById(userId).orElse(null);
        if (resident == null) {
            return Set.of();
        }
        Set<String> codes = new HashSet<>(effectiveRoleCodes(resident));
        codes.remove(null);
        return codes;
    }

    public Set<String> listEffectivePermissionCodes(String userId, String propertyCompanyId) {
        Set<String> codes = new HashSet<>();
        Resident resident = residentRepository.findById(userId).orElse(null);
        if (resident == null) {
            return codes;
        }
        if ("platform_admin".equals(resident.getRole())) {
            permissionRepository.findAll().forEach(p -> codes.add(p.getCode()));
            return codes;
        }
        if ("property_admin".equals(normalizeBaseRole(resident.getRole()))) {
            boolean operatorAccount = isOperatorAccount(resident);
            permissionRepository.findAll().stream()
                    .map(Permission::getCode)
                    .filter(code -> !PLATFORM_ONLY_CODES.contains(code))
                    .filter(code -> !operatorAccount || !PROPERTY_LEADER_ONLY_CODES.contains(code))
                    .forEach(codes::add);
        } else {
            // 2026-10-20 负责人标签化：按有效角色集合（库内角色 + 负责人身份）取预设并集
            for (String roleCode : effectiveRoleCodes(resident)) {
                rolePresetRepository.findByCode(roleCode).ifPresent(preset -> {
                    rolePresetPermissionRepository.findByPresetId(preset.getId()).forEach(link ->
                            permissionRepository.findById(link.getPermissionId())
                                    .ifPresent(p -> codes.add(p.getCode())));
                });
            }
            // 社区管理员默认拥有社区论坛权限，保证 /auth/profile 能直接驱动前端菜单。
            if (COMMUNITY_ADMIN.equals(normalizeBaseRole(resident.getRole()))) {
                permissionRepository.findAll().stream()
                        .map(Permission::getCode)
                        .filter(COMMUNITY_ADMIN_DEFAULT_CODES::contains)
                        .forEach(codes::add);
            }
        }
        Set<String> explicitlyGranted = new HashSet<>();
        userPermissionRepository.findByResidentIdAndPropertyCompanyId(userId, propertyCompanyId)
                .forEach(override -> permissionRepository.findById(override.getPermissionId())
                        .ifPresent(p -> {
                            if (Boolean.TRUE.equals(override.getGranted())) {
                                codes.add(p.getCode());
                                explicitlyGranted.add(p.getCode());
                            } else {
                                codes.remove(p.getCode());
                            }
                        }));
        // 下发给管理端的有效权限必须与接口可放行范围一致，避免“菜单有、接口 403”。
        // 领导专属码：标签身份一律不下发（requirePermission 直接拒绝）；物业操作员默认剥夺，
        // 但 hasPermission 认用户级授权，所以权限配置里显式授予过的必须照实下发，
        // 否则接口已放行、菜单看不到，表现就是“赋予了也不生效”。
        boolean basePropertyAdmin = "property_admin".equals(normalizeBaseRole(resident.getRole()));
        boolean operatorAccount = isOperatorAccount(resident);
        codes.removeIf(code -> PLATFORM_ONLY_CODES.contains(code)
                || ((!basePropertyAdmin || (operatorAccount && !explicitlyGranted.contains(code)))
                    && PROPERTY_LEADER_ONLY_CODES.contains(code)));
        return codes;
    }

    public static String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            return code;
        }
        return code.replace(':', '.');
    }

    /**
     * 当前账号是否为「物业领导」。
     *
     * <p>整改清单条目37：现行模型下物业领导的登录态是 {@code role=property_admin} +
     * {@code residents.property_sub_role=property_leader}，JWT 的 role 里根本不会出现
     * {@code property_leader}，此前只比对 JWT 字符串导致「仅平台管理员或物业领导可用」
     * 的板块把物业领导自己挡在门外。口径与
     * {@code AdminPropertyOperatorService#requirePropertyLeaderOrAdmin} 保持一致：
     * 兼容历史 role=property_leader，子角色为空的老 property_admin 主账号按领导放行，
     * property_operator 一律不是领导。</p>
     */
    public boolean isCurrentPropertyLeader() {
        String role = UserContext.currentRole();
        if ("property_leader".equals(role)) {
            return true;
        }
        if ("property_operator".equals(role)) {
            return false;
        }
        if (!"property_admin".equals(RoleGuard.normalizeRole(role))) {
            return false;
        }
        Resident resident = residentRepository.findById(UserContext.currentUserId()).orElse(null);
        if (resident == null) {
            return false;
        }
        if ("property_operator".equals(resident.getRole())) {
            return false;
        }
        String subRole = resident.getPropertySubRole();
        return !StringUtils.hasText(subRole) || "property_leader".equals(subRole);
    }
}
