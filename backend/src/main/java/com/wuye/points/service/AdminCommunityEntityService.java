package com.wuye.points.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.communityentity.BindPropertyCompanyRequest;
import com.wuye.points.dto.communityentity.CommunityEntityDetailResponse;
import com.wuye.points.dto.communityentity.CommunityEntityListItemResponse;
import com.wuye.points.dto.communityentity.CommunityPropertyBindingItemResponse;
import com.wuye.points.dto.communityentity.CreateCommunityEntityRequest;
import com.wuye.points.dto.communityentity.UpdateCommunityPropertyPermissionRequest;
import com.wuye.points.entity.CommunityEntity;
import com.wuye.points.entity.CommunityPropertyBinding;
import com.wuye.points.entity.CommunityPropertyPermission;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityEntityRepository;
import com.wuye.points.repository.CommunityPropertyBindingRepository;
import com.wuye.points.repository.CommunityPropertyPermissionRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.CommunityEntitySpecifications;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCommunityEntityService {

    private static final Set<String> PERMISSION_LEVELS = Set.of("read_only", "operate");
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final CommunityEntityRepository communityEntityRepository;
    private final CommunityPropertyBindingRepository bindingRepository;
    private final CommunityPropertyPermissionRepository permissionRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ResidentRepository residentRepository;
    private final PermissionGuard permissionGuard;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<CommunityEntityListItemResponse> listCommunityEntities(
            int page, int pageSize, String keyword) {
        permissionGuard.requirePermissionOrAdmin("community_entity.manage");
        roleGuard.requireAnyRole("platform_admin", "property_admin");

        String propertyCompanyId = roleGuard.isPlatformAdmin()
                ? null : UserContext.currentPropertyCompanyId();

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<CommunityEntity> entityPage = communityEntityRepository.findAll(
                CommunityEntitySpecifications.withFilters(keyword, propertyCompanyId),
                pageable);

        List<CommunityEntityListItemResponse> list = entityPage.getContent().stream()
                .map(this::toListItem)
                .toList();
        return PageResponse.of(list, page, pageSize, entityPage.getTotalElements());
    }

    @Transactional
    public CommunityEntityDetailResponse createCommunityEntity(CreateCommunityEntityRequest request) {
        permissionGuard.requirePermissionOrAdmin("community_entity.manage");
        roleGuard.requireAnyRole("platform_admin");

        if (StringUtils.hasText(request.getAdminResidentId())) {
            adminSupport.validateIdPrefix(request.getAdminResidentId(), "res_");
            residentRepository.findById(request.getAdminResidentId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        }

        CommunityEntity entity = new CommunityEntity();
        entity.setId(idGenerator.generateId("cent"));
        entity.setName(request.getName().trim());
        entity.setDescription(request.getDescription());
        entity.setAdminResidentId(request.getAdminResidentId());
        entity.setStatus("active");
        communityEntityRepository.save(entity);
        return toDetail(entity);
    }

    @Transactional(readOnly = true)
    public List<CommunityPropertyBindingItemResponse> listPropertyBindings(String communityEntityId) {
        permissionGuard.requirePermissionOrAdmin("community_entity.manage");
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        adminSupport.validateIdPrefix(communityEntityId, "cent_");

        CommunityEntity entity = getAccessibleEntity(communityEntityId);
        return bindingRepository.findByCommunityEntityId(entity.getId()).stream()
                .map(this::toBindingItem)
                .toList();
    }

    @Transactional
    public CommunityPropertyBindingItemResponse bindPropertyCompany(
            String communityEntityId, BindPropertyCompanyRequest request) {
        permissionGuard.requirePermissionOrAdmin("community_entity.manage");
        roleGuard.requireAnyRole("platform_admin");
        adminSupport.validateIdPrefix(communityEntityId, "cent_");
        adminSupport.validateIdPrefix(request.getPropertyCompanyId(), "pc_");

        CommunityEntity entity = communityEntityRepository.findById(communityEntityId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "社区不存在"));
        propertyCompanyRepository.findById(request.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        if (bindingRepository.existsByCommunityEntityIdAndPropertyCompanyId(
                communityEntityId, request.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该物业公司已绑定");
        }

        String permissionLevel = StringUtils.hasText(request.getPermissionLevel())
                ? request.getPermissionLevel() : "read_only";
        validatePermissionLevel(permissionLevel);

        CommunityPropertyBinding binding = new CommunityPropertyBinding();
        binding.setId(idGenerator.generateId("cpb"));
        binding.setCommunityEntityId(communityEntityId);
        binding.setPropertyCompanyId(request.getPropertyCompanyId());
        binding.setPermissionLevel(permissionLevel);
        binding.setBoundAt(LocalDateTime.now());
        bindingRepository.save(binding);

        CommunityPropertyPermission permission = new CommunityPropertyPermission();
        permission.setId(idGenerator.generateId("cpp"));
        permission.setBindingId(binding.getId());
        permission.setAllowedModules(null);
        permissionRepository.save(permission);

        return toBindingItem(binding);
    }

    @Transactional
    public void unbindPropertyCompany(String communityEntityId, String propertyCompanyId) {
        permissionGuard.requirePermissionOrAdmin("community_entity.manage");
        roleGuard.requireAnyRole("platform_admin");
        adminSupport.validateIdPrefix(communityEntityId, "cent_");
        adminSupport.validateIdPrefix(propertyCompanyId, "pc_");

        CommunityPropertyBinding binding = bindingRepository
                .findByCommunityEntityIdAndPropertyCompanyId(communityEntityId, propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "绑定关系不存在"));
        permissionRepository.findByBindingId(binding.getId()).ifPresent(permissionRepository::delete);
        bindingRepository.delete(binding);
    }

    @Transactional
    public CommunityPropertyBindingItemResponse updatePermissions(
            String communityEntityId,
            String propertyCompanyId,
            UpdateCommunityPropertyPermissionRequest request) {
        permissionGuard.requirePermissionOrAdmin("community_entity.manage");
        roleGuard.requireAnyRole("platform_admin");
        adminSupport.validateIdPrefix(communityEntityId, "cent_");
        adminSupport.validateIdPrefix(propertyCompanyId, "pc_");

        CommunityPropertyBinding binding = bindingRepository
                .findByCommunityEntityIdAndPropertyCompanyId(communityEntityId, propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "绑定关系不存在"));

        if (StringUtils.hasText(request.getPermissionLevel())) {
            validatePermissionLevel(request.getPermissionLevel());
            binding.setPermissionLevel(request.getPermissionLevel());
            bindingRepository.save(binding);
        }

        CommunityPropertyPermission permission = permissionRepository.findByBindingId(binding.getId())
                .orElseGet(() -> {
                    CommunityPropertyPermission created = new CommunityPropertyPermission();
                    created.setId(idGenerator.generateId("cpp"));
                    created.setBindingId(binding.getId());
                    return created;
                });
        if (request.getAllowedModules() != null) {
            permission.setAllowedModules(toJson(request.getAllowedModules()));
        }
        permissionRepository.save(permission);
        return toBindingItem(binding);
    }

    private CommunityEntity getAccessibleEntity(String id) {
        CommunityEntity entity = communityEntityRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "社区不存在"));
        if (!roleGuard.isPlatformAdmin()) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            boolean bound = bindingRepository.existsByCommunityEntityIdAndPropertyCompanyId(
                    id, propertyCompanyId);
            if (!bound) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        }
        return entity;
    }

    private CommunityEntityListItemResponse toListItem(CommunityEntity entity) {
        Resident admin = StringUtils.hasText(entity.getAdminResidentId())
                ? residentRepository.findById(entity.getAdminResidentId()).orElse(null) : null;
        int boundCount = bindingRepository.findByCommunityEntityId(entity.getId()).size();
        return CommunityEntityListItemResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .adminResidentId(entity.getAdminResidentId())
                .adminResidentName(admin != null ? admin.getName() : null)
                .status(entity.getStatus())
                .boundPropertyCount(boundCount)
                .createdAt(DateTimeUtil.toIsoUtc(entity.getCreatedAt()))
                .build();
    }

    private CommunityEntityDetailResponse toDetail(CommunityEntity entity) {
        Resident admin = StringUtils.hasText(entity.getAdminResidentId())
                ? residentRepository.findById(entity.getAdminResidentId()).orElse(null) : null;
        return CommunityEntityDetailResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .adminResidentId(entity.getAdminResidentId())
                .adminResidentName(admin != null ? admin.getName() : null)
                .status(entity.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(entity.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(entity.getUpdatedAt()))
                .build();
    }

    private CommunityPropertyBindingItemResponse toBindingItem(CommunityPropertyBinding binding) {
        PropertyCompany company = propertyCompanyRepository.findById(binding.getPropertyCompanyId()).orElse(null);
        List<String> allowedModules = permissionRepository.findByBindingId(binding.getId())
                .map(p -> parseStringList(p.getAllowedModules()))
                .orElse(List.of());
        return CommunityPropertyBindingItemResponse.builder()
                .bindingId(binding.getId())
                .propertyCompanyId(binding.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .permissionLevel(binding.getPermissionLevel())
                .allowedModules(allowedModules)
                .boundAt(DateTimeUtil.toIsoUtc(binding.getBoundAt()))
                .build();
    }

    private void validatePermissionLevel(String level) {
        if (!PERMISSION_LEVELS.contains(level)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "permissionLevel不合法");
        }
    }

    private String toJson(List<String> values) {
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
