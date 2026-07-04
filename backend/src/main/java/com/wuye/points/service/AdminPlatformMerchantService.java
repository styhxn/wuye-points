package com.wuye.points.service;

import com.wuye.points.dto.admin.merchant.ProfitSpaceResponse;
import com.wuye.points.dto.admin.platformmerchant.BatchConfigRequest;
import com.wuye.points.dto.admin.platformmerchant.BatchConfigResponse;
import com.wuye.points.dto.admin.platformmerchant.CreatePlatformMerchantRequest;
import com.wuye.points.dto.admin.platformmerchant.LinkMerchantRequest;
import com.wuye.points.dto.admin.platformmerchant.LinkMerchantResponse;
import com.wuye.points.dto.admin.platformmerchant.PlatformMerchantDetailResponse;
import com.wuye.points.dto.admin.platformmerchant.PlatformMerchantListItemResponse;
import com.wuye.points.dto.admin.platformmerchant.UpdatePlatformMerchantRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PlatformMerchant;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
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

import com.wuye.points.context.UserContext;
import com.wuye.points.enums.AuditStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminPlatformMerchantService {

    private static final Set<String> VALID_STATUS = Set.of("active", "inactive");

    private final PlatformMerchantRepository platformMerchantRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
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

    @Transactional(readOnly = true)
    public ProfitSpaceResponse getProfitSpace(String merchantId, BigDecimal consumptionAmount) {
        permissionGuard.requirePlatformAdmin();

        com.wuye.points.entity.Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        String propertyCompanyId = merchant.getPropertyCompanyId();

        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchantId, propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "未配置物业参数"));

        PropertyCompany pc = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.SYSTEM_ERROR));

        BigDecimal commissionAmount = consumptionAmount.multiply(config.getCommissionRate());
        BigDecimal distributable = commissionAmount;

        return ProfitSpaceResponse.builder()
                .merchantId(merchantId)
                .merchantName(merchant.getName())
                .propertyCompanyId(propertyCompanyId)
                .consumptionAmount(consumptionAmount)
                .commissionRate(config.getCommissionRate())
                .commissionAmount(commissionAmount)
                .distributableAmount(distributable)
                .propertyShare(distributable.multiply(pc.getPropertyShareRate()))
                .coordinatorShare(distributable.multiply(pc.getCoordinatorShareRate()))
                .sectorLeaderShare(distributable.multiply(pc.getCoordinatorShareRate()).multiply(pc.getSectorLeaderRate()))
                .individualLeaderShare(distributable.multiply(pc.getCoordinatorShareRate()).multiply(pc.getSectorLeaderRate()).multiply(pc.getIndividualLeaderRate()))
                .build();
    }

    @Transactional
    public BatchConfigResponse batchSetParams(BatchConfigRequest request) {
        permissionGuard.requirePlatformAdmin();

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (BatchConfigRequest.MerchantConfigItem item : request.getItems()) {
            try {
                String merchantId = item.getMerchantId();
                String propertyCompanyId = item.getPropertyCompanyId();

                if (!StringUtils.hasText(merchantId) || !merchantId.startsWith("m_")) {
                    errors.add("merchantId格式不正确: " + merchantId);
                    failCount++;
                    continue;
                }
                if (!StringUtils.hasText(propertyCompanyId) || !propertyCompanyId.startsWith("pc_")) {
                    errors.add("propertyCompanyId格式不正确: " + propertyCompanyId);
                    failCount++;
                    continue;
                }

                com.wuye.points.entity.Merchant merchant = merchantRepository.findById(merchantId)
                        .orElse(null);
                if (merchant == null) {
                    errors.add("商家不存在: " + merchantId);
                    failCount++;
                    continue;
                }

                MerchantPropertyConfig config = merchantPropertyConfigRepository
                        .findByMerchantIdAndPropertyCompanyId(merchantId, propertyCompanyId)
                        .orElseGet(() -> {
                            MerchantPropertyConfig newConfig = new MerchantPropertyConfig();
                            newConfig.setId(idGenerator.generateId("mpc"));
                            newConfig.setMerchantId(merchantId);
                            newConfig.setPropertyCompanyId(propertyCompanyId);
                            newConfig.setStatus("active");
                            return newConfig;
                        });

                if (item.getCommissionRate() != null) {
                    config.setCommissionRate(item.getCommissionRate());
                }
                if (item.getPointExchangeRate() != null) {
                    config.setPointExchangeRate(item.getPointExchangeRate());
                }
                if (item.getMemberDiscountPrice() != null) {
                    config.setMemberDiscountPrice(item.getMemberDiscountPrice());
                }
                if (item.getCoinRebateRate() != null) {
                    config.setCoinRebateRate(item.getCoinRebateRate());
                }
                if (item.getCoinRebateEnabled() != null) {
                    config.setCoinRebateEnabled(item.getCoinRebateEnabled());
                }

                merchantPropertyConfigRepository.save(config);
                successCount++;
            } catch (Exception e) {
                errors.add("配置失败: " + e.getMessage());
                failCount++;
            }
        }

        return BatchConfigResponse.builder()
                .successCount(successCount)
                .failCount(failCount)
                .errors(errors)
                .build();
    }

    @Transactional
    public LinkMerchantResponse linkToProperty(String platformMerchantId, LinkMerchantRequest request) {
        permissionGuard.requirePlatformAdmin();
        adminSupport.validateIdPrefix(platformMerchantId, "pm");

        PlatformMerchant platformMerchant = platformMerchantRepository.findById(platformMerchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "平台商家不存在"));

        String propertyCompanyId = request.getPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId)) {
            propertyCompanyId = UserContext.currentPropertyCompanyId();
        }
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }

        PropertyCompany propertyCompany = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        boolean exists = merchantRepository.existsByPlatformMerchantIdAndPropertyCompanyId(
                platformMerchantId, propertyCompanyId);
        if (exists) {
            throw new BizException(ErrorCode.MERCHANT_DUPLICATE_BINDING);
        }

        Merchant merchant = new Merchant();
        merchant.setId(idGenerator.generateId("m"));
        merchant.setPlatformMerchantId(platformMerchantId);
        merchant.setPropertyCompanyId(propertyCompanyId);
        merchant.setName(platformMerchant.getName());
        merchant.setDescription(platformMerchant.getDescription());
        merchant.setCoverUrls(platformMerchant.getCoverUrl());
        merchant.setCategory(platformMerchant.getCategory());
        merchant.setBusinessHours(platformMerchant.getBusinessHours());
        merchant.setContactPhone(platformMerchant.getContactPhone());
        merchant.setAddress(platformMerchant.getAddress());
        merchant.setMerchantLevel(StringUtils.hasText(request.getMerchantLevel())
                ? request.getMerchantLevel() : "normal");
        merchant.setRankOrder(request.getRankOrder() != null ? request.getRankOrder() : 0);
        merchant.setDeliveryFee(request.getDeliveryFee());
        merchant.setFreeDeliveryThreshold(request.getFreeDeliveryThreshold());
        merchant.setAuditStatus(AuditStatus.approved.name());
        merchant.setStatus("active");
        merchantRepository.save(merchant);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(idGenerator.generateId("mpc"));
        config.setMerchantId(merchant.getId());
        config.setPropertyCompanyId(propertyCompanyId);
        config.setCommissionRate(request.getCommissionRate());
        config.setPointExchangeRate(request.getPointExchangeRate());
        config.setMemberDiscountPrice(request.getMemberDiscountPrice());
        config.setCoinRebateRate(request.getCoinRebateRate());
        config.setCoinRebateEnabled(Boolean.TRUE.equals(request.getCoinRebateEnabled()));
        config.setStatus("active");
        merchantPropertyConfigRepository.save(config);

        return LinkMerchantResponse.builder()
                .merchantId(merchant.getId())
                .platformMerchantId(platformMerchantId)
                .propertyCompanyId(propertyCompanyId)
                .auditStatus(merchant.getAuditStatus())
                .build();
    }
}
