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

    public boolean isPlatformOnlyPermission(String permissionCode) {
        return PLATFORM_ONLY_CODES.contains(normalizeCode(permissionCode));
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
}
