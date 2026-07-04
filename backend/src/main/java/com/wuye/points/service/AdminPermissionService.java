package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.permission.AccountListItemResponse;
import com.wuye.points.dto.admin.permission.AccountListResponse;
import com.wuye.points.dto.admin.permission.AppliedPresetResponse;
import com.wuye.points.dto.admin.permission.CreateRolePresetRequest;
import com.wuye.points.dto.admin.permission.GrantPermissionsRequest;
import com.wuye.points.dto.admin.permission.GrantPermissionsResponse;
import com.wuye.points.dto.admin.permission.PermissionChangeLogItemResponse;
import com.wuye.points.dto.admin.permission.PermissionItemResponse;
import com.wuye.points.dto.admin.permission.PermissionListResponse;
import com.wuye.points.dto.admin.permission.PermissionOverrideItemResponse;
import com.wuye.points.dto.admin.permission.RevokePermissionsRequest;
import com.wuye.points.dto.admin.permission.RevokePermissionsResponse;
import com.wuye.points.dto.admin.permission.RolePresetItemResponse;
import com.wuye.points.dto.admin.permission.RolePresetListResponse;
import com.wuye.points.dto.admin.permission.UpdateRolePresetRequest;
import com.wuye.points.dto.admin.permission.UserPermissionsResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Permission;
import com.wuye.points.entity.PermissionChangeLog;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.RolePreset;
import com.wuye.points.entity.RolePresetPermission;
import com.wuye.points.entity.UserPermission;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PermissionChangeLogRepository;
import com.wuye.points.repository.PermissionRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.RolePresetPermissionRepository;
import com.wuye.points.repository.RolePresetRepository;
import com.wuye.points.repository.UserPermissionRepository;
import com.wuye.points.repository.spec.PermissionChangeLogSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePresetRepository rolePresetRepository;
    private final RolePresetPermissionRepository rolePresetPermissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionChangeLogRepository permissionChangeLogRepository;
    private final ResidentRepository residentRepository;
    private final PermissionGuard permissionGuard;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public AccountListResponse listAccounts(String keyword) {
        permissionGuard.requirePermissionOrAdmin("permission.read");

        String propertyCompanyId = roleGuard.isPlatformAdmin()
                ? null
                : UserContext.currentPropertyCompanyId();

        List<Resident> residents;
        if (propertyCompanyId != null) {
            residents = residentRepository.findByPropertyCompanyId(propertyCompanyId);
        } else {
            residents = residentRepository.findAll();
        }

        if (StringUtils.hasText(keyword)) {
            String lowerKeyword = keyword.toLowerCase();
            residents = residents.stream()
                    .filter(r -> (r.getName() != null && r.getName().toLowerCase().contains(lowerKeyword))
                            || (r.getPhone() != null && r.getPhone().contains(keyword)))
                    .toList();
        }

        List<AccountListItemResponse> accounts = residents.stream()
                .filter(r -> r.getRole() != null)
                .map(r -> {
                    int permCount = permissionGuard.listEffectivePermissionCodes(
                            r.getId(), r.getPropertyCompanyId()).size();
                    return AccountListItemResponse.builder()
                            .id(r.getId())
                            .name(r.getName())
                            .phone(maskPhone(r.getPhone()))
                            .role(r.getRole())
                            .propertyCompanyId(r.getPropertyCompanyId())
                            .status(r.getStatus())
                            .effectivePermissionCount(permCount)
                            .build();
                })
                .toList();

        return AccountListResponse.builder()
                .accounts(accounts)
                .total(accounts.size())
                .build();
    }

    @Transactional(readOnly = true)
    public PermissionListResponse listPermissions() {
        permissionGuard.requirePermissionOrAdmin("permission.read");

        List<Permission> all = permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getModule, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Permission::getCode))
                .toList();

        List<PermissionItemResponse> permissions = all.stream()
                .filter(permission -> roleGuard.isPlatformAdmin()
                        || !permissionGuard.isPlatformOnlyPermission(permission.getCode()))
                .map(this::toPermissionItem)
                .toList();

        Set<String> modules = new LinkedHashSet<>();
        permissions.forEach(item -> {
            if (StringUtils.hasText(item.getModule())) {
                modules.add(item.getModule());
            }
        });

        return PermissionListResponse.builder()
                .permissions(permissions)
                .modules(new ArrayList<>(modules))
                .build();
    }

    @Transactional(readOnly = true)
    public RolePresetListResponse listRolePresets(String scope) {
        permissionGuard.requirePermissionOrAdmin("permission.read");

        List<RolePreset> presets = loadPresetsByScope(scope);
        List<RolePresetItemResponse> items = presets.stream()
                .map(this::toRolePresetItem)
                .toList();

        return RolePresetListResponse.builder().presets(items).build();
    }

    @Transactional
    public RolePresetItemResponse createRolePreset(CreateRolePresetRequest request) {
        permissionGuard.requirePermissionOrAdmin("permission.manage");

        String propertyCompanyId = resolvePresetPropertyCompanyId(null);
        validatePresetPermissionCodes(request.getPermissionCodes());

        RolePreset preset = new RolePreset();
        preset.setId(idGenerator.generateId("rp"));
        preset.setName(request.getName().trim());
        preset.setCode(resolvePresetCode(request.getCode(), request.getName()));
        preset.setDescription(request.getDescription());
        preset.setPropertyCompanyId(propertyCompanyId);
        preset.setStatus("active");
        rolePresetRepository.save(preset);

        replacePresetPermissions(preset.getId(), request.getPermissionCodes());
        return toRolePresetItem(preset);
    }

    @Transactional
    public RolePresetItemResponse updateRolePreset(String id, UpdateRolePresetRequest request) {
        permissionGuard.requirePermissionOrAdmin("permission.manage");
        adminSupport.validateIdPrefix(id, "rp_");

        RolePreset preset = getAccessiblePreset(id);
        boolean builtin = preset.getPropertyCompanyId() == null;

        if (builtin) {
            if (request.getDescription() != null) {
                preset.setDescription(request.getDescription());
            }
            if (request.getName() != null || request.getCode() != null || request.getPermissionCodes() != null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "内置预设仅可更新描述");
            }
        } else {
            if (request.getName() != null) {
                preset.setName(request.getName().trim());
            }
            if (request.getCode() != null) {
                String newCode = request.getCode().trim();
                rolePresetRepository.findByCode(newCode).ifPresent(existing -> {
                    if (!existing.getId().equals(preset.getId())) {
                        throw new BizException(ErrorCode.PARAM_INVALID, "预设编码已存在");
                    }
                });
                preset.setCode(newCode);
            }
            if (request.getDescription() != null) {
                preset.setDescription(request.getDescription());
            }
            if (request.getPermissionCodes() != null) {
                validatePresetPermissionCodes(request.getPermissionCodes());
                replacePresetPermissions(preset.getId(), request.getPermissionCodes());
            }
        }

        rolePresetRepository.save(preset);
        return toRolePresetItem(preset);
    }

    @Transactional(readOnly = true)
    public UserPermissionsResponse getUserPermissions(String userId) {
        permissionGuard.requirePermissionOrAdmin("permission.read");
        adminSupport.validateIdPrefix(userId, "res_");

        Resident resident = getAccessibleResident(userId);
        String propertyCompanyId = resident.getPropertyCompanyId();

        AppliedPresetResponse appliedPreset = null;
        if (StringUtils.hasText(resident.getRole())) {
            appliedPreset = rolePresetRepository.findByCode(resident.getRole())
                    .map(preset -> AppliedPresetResponse.builder()
                            .id(preset.getId())
                            .name(preset.getName())
                            .code(preset.getCode())
                            .build())
                    .orElse(null);
        }

        List<PermissionOverrideItemResponse> overrides = userPermissionRepository
                .findByResidentIdAndPropertyCompanyId(userId, propertyCompanyId).stream()
                .map(override -> permissionRepository.findById(override.getPermissionId())
                        .map(permission -> PermissionOverrideItemResponse.builder()
                                .permissionCode(toApiPermissionCode(permission.getCode()))
                                .granted(override.getGranted())
                                .source("custom")
                                .operatorId(override.getOperatorId())
                                .updatedAt(DateTimeUtil.toIsoUtc(override.getUpdatedAt()))
                                .build())
                        .orElse(null))
                .filter(item -> item != null)
                .toList();

        Set<String> effective = permissionGuard.listEffectivePermissionCodes(userId, propertyCompanyId).stream()
                .map(this::toApiPermissionCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LocalDateTime latestUpdatedAt = userPermissionRepository
                .findByResidentIdAndPropertyCompanyId(userId, propertyCompanyId).stream()
                .map(UserPermission::getUpdatedAt)
                .filter(value -> value != null)
                .max(LocalDateTime::compareTo)
                .orElse(resident.getUpdatedAt());

        return UserPermissionsResponse.builder()
                .accountId(resident.getId())
                .accountName(resident.getName())
                .appliedPreset(appliedPreset)
                .overrides(overrides)
                .effectivePermissions(new ArrayList<>(effective))
                .effectivePermissionCount(effective.size())
                .updatedAt(DateTimeUtil.toIsoUtc(latestUpdatedAt))
                .build();
    }

    @Transactional
    public GrantPermissionsResponse grantPermissions(String userId, GrantPermissionsRequest request) {
        permissionGuard.requirePermissionOrAdmin("permission.manage");
        adminSupport.validateIdPrefix(userId, "res_");

        Resident resident = getAccessibleResident(userId);
        String propertyCompanyId = resident.getPropertyCompanyId();
        String operatorId = UserContext.currentUserId();

        List<String> grantedCodes = new ArrayList<>();
        for (String rawCode : request.getPermissionCodes()) {
            Permission permission = resolveGrantablePermission(rawCode);
            String apiCode = toApiPermissionCode(permission.getCode());

            String beforeValue = "false";
            UserPermission userPermission = userPermissionRepository
                    .findByResidentIdAndPermissionId(userId, permission.getId())
                    .orElse(null);
            if (userPermission != null) {
                beforeValue = String.valueOf(userPermission.getGranted());
            } else {
                userPermission = new UserPermission();
                userPermission.setId(idGenerator.generateId("up"));
                userPermission.setResidentId(userId);
                userPermission.setPermissionId(permission.getId());
                userPermission.setPropertyCompanyId(propertyCompanyId);
            }
            userPermission.setGranted(true);
            userPermission.setOperatorId(operatorId);
            userPermissionRepository.save(userPermission);

            writePermissionChangeLog(userId, propertyCompanyId, operatorId, "grant",
                    permission.getCode(), null, beforeValue, "true", request.getReason());
            grantedCodes.add(apiCode);
        }

        return GrantPermissionsResponse.builder()
                .accountId(userId)
                .grantedCount(grantedCodes.size())
                .grantedCodes(grantedCodes)
                .operatorId(operatorId)
                .createdAt(DateTimeUtil.nowIsoUtc())
                .build();
    }

    @Transactional
    public RevokePermissionsResponse revokePermissions(String userId, RevokePermissionsRequest request) {
        permissionGuard.requirePermissionOrAdmin("permission.manage");
        adminSupport.validateIdPrefix(userId, "res_");

        Resident resident = getAccessibleResident(userId);
        String propertyCompanyId = resident.getPropertyCompanyId();
        String operatorId = UserContext.currentUserId();

        List<String> revokedCodes = new ArrayList<>();
        for (String rawCode : request.getPermissionCodes()) {
            Permission permission = resolveGrantablePermission(rawCode);
            String apiCode = toApiPermissionCode(permission.getCode());

            String beforeValue = "true";
            UserPermission userPermission = userPermissionRepository
                    .findByResidentIdAndPermissionId(userId, permission.getId())
                    .orElse(null);
            if (userPermission != null) {
                beforeValue = String.valueOf(userPermission.getGranted());
            } else {
                userPermission = new UserPermission();
                userPermission.setId(idGenerator.generateId("up"));
                userPermission.setResidentId(userId);
                userPermission.setPermissionId(permission.getId());
                userPermission.setPropertyCompanyId(propertyCompanyId);
            }
            userPermission.setGranted(false);
            userPermission.setOperatorId(operatorId);
            userPermissionRepository.save(userPermission);

            writePermissionChangeLog(userId, propertyCompanyId, operatorId, "revoke",
                    permission.getCode(), null, beforeValue, "false", request.getReason());
            revokedCodes.add(apiCode);
        }

        return RevokePermissionsResponse.builder()
                .accountId(userId)
                .revokedCount(revokedCodes.size())
                .revokedCodes(revokedCodes)
                .operatorId(operatorId)
                .createdAt(DateTimeUtil.nowIsoUtc())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PermissionChangeLogItemResponse> listPermissionChangeLogs(
            int page,
            int pageSize,
            String residentId,
            String operatorId,
            String action,
            String startDate,
            String endDate) {
        permissionGuard.requirePermissionOrAdmin("permission.read");

        if (StringUtils.hasText(residentId)) {
            adminSupport.validateIdPrefix(residentId, "res_");
        }
        if (StringUtils.hasText(operatorId)) {
            adminSupport.validateIdPrefix(operatorId, "res_");
        }
        if (StringUtils.hasText(action) && !Set.of("grant", "revoke", "apply_preset").contains(action)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "action不合法");
        }

        String propertyCompanyId = roleGuard.isPlatformAdmin()
                ? null
                : UserContext.currentPropertyCompanyId();

        LocalDate start = adminSupport.parseDate(startDate);
        LocalDate end = adminSupport.parseDate(endDate);
        LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = end != null ? end.plusDays(1).atStartOfDay() : null;

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<PermissionChangeLog> logPage = permissionChangeLogRepository.findAll(
                PermissionChangeLogSpecifications.withFilters(
                        propertyCompanyId, residentId, operatorId, action, startDateTime, endDateTime),
                pageable);

        Set<String> residentIds = new HashSet<>();
        logPage.getContent().forEach(log -> {
            residentIds.add(log.getResidentId());
            residentIds.add(log.getOperatorId());
        });
        Map<String, Resident> residentMap = residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, resident -> resident));

        List<PermissionChangeLogItemResponse> list = logPage.getContent().stream()
                .map(log -> toChangeLogItem(log, residentMap))
                .toList();

        return PageResponse.of(list, page, pageSize, logPage.getTotalElements());
    }

    private List<RolePreset> loadPresetsByScope(String scope) {
        if ("platform".equals(scope)) {
            return rolePresetRepository.findByPropertyCompanyIdIsNull();
        }
        if ("property".equals(scope)) {
            if (roleGuard.isPlatformAdmin()) {
                return rolePresetRepository.findByPropertyCompanyIdIsNotNull();
            }
            return rolePresetRepository.findByPropertyCompanyId(UserContext.currentPropertyCompanyId());
        }
        if (roleGuard.isPlatformAdmin()) {
            return rolePresetRepository.findAll();
        }
        List<RolePreset> merged = new ArrayList<>(rolePresetRepository.findByPropertyCompanyIdIsNull());
        merged.addAll(rolePresetRepository.findByPropertyCompanyId(UserContext.currentPropertyCompanyId()));
        return merged;
    }

    private RolePreset getAccessiblePreset(String id) {
        RolePreset preset = rolePresetRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PERMISSION_PRESET_NOT_FOUND));
        if (!roleGuard.isPlatformAdmin()
                && preset.getPropertyCompanyId() != null
                && !preset.getPropertyCompanyId().equals(UserContext.currentPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_PRESET_NOT_FOUND);
        }
        return preset;
    }

    private Resident getAccessibleResident(String userId) {
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        return resident;
    }

    private String resolvePresetPropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPropertyAdmin()) {
            return UserContext.currentPropertyCompanyId();
        }
        return requestedPropertyCompanyId;
    }

    private void validatePresetPermissionCodes(List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING, "permissionCodes不能为空");
        }
        for (String code : permissionCodes) {
            resolveGrantablePermission(code);
        }
    }

    private Permission resolveGrantablePermission(String rawCode) {
        String normalized = PermissionGuard.normalizeCode(rawCode);
        Permission permission = permissionRepository.findByCode(normalized)
                .orElseThrow(() -> new BizException(ErrorCode.PERMISSION_NOT_FOUND));
        if (roleGuard.isPropertyAdmin() && permissionGuard.isPlatformOnlyPermission(permission.getCode())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return permission;
    }

    private void replacePresetPermissions(String presetId, List<String> permissionCodes) {
        rolePresetPermissionRepository.findByPresetId(presetId).forEach(rolePresetPermissionRepository::delete);
        for (String code : permissionCodes) {
            Permission permission = resolveGrantablePermission(code);
            RolePresetPermission link = new RolePresetPermission();
            link.setId(idGenerator.generateId("rpp"));
            link.setPresetId(presetId);
            link.setPermissionId(permission.getId());
            rolePresetPermissionRepository.save(link);
        }
    }

    private String resolvePresetCode(String requestedCode, String name) {
        if (StringUtils.hasText(requestedCode)) {
            String code = requestedCode.trim();
            if (rolePresetRepository.findByCode(code).isPresent()) {
                throw new BizException(ErrorCode.PARAM_INVALID, "预设编码已存在");
            }
            return code;
        }
        String base = name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
        if (!StringUtils.hasText(base)) {
            base = "preset";
        }
        String code = base;
        int suffix = 1;
        while (rolePresetRepository.findByCode(code).isPresent()) {
            code = base + "_" + suffix++;
        }
        return code;
    }

    private void writePermissionChangeLog(
            String residentId,
            String propertyCompanyId,
            String operatorId,
            String action,
            String permissionCode,
            String presetCode,
            String beforeValue,
            String afterValue,
            String reason) {
        PermissionChangeLog log = new PermissionChangeLog();
        log.setId(idGenerator.generateId("pcl"));
        log.setResidentId(residentId);
        log.setOperatorId(operatorId);
        log.setPropertyCompanyId(propertyCompanyId);
        log.setAction(action);
        log.setPermissionCode(toApiPermissionCode(permissionCode));
        log.setPresetCode(presetCode);
        log.setBeforeValue(beforeValue);
        log.setAfterValue(reason != null ? afterValue + "|reason=" + reason : afterValue);
        permissionChangeLogRepository.save(log);
    }

    private PermissionItemResponse toPermissionItem(Permission permission) {
        return PermissionItemResponse.builder()
                .id(permission.getId())
                .code(toApiPermissionCode(permission.getCode()))
                .name(permission.getName())
                .module(permission.getModule())
                .description(permission.getDescription())
                .isPlatformOnly(permissionGuard.isPlatformOnlyPermission(permission.getCode()))
                .status(permission.getStatus())
                .build();
    }

    private RolePresetItemResponse toRolePresetItem(RolePreset preset) {
        List<String> permissionCodes = rolePresetPermissionRepository.findByPresetId(preset.getId()).stream()
                .map(link -> permissionRepository.findById(link.getPermissionId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Permission::getCode)
                .map(this::toApiPermissionCode)
                .toList();

        return RolePresetItemResponse.builder()
                .id(preset.getId())
                .name(preset.getName())
                .code(preset.getCode())
                .description(preset.getDescription())
                .propertyCompanyId(preset.getPropertyCompanyId())
                .isBuiltin(preset.getPropertyCompanyId() == null)
                .permissionCodes(permissionCodes)
                .permissionCount(permissionCodes.size())
                .createdAt(DateTimeUtil.toIsoUtc(preset.getCreatedAt()))
                .build();
    }

    private PermissionChangeLogItemResponse toChangeLogItem(
            PermissionChangeLog log,
            Map<String, Resident> residentMap) {
        String afterValue = log.getAfterValue();
        String reason = null;
        if (afterValue != null && afterValue.contains("|reason=")) {
            int index = afterValue.indexOf("|reason=");
            reason = afterValue.substring(index + "|reason=".length());
            afterValue = afterValue.substring(0, index);
        }

        Resident target = residentMap.get(log.getResidentId());
        Resident operator = residentMap.get(log.getOperatorId());
        return PermissionChangeLogItemResponse.builder()
                .id(log.getId())
                .residentId(log.getResidentId())
                .residentName(target != null ? target.getName() : null)
                .operatorId(log.getOperatorId())
                .operatorName(operator != null ? operator.getName() : null)
                .action(log.getAction())
                .permissionCode(log.getPermissionCode())
                .presetCode(log.getPresetCode())
                .beforeValue(log.getBeforeValue())
                .afterValue(afterValue)
                .reason(reason)
                .createdAt(DateTimeUtil.toIsoUtc(log.getCreatedAt()))
                .build();
    }

    private String toApiPermissionCode(String code) {
        if (!StringUtils.hasText(code)) {
            return code;
        }
        return code.replace('.', ':');
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
