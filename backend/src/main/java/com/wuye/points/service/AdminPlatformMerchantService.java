package com.wuye.points.service;

import com.wuye.points.dto.admin.platformmerchant.CreatePlatformMerchantRequest;
import com.wuye.points.dto.admin.platformmerchant.PlatformMerchantDetailResponse;
import com.wuye.points.dto.admin.platformmerchant.PlatformMerchantListItemResponse;
import com.wuye.points.dto.admin.platformmerchant.UpdatePlatformMerchantRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.PlatformMerchant;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.spec.PlatformMerchantSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminPlatformMerchantService {

    private static final Set<String> VALID_STATUS = Set.of("active", "inactive");

    private final PlatformMerchantRepository platformMerchantRepository;
    private final MerchantRepository merchantRepository;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<PlatformMerchantListItemResponse> list(
            int page,
            int pageSize,
            String keyword,
            String category,
            String status,
            String linkedPropertyCompanyId) {
        permissionGuard.requirePlatformAdmin();

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Specification<PlatformMerchant> spec = PlatformMerchantSpecifications.withFilters(
                keyword, category, status, linkedPropertyCompanyId);
        Page<PlatformMerchant> merchantPage = platformMerchantRepository.findAll(spec, pageable);

        List<PlatformMerchantListItemResponse> list = merchantPage.getContent().stream()
                .map(this::toListItem)
                .toList();

        return PageResponse.of(list, page, pageSize, merchantPage.getTotalElements());
    }

    @Transactional
    public PlatformMerchantDetailResponse create(CreatePlatformMerchantRequest request) {
        permissionGuard.requirePlatformAdmin();
        validateCategory(request.getCategory());

        PlatformMerchant merchant = new PlatformMerchant();
        merchant.setId(idGenerator.generateId("pm"));
        merchant.setName(request.getName().trim());
        merchant.setCategory(request.getCategory());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setAddress(request.getAddress());
        merchant.setCoverUrl(request.getCoverUrl());
        merchant.setDescription(request.getDescription());
        merchant.setBusinessHours(request.getBusinessHours());
        merchant.setStatus("active");

        platformMerchantRepository.save(merchant);
        return toDetail(merchant);
    }

    @Transactional(readOnly = true)
    public PlatformMerchantDetailResponse getById(String id) {
        permissionGuard.requirePlatformAdmin();
        adminSupport.validateIdPrefix(id, "pm");
        PlatformMerchant merchant = platformMerchantRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "平台商家不存在"));
        return toDetail(merchant);
    }

    @Transactional
    public PlatformMerchantDetailResponse update(String id, UpdatePlatformMerchantRequest request) {
        permissionGuard.requirePlatformAdmin();
        adminSupport.validateIdPrefix(id, "pm");

        PlatformMerchant merchant = platformMerchantRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "平台商家不存在"));

        if (StringUtils.hasText(request.getName())) {
            merchant.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getCategory())) {
            validateCategory(request.getCategory());
            merchant.setCategory(request.getCategory());
        }
        if (StringUtils.hasText(request.getContactPhone())) {
            merchant.setContactPhone(request.getContactPhone());
        }
        if (request.getAddress() != null) {
            merchant.setAddress(request.getAddress());
        }
        if (request.getCoverUrl() != null) {
            merchant.setCoverUrl(request.getCoverUrl());
        }
        if (request.getDescription() != null) {
            merchant.setDescription(request.getDescription());
        }
        if (request.getBusinessHours() != null) {
            merchant.setBusinessHours(request.getBusinessHours());
        }
        if (StringUtils.hasText(request.getStatus())) {
            if (!VALID_STATUS.contains(request.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
            }
            merchant.setStatus(request.getStatus());
        }

        platformMerchantRepository.save(merchant);
        return toDetail(merchant);
    }

    private PlatformMerchantListItemResponse toListItem(PlatformMerchant merchant) {
        return PlatformMerchantListItemResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .category(merchant.getCategory())
                .coverUrl(merchant.getCoverUrl())
                .contactPhone(merchant.getContactPhone())
                .address(merchant.getAddress())
                .businessHours(merchant.getBusinessHours())
                .linkedPropertyCount(merchantRepository.countByPlatformMerchantId(merchant.getId()))
                .status(merchant.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(merchant.getCreatedAt()))
                .build();
    }

    private PlatformMerchantDetailResponse toDetail(PlatformMerchant merchant) {
        return PlatformMerchantDetailResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .category(merchant.getCategory())
                .coverUrl(merchant.getCoverUrl())
                .contactPhone(merchant.getContactPhone())
                .address(merchant.getAddress())
                .description(merchant.getDescription())
                .businessHours(merchant.getBusinessHours())
                .linkedPropertyCount(merchantRepository.countByPlatformMerchantId(merchant.getId()))
                .status(merchant.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(merchant.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(merchant.getUpdatedAt()))
                .build();
    }

    private void validateCategory(String category) {
        if (!StringUtils.hasText(category)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "品类不能为空");
        }
        if (!RoleGuard.MERCHANT_CATEGORIES.contains(category)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "category值无效");
        }
    }
}
