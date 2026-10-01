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
 * 管理端权限码校验（platform_admin 全通；property_admin 按权限池 + 用户覆盖）
 */
@Component
@RequiredArgsConstructor
public class PermissionGuard {

    private static final Set<String> PLATFORM_ONLY_CODES = Set.of(
            "property_company.create", "property_company.update", "property_company.manage",
            "merchant.create", "merchant.read", "merchant.update");

    private static final Set<String> PROPERTY_LEADER_ONLY_CODES = Set.of(
            "config.set", "permission.manage", "permission.read", "property_operator.manage");

    private final RoleGuard roleGuard;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final RolePresetRepository rolePresetRepository;
    private final RolePresetPermissionRepository rolePresetPermissionRepository;
    private final ResidentRepository residentRepository;

    public void requireAdmin() {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
    }

    public void requirePlatformAdmin() {
        roleGuard.requireAnyRole("platform_admin");
    }

    public void requirePermission(String permissionCode) {
        requireAdmin();
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        String normalized = normalizeCode(permissionCode);
        if (PLATFORM_ONLY_CODES.contains(normalized)) {
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
        if ("property_admin".equals(resident.getRole())) {
            return true;
        }

        Optional<RolePreset> presetOpt = rolePresetRepository.findByCode(resident.getRole());
        if (presetOpt.isEmpty()) {
            return false;
        }
        List<RolePresetPermission> links = rolePresetPermissionRepository.findByPresetId(presetOpt.get().getId());
        return links.stream().anyMatch(link -> permissionId.equals(link.getPermissionId()));
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
        if ("property_admin".equals(resident.getRole())) {
            permissionRepository.findAll().stream()
                    .map(Permission::getCode)
                    .filter(code -> !PLATFORM_ONLY_CODES.contains(code))
                    .forEach(codes::add);
        } else {
            rolePresetRepository.findByCode(resident.getRole()).ifPresent(preset -> {
                rolePresetPermissionRepository.findByPresetId(preset.getId()).forEach(link ->
                        permissionRepository.findById(link.getPermissionId())
                                .ifPresent(p -> codes.add(p.getCode())));
            });
        }
        userPermissionRepository.findByResidentIdAndPropertyCompanyId(userId, propertyCompanyId)
                .forEach(override -> permissionRepository.findById(override.getPermissionId())
                        .ifPresent(p -> {
                            if (Boolean.TRUE.equals(override.getGranted())) {
                                codes.add(p.getCode());
                            } else {
                                codes.remove(p.getCode());
                            }
                        }));
        return codes;
    }

    public static String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            return code;
        }
        return code.replace(':', '.');
    }

    public boolean isCurrentPropertyLeader() {
        return "property_leader".equals(UserContext.currentRole());
    }
}
