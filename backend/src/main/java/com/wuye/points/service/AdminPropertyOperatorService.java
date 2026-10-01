package com.wuye.points.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertyoperator.CreatePropertyOperatorRequest;
import com.wuye.points.dto.propertyoperator.PropertyOperatorListItemResponse;
import com.wuye.points.dto.propertyoperator.UpdatePropertyOperatorScopeRequest;
import com.wuye.points.dto.propertyoperator.UpdatePropertyOperatorStatusRequest;
import com.wuye.points.entity.PropertyOperatorScope;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyOperatorScopeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PropertyOperatorScopeSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PasswordUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPropertyOperatorService {

    private static final Set<String> PROPERTY_SUB_ROLES = Set.of("property_leader", "property_operator");
    private static final Set<String> OPERATOR_STATUS = Set.of("active", "disabled");
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final PropertyOperatorScopeRepository scopeRepository;
    private final ResidentRepository residentRepository;
    private final PermissionGuard permissionGuard;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public PropertyOperatorListItemResponse createOperator(
            CreatePropertyOperatorRequest request, String queryPropertyCompanyId) {
        permissionGuard.requirePermissionOrAdmin("property_operator.manage");
        requirePropertyLeaderOrAdmin();

        String propertyCompanyId;
        if (roleGuard.isPlatformAdmin()) {
            propertyCompanyId = StringUtils.hasText(request.getPropertyCompanyId())
                    ? request.getPropertyCompanyId()
                    : queryPropertyCompanyId;
            if (!StringUtils.hasText(propertyCompanyId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
            }
            adminSupport.validateIdPrefix(propertyCompanyId, "pc_");
        } else {
            propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (!StringUtils.hasText(propertyCompanyId)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "当前用户未关联物业公司");
            }
        }
        final String resolvedPropertyCompanyId = propertyCompanyId;

        String subRole = StringUtils.hasText(request.getPropertySubRole())
                ? request.getPropertySubRole() : "property_operator";
        validateSubRole(subRole);

        Resident resident = residentRepository.findByPhone(request.getPhone().trim()).orElse(null);
        if (resident == null) {
            resident = new Resident();
            resident.setId(idGenerator.generateId("res"));
            resident.setPhone(request.getPhone().trim());
            resident.setName(request.getName().trim());
            resident.setPassword(PasswordUtil.hashPassword(request.getPassword()));
            resident.setRole("property_admin");
            resident.setPropertySubRole(subRole);
            resident.setPropertyCompanyId(resolvedPropertyCompanyId);
            resident.setUserType("owner");
            resident.setStatus("active");
            resident.setWechatBound(false);
            resident.setPointBalance(0);
            resident.setCoinBalance(BigDecimal.ZERO);
            resident.setCoinFrozen(false);
            resident.setCoinHidden(false);
            residentRepository.save(resident);
        } else {
            if (!resolvedPropertyCompanyId.equals(resident.getPropertyCompanyId())) {
                throw new BizException(ErrorCode.RESIDENT_DUPLICATE_PHONE);
            }
            resident.setName(request.getName().trim());
            resident.setRole("property_admin");
            resident.setPropertySubRole(subRole);
            if (StringUtils.hasText(request.getPassword())) {
                resident.setPassword(PasswordUtil.hashPassword(request.getPassword()));
            }
            residentRepository.save(resident);
        }

        final String residentId = resident.getId();
        PropertyOperatorScope scope = scopeRepository
                .findByResidentIdAndPropertyCompanyId(residentId, resolvedPropertyCompanyId)
                .orElse(null);
        if (scope == null) {
            scope = new PropertyOperatorScope();
            scope.setId(idGenerator.generateId("pos"));
            scope.setResidentId(residentId);
            scope.setPropertyCompanyId(resolvedPropertyCompanyId);
        }
        scope.setCommunityIds(toJson(request.getCommunityIds()));
        scope.setBuildingNos(toJson(request.getBuildingNos()));
        scopeRepository.save(scope);

        return toListItem(scope, resident);
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertyOperatorListItemResponse> listOperators(
            int page, int pageSize, String keyword, String status) {
        permissionGuard.requirePermissionOrAdmin("property_operator.manage");
        requirePropertyLeaderOrAdmin();

        if (StringUtils.hasText(status) && !OPERATOR_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }

        String propertyCompanyId = adminSupport.resolvePropertyCompanyId(null);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<PropertyOperatorScope> scopePage = scopeRepository.findAll(
                PropertyOperatorScopeSpecifications.withFilters(propertyCompanyId, keyword, status),
                pageable);

        Map<String, Resident> residentMap = loadResidents(scopePage.getContent().stream()
                .map(PropertyOperatorScope::getResidentId)
                .toList());

        List<PropertyOperatorListItemResponse> list = scopePage.getContent().stream()
                .map(scope -> toListItem(scope, residentMap.get(scope.getResidentId())))
                .toList();
        return PageResponse.of(list, page, pageSize, scopePage.getTotalElements());
    }

    @Transactional
    public PropertyOperatorListItemResponse updateScope(String id, UpdatePropertyOperatorScopeRequest request) {
        permissionGuard.requirePermissionOrAdmin("property_operator.manage");
        requirePropertyLeaderOrAdmin();
        adminSupport.validateIdPrefix(id, "pos_");

        PropertyOperatorScope scope = getAccessibleScope(id);
        if (request.getCommunityIds() != null) {
            scope.setCommunityIds(toJson(request.getCommunityIds()));
        }
        if (request.getBuildingNos() != null) {
            scope.setBuildingNos(toJson(request.getBuildingNos()));
        }
        scopeRepository.save(scope);

        Resident resident = residentRepository.findById(scope.getResidentId()).orElse(null);
        return toListItem(scope, resident);
    }

    @Transactional
    public PropertyOperatorListItemResponse updateStatus(String id, UpdatePropertyOperatorStatusRequest request) {
        permissionGuard.requirePermissionOrAdmin("property_operator.manage");
        requirePropertyLeaderOrAdmin();
        adminSupport.validateIdPrefix(id, "pos_");

        if (!OPERATOR_STATUS.contains(request.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }

        PropertyOperatorScope scope = getAccessibleScope(id);
        Resident resident = residentRepository.findById(scope.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        resident.setStatus(request.getStatus());
        residentRepository.save(resident);
        return toListItem(scope, resident);
    }

    private PropertyOperatorScope getAccessibleScope(String id) {
        PropertyOperatorScope scope = scopeRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "物业管理员不存在"));
        adminSupport.assertSameProperty(scope.getPropertyCompanyId());
        return scope;
    }

    /** 仅平台超管或物业领导可管理物业管理员账号（property_operator 不可） */
    private void requirePropertyLeaderOrAdmin() {
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (!roleGuard.isPropertyAdmin()) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        Resident current = residentRepository.findById(UserContext.currentUserId()).orElse(null);
        if (current == null) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        String role = current.getRole();
        if ("property_leader".equals(role)) {
            return;
        }
        if ("property_operator".equals(role)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        String subRole = current.getPropertySubRole();
        // 子角色为空：兼容老 property_admin，按领导放行
        if (!StringUtils.hasText(subRole) || "property_leader".equals(subRole)) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void validateSubRole(String subRole) {
        if (!PROPERTY_SUB_ROLES.contains(subRole)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertySubRole不合法");
        }
    }

    private PropertyOperatorListItemResponse toListItem(PropertyOperatorScope scope, Resident resident) {
        return PropertyOperatorListItemResponse.builder()
                .id(scope.getId())
                .residentId(scope.getResidentId())
                .name(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .propertySubRole(resident != null ? resident.getPropertySubRole() : null)
                .communityIds(parseStringList(scope.getCommunityIds()))
                .buildingNos(parseStringList(scope.getBuildingNos()))
                .status(resident != null ? resident.getStatus() : null)
                .createdAt(DateTimeUtil.toIsoUtc(scope.getCreatedAt()))
                .build();
    }

    private Map<String, Resident> loadResidents(List<String> residentIds) {
        if (residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, r -> r));
    }

    private String toJson(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "JSON序列化失败");
        }
    }

    private List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (Exception e) {
            return List.of();
        }
    }
}
