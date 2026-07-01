package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.CreateMerchantRequest;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.dto.merchant.MerchantListItemResponse;
import com.wuye.points.dto.merchant.UpdateMerchantRequest;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PlatformMerchant;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.MerchantSpecifications;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class MerchantService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "createdAt", "rankOrder", "levelWeight", "name");
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.1000");
    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");

    private final MerchantRepository merchantRepository;
    private final PlatformMerchantRepository platformMerchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final ProductRepository productRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ResidentRepository residentRepository;
    private final MerchantConverter merchantConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<MerchantListItemResponse> listMerchants(
            int page,
            int pageSize,
            String keyword,
            String category,
            String merchantLevel,
            String propertyCompanyId,
            String sort) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateListFilters(category, merchantLevel);

        String scopedPropertyCompanyId = resolveBrowsePropertyCompanyId(propertyCompanyId);
        boolean includeMemberInfo = UserContext.currentUserId() != null;
        boolean publicOnly = !isAdminUser();

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<Merchant> merchantPage = merchantRepository.findAll(
                MerchantSpecifications.withFilters(
                        scopedPropertyCompanyId, keyword, category, merchantLevel, publicOnly),
                pageable);

        Map<String, MerchantPropertyConfig> configMap = loadConfigMap(
                merchantPage.getContent(), scopedPropertyCompanyId);
        List<MerchantListItemResponse> list = merchantPage.getContent().stream()
                .map(merchant -> merchantConverter.toListItem(
                        merchant, configMap.get(merchant.getId()), includeMemberInfo))
                .toList();

        return PageResponse.of(list, page, pageSize, merchantPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public MerchantDetailResponse getMerchant(String id) {
        if (!id.startsWith("mch_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商家ID格式不正确");
        }

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        assertCanViewMerchant(merchant);

        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), merchant.getPropertyCompanyId())
                .orElse(null);
        List<Product> products = productRepository.findTop20ByMerchantIdAndStatusOrderByCreatedAtDesc(
                merchant.getId(), "active");
        boolean includeMemberInfo = UserContext.currentUserId() != null;

        return merchantConverter.toDetail(merchant, config, products, includeMemberInfo);
    }

    @Transactional
    public MerchantDetailResponse applyMerchant(CreateMerchantRequest request) {
        roleGuard.requireAnyRole("resident");
        validateCreateRequest(request);

        Resident applicant = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        String propertyCompanyId = applicant.getPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "请先绑定所属物业公司");
        }
        propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "物业公司不存在"));

        assertNoDuplicateApplication(request.getContactPhone(), propertyCompanyId);

        PlatformMerchant platformMerchant = new PlatformMerchant();
        platformMerchant.setId(idGenerator.generateId("pm"));
        platformMerchant.setName(request.getName());
        platformMerchant.setDescription(request.getDescription());
        platformMerchant.setCategory(request.getCategory());
        platformMerchant.setContactPhone(request.getContactPhone());
        platformMerchant.setAddress(request.getAddress());
        platformMerchant.setBusinessHours(request.getBusinessHours());
        if (request.getCoverUrls() != null && !request.getCoverUrls().isEmpty()) {
            platformMerchant.setCoverUrl(request.getCoverUrls().get(0));
        }
        platformMerchant.setStatus("active");
        platformMerchantRepository.save(platformMerchant);

        Merchant merchant = new Merchant();
        merchant.setId(idGenerator.generateId("mch"));
        merchant.setPlatformMerchantId(platformMerchant.getId());
        merchant.setPropertyCompanyId(propertyCompanyId);
        merchant.setName(request.getName());
        merchant.setDescription(request.getDescription());
        merchant.setCoverUrls(merchantConverter.serializeCoverUrls(request.getCoverUrls()));
        merchant.setVideoUrl(request.getVideoUrl());
        merchant.setMerchantLevel("normal");
        merchant.setLevelWeight(merchantConverter.levelWeightOf("normal"));
        merchant.setCategory(request.getCategory());
        merchant.setAuditStatus("pending_audit");
        merchant.setRankOrder(0);
        merchant.setBusinessHours(request.getBusinessHours());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setAddress(request.getAddress());
        merchant.setDeliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO);
        merchant.setFreeDeliveryThreshold(request.getFreeDeliveryThreshold());
        merchant.setStatus("active");
        merchantRepository.save(merchant);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(idGenerator.generateId("mpc"));
        config.setMerchantId(merchant.getId());
        config.setPropertyCompanyId(propertyCompanyId);
        config.setCommissionRate(resolveCommissionRate(request.getCommissionRate()));
        config.setPointExchangeRate(resolvePointExchangeRate(request.getPointExchangeRate()));
        config.setCoinRebateRate(BigDecimal.ZERO);
        config.setCoinRebateEnabled(false);
        config.setStatus("active");
        merchantPropertyConfigRepository.save(config);

        return merchantConverter.toDetail(merchant, config, List.of(), true);
    }

    @Transactional
    public MerchantDetailResponse updateMerchant(String id, UpdateMerchantRequest request) {
        if (!id.startsWith("mch_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商家ID格式不正确");
        }

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        assertCanManageMerchant(merchant);
        validateUpdateRequest(request);

        if (StringUtils.hasText(request.getName())) {
            merchant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            merchant.setDescription(request.getDescription());
        }
        if (request.getCoverUrls() != null) {
            merchant.setCoverUrls(merchantConverter.serializeCoverUrls(request.getCoverUrls()));
        }
        if (request.getVideoUrl() != null) {
            merchant.setVideoUrl(request.getVideoUrl());
        }
        if (request.getBusinessHours() != null) {
            merchant.setBusinessHours(request.getBusinessHours());
        }
        if (StringUtils.hasText(request.getContactPhone())) {
            merchant.setContactPhone(request.getContactPhone());
        }
        if (request.getAddress() != null) {
            merchant.setAddress(request.getAddress());
        }
        if (request.getDeliveryFee() != null) {
            merchant.setDeliveryFee(request.getDeliveryFee());
        }
        if (request.getFreeDeliveryThreshold() != null) {
            merchant.setFreeDeliveryThreshold(request.getFreeDeliveryThreshold());
        }
        if (request.getRankOrder() != null) {
            if (!roleGuard.isPlatformAdmin() && !roleGuard.isPropertyAdmin()) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            merchant.setRankOrder(request.getRankOrder());
        }
        if (StringUtils.hasText(request.getMerchantLevel())) {
            if (!roleGuard.isPlatformAdmin()) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "仅平台管理员可设置商家分级");
            }
            if (!RoleGuard.MERCHANT_LEVELS.contains(request.getMerchantLevel())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "merchantLevel值无效");
            }
            if ("official_certified".equals(request.getMerchantLevel()) && !roleGuard.isPlatformAdmin()) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            merchant.setMerchantLevel(request.getMerchantLevel());
            merchant.setLevelWeight(merchantConverter.levelWeightOf(request.getMerchantLevel()));
        }

        merchantRepository.save(merchant);

        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), merchant.getPropertyCompanyId())
                .orElse(null);
        List<Product> products = productRepository.findTop20ByMerchantIdAndStatusOrderByCreatedAtDesc(
                merchant.getId(), "active");

        return merchantConverter.toDetail(merchant, config, products, true);
    }

    @Transactional(readOnly = true)
    public MerchantDetailResponse getMyMerchant() {
        roleGuard.requireAnyRole("merchant");
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }

        Merchant merchant = merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), merchant.getPropertyCompanyId())
                .orElse(null);
        List<Product> products = productRepository.findByMerchantId(merchant.getId());

        return merchantConverter.toDetail(merchant, config, products, true);
    }

    private void assertCanViewMerchant(Merchant merchant) {
        if (isAdminUser() || isOwnMerchant(merchant)) {
            return;
        }
        if (!"approved".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_AUDITING);
        }
        if (!"active".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        String viewerPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (viewerPropertyCompanyId != null
                && !viewerPropertyCompanyId.equals(merchant.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
    }

    private void assertCanManageMerchant(Merchant merchant) {
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (roleGuard.isPropertyAdmin()) {
            roleGuard.assertSamePropertyOrPlatform(merchant.getPropertyCompanyId());
            return;
        }
        if (roleGuard.isMerchant() && isOwnMerchant(merchant)) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private boolean isOwnMerchant(Merchant merchant) {
        if (!roleGuard.isMerchant()) {
            return false;
        }
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId()).orElse(null);
        if (merchantUser == null || !StringUtils.hasText(merchantUser.getPhone())) {
            return false;
        }
        return merchantUser.getPhone().equals(merchant.getContactPhone())
                && merchantUser.getPropertyCompanyId().equals(merchant.getPropertyCompanyId());
    }

    private boolean isAdminUser() {
        return roleGuard.isPlatformAdmin() || roleGuard.isPropertyAdmin();
    }

    private String resolveBrowsePropertyCompanyId(String propertyCompanyId) {
        String contextPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (StringUtils.hasText(contextPropertyCompanyId)) {
            if (roleGuard.isPlatformAdmin() && StringUtils.hasText(propertyCompanyId)) {
                validatePropertyCompanyId(propertyCompanyId);
                return propertyCompanyId;
            }
            return contextPropertyCompanyId;
        }
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }
        validatePropertyCompanyId(propertyCompanyId);
        return propertyCompanyId;
    }

    private void validatePropertyCompanyId(String propertyCompanyId) {
        if (!propertyCompanyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
        }
        propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "物业公司不存在"));
    }

    private void validateListFilters(String category, String merchantLevel) {
        if (StringUtils.hasText(category) && !RoleGuard.MERCHANT_CATEGORIES.contains(category)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "category值无效");
        }
        if (StringUtils.hasText(merchantLevel) && !RoleGuard.MERCHANT_LEVELS.contains(merchantLevel)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "merchantLevel值无效");
        }
    }

    private void validateCreateRequest(CreateMerchantRequest request) {
        if (!RoleGuard.MERCHANT_CATEGORIES.contains(request.getCategory())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "category值无效");
        }
        if (request.getCommissionRate() != null
                && (request.getCommissionRate().compareTo(BigDecimal.ZERO) < 0
                || request.getCommissionRate().compareTo(BigDecimal.ONE) > 0)) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "commissionRate需在0-1之间");
        }
        if (request.getPointExchangeRate() != null
                && request.getPointExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "pointExchangeRate需大于0");
        }
    }

    private void validateUpdateRequest(UpdateMerchantRequest request) {
        if (StringUtils.hasText(request.getMerchantLevel())
                && !RoleGuard.MERCHANT_LEVELS.contains(request.getMerchantLevel())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "merchantLevel值无效");
        }
    }

    private void assertNoDuplicateApplication(String contactPhone, String propertyCompanyId) {
        List<Merchant> existing = merchantRepository.findByContactPhoneAndPropertyCompanyId(
                contactPhone, propertyCompanyId);
        boolean hasActiveApplication = existing.stream()
                .anyMatch(m -> "pending_audit".equals(m.getAuditStatus())
                        || "approved".equals(m.getAuditStatus()));
        if (hasActiveApplication) {
            throw new BizException(ErrorCode.MERCHANT_DUPLICATE_BINDING);
        }
    }

    private BigDecimal resolveCommissionRate(BigDecimal requested) {
        return requested != null ? requested : DEFAULT_COMMISSION_RATE;
    }

    private BigDecimal resolvePointExchangeRate(BigDecimal requested) {
        return requested != null ? requested : DEFAULT_POINT_EXCHANGE_RATE;
    }

    private Map<String, MerchantPropertyConfig> loadConfigMap(List<Merchant> merchants,
                                                              String propertyCompanyId) {
        return merchants.stream()
                .map(m -> merchantPropertyConfigRepository
                        .findByMerchantIdAndPropertyCompanyId(m.getId(), propertyCompanyId)
                        .map(config -> Map.entry(m.getId(), config))
                        .orElse(null))
                .filter(entry -> entry != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Sort sortOrder = parseSort(sort);
        return PageRequest.of(page - 1, pageSize, sortOrder);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Order.desc("rankOrder"), Sort.Order.desc("levelWeight"));
        }
        String[] parts = sort.split(",");
        Sort.Order[] orders = new Sort.Order[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            boolean desc = part.startsWith("-");
            String field = desc ? part.substring(1) : (part.startsWith("+") ? part.substring(1) : part);
            if (!SORTABLE_FIELDS.contains(field)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "sort字段不支持: " + field);
            }
            orders[i] = desc ? Sort.Order.desc(field) : Sort.Order.asc(field);
        }
        return Sort.by(orders);
    }
}
