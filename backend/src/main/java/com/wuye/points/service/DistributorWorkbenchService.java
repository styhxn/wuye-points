package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.residentmerchant.CreateDistributorProductRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distributor.AdjustDistributorProductStockRequest;
import com.wuye.points.dto.distributor.DistributorIdentityResponse;
import com.wuye.points.dto.distributor.DistributorMyProductItemResponse;
import com.wuye.points.dto.distributor.DistributorPickupItemResponse;
import com.wuye.points.dto.distributor.UpdateDistributorProductRequest;
import com.wuye.points.entity.DistributorProduct;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantListing;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentMerchantListingRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SensitiveMasker;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 一级货源经销商手机端工作台
 * 资格 = merchants.is_distributor=1（后台开通）且审核通过；已放宽，不再限定 merchant 角色。
 * 任意已登录、且名下拥有已开通一级代理权限商家载体（merchant）的注册用户，均可操作自己的货源商品。
 * 非商家角色的一级代理商家载体由注册入口创建（见开通逻辑）。
 */
@Service
@RequiredArgsConstructor
public class DistributorWorkbenchService {

    private final MerchantRepository merchantRepository;
    private final DistributorProductRepository distributorProductRepository;
    private final ResidentMerchantListingRepository listingRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final WithdrawableService withdrawableService;

    /**
     * 解析当前登录用户的一级代理身份：以名下商家 is_distributor=1 判定（后台开通）。
     * 已去除 merchant 角色限制——任意已登录用户，只要其名下存在已开通一级代理且审核通过的商家载体即可。
     */
    private Merchant resolveCurrentDistributor() {
        // 放宽：一级代理不再限定 merchant 角色，任意已登录注册用户（其名下有 is_distributor=1 商家载体）均可操作
        roleGuard.requireAnyRole("resident", "merchant", "courier", "technician",
                "coordinator", "sector_leader", "individual_leader", "activity_leader",
                "property_admin", "platform_admin", "community_admin");
        List<Merchant> merchants = merchantRepository.findByResidentId(UserContext.currentUserId());
        return merchants.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsDistributor()))
                .filter(m -> "approved".equals(m.getAuditStatus()))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.DISTRIBUTOR_NOT_ENABLED, "当前商家未开通一级代理权限"));
    }

    /**
     * 当前用户一级代理身份查询：不抛 60030，未开通时返回 isDistributor=false（HTTP 200 + code 0）。
     * 「没有一级代理身份」是正常结果，不是权限错误。
     */
    @Transactional(readOnly = true)
    public DistributorIdentityResponse getMyIdentity() {
        roleGuard.requireAnyRole("resident", "merchant", "courier", "technician",
                "coordinator", "sector_leader", "individual_leader", "activity_leader",
                "property_admin", "platform_admin", "community_admin");
        String userId = UserContext.currentUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Merchant merchant = merchantRepository.findByResidentId(userId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsDistributor()))
                .filter(m -> "approved".equals(m.getAuditStatus()))
                .findFirst()
                .orElse(null);
        if (merchant == null) {
            return DistributorIdentityResponse.builder().isDistributor(false).build();
        }
        WithdrawableService.MerchantWithdrawable w = withdrawableService.calcMerchantWithdrawable(merchant);
        return DistributorIdentityResponse.builder()
                .isDistributor(true)
                .distributorMerchantId(merchant.getId())
                .merchantName(merchant.getName())
                .auditStatus(merchant.getAuditStatus())
                .withdrawableAmount(w.withdrawableAmount())
                .withdrawalBlocked(w.withdrawalBlocked())
                .totalRevenue(w.totalRevenue())
                .totalOrders(w.totalOrders())
                .build();
    }

    @Transactional
    public DistributorMyProductItemResponse createMyProduct(CreateDistributorProductRequest request) {
        Merchant distributor = resolveCurrentDistributor();
        DistributorProduct product = new DistributorProduct();
        product.setId(idGenerator.generateId("dpr"));
        product.setDistributorMerchantId(distributor.getId());
        product.setName(request.getName().trim());
        product.setCoverUrl(request.getCoverUrl());
        product.setDescription(request.getDescription());
        product.setWholesalePrice(request.getWholesalePrice());
        product.setSuggestedRetailPrice(request.getSuggestedRetailPrice());
        product.setStock(request.getStock() == null ? 0 : request.getStock());
        product.setStatus("active");
        if (StringUtils.hasText(request.getCategory())) {
            product.setCategory(request.getCategory());
        } else if (StringUtils.hasText(request.getCategoryId())) {
            product.setCategory(request.getCategoryId());
        }
        if (StringUtils.hasText(request.getServiceScope())) {
            if (!Set.of("city", "community").contains(request.getServiceScope())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "serviceScope 必须为 city(本市) 或 community(小区)");
            }
            product.setServiceScope(request.getServiceScope());
        }
        return toItem(distributorProductRepository.save(product), 0);
    }

    @Transactional(readOnly = true)
    public PageResponse<DistributorMyProductItemResponse> listMyProducts(int page, int pageSize) {
        Merchant distributor = resolveCurrentDistributor();
        int size = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DistributorProduct> result =
                distributorProductRepository.findByDistributorMerchantIdAndStatusNotOrderByCreatedAtDesc(
                        distributor.getId(), "deleted", pageable);

        Map<String, Long> pickupCounts = countPickups(
                result.getContent().stream().map(DistributorProduct::getId).toList());
        List<DistributorMyProductItemResponse> items = result.getContent().stream()
                .map(p -> toItem(p, pickupCounts.getOrDefault(p.getId(), 0L)))
                .toList();
        return PageResponse.of(items, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public DistributorMyProductItemResponse updateMyProduct(
            String productId, UpdateDistributorProductRequest request) {
        Merchant distributor = resolveCurrentDistributor();
        DistributorProduct product = loadOwnedProduct(distributor.getId(), productId);
        assertNotDeleted(product, "修改");
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName().trim());
        }
        if (request.getCoverUrl() != null) {
            product.setCoverUrl(request.getCoverUrl());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getWholesalePrice() != null) {
            product.setWholesalePrice(request.getWholesalePrice());
        }
        if (request.getSuggestedRetailPrice() != null) {
            product.setSuggestedRetailPrice(request.getSuggestedRetailPrice());
        }
        if (StringUtils.hasText(request.getStatus())) {
            if (!Set.of("active", "inactive").contains(request.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status 必须为 active 或 inactive");
            }
            product.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getServiceScope())) {
            if (!Set.of("city", "community").contains(request.getServiceScope())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "serviceScope 必须为 city(本市) 或 community(小区)");
            }
            product.setServiceScope(request.getServiceScope());
        }
        if (StringUtils.hasText(request.getCategory())) {
            product.setCategory(request.getCategory());
        }
        long pickupCount = countPickups(List.of(product.getId())).getOrDefault(product.getId(), 0L);
        return toItem(distributorProductRepository.save(product), pickupCount);
    }

    @Transactional
    public DistributorMyProductItemResponse adjustMyProductStock(
            String productId, AdjustDistributorProductStockRequest request) {
        Merchant distributor = resolveCurrentDistributor();
        DistributorProduct product = loadOwnedProduct(distributor.getId(), productId);
        assertNotDeleted(product, "调整库存");
        int newStock = defaultStock(product) + request.getDelta();
        if (newStock < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "库存不足，无法扣减");
        }
        product.setStock(newStock);
        long pickupCount = countPickups(List.of(product.getId())).getOrDefault(product.getId(), 0L);
        return toItem(distributorProductRepository.save(product), pickupCount);
    }

    /**
     * 经销商删除商品（软删除）：仅将 status 置为 deleted。
     * 商品从经销商列表与选品池移除，但下游拿货记录、订单历史数据保留不受影响。
     */
    @Transactional
    public void deleteMyProduct(String productId) {
        Merchant distributor = resolveCurrentDistributor();
        DistributorProduct product = loadOwnedProduct(distributor.getId(), productId);
        assertNotDeleted(product, "删除");
        product.setStatus("deleted");
        distributorProductRepository.save(product);
    }

    /**
     * 发货/拿货查询：下游业主商户对本经销商商品的拿货上架记录。
     */
    @Transactional(readOnly = true)
    public List<DistributorPickupItemResponse> listMyPickups() {
        Merchant distributor = resolveCurrentDistributor();
        List<String> productIds = distributorProductRepository
                .findByDistributorMerchantId(distributor.getId()).stream()
                .map(DistributorProduct::getId)
                .toList();
        if (productIds.isEmpty()) {
            return List.of();
        }
        Map<String, DistributorProduct> productMap = distributorProductRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(DistributorProduct::getId, p -> p));
        List<ResidentMerchantListing> listings =
                listingRepository.findByDistributorProductIdInOrderByCreatedAtDesc(productIds);
        Map<String, Resident> residentMap = loadResidents(listings.stream()
                .map(ResidentMerchantListing::getResidentId)
                .distinct()
                .toList());
        return listings.stream()
                .map(listing -> {
                    Resident picker = residentMap.get(listing.getResidentId());
                    return DistributorPickupItemResponse.builder()
                            .listingId(listing.getId())
                            .residentMerchantId(listing.getResidentMerchantId())
                            .residentId(listing.getResidentId())
                            .residentName(picker != null ? maskName(picker.getName()) : null)
                            .residentPhone(picker != null ? SensitiveMasker.maskPhone(picker.getPhone()) : null)
                            .distributorProductId(listing.getDistributorProductId())
                            .productName(productMap.get(listing.getDistributorProductId()) != null
                                    ? productMap.get(listing.getDistributorProductId()).getName() : null)
                            .retailPrice(listing.getRetailPrice())
                            .enabled(listing.getEnabled())
                            .shareCount(listing.getShareCount())
                            .createdAt(listing.getCreatedAt())
                            .build();
                })
                .toList();
    }

    private Map<String, Resident> loadResidents(List<String> residentIds) {
        if (residentIds == null || residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, r -> r, (a, b) -> a));
    }

    /** 姓名脱敏：张阿姨 → 张*姨（单字名仅保留首字） */
    private String maskName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * 经销商取消某业主商户的拿货：仅退回未卖出件数（quantity_remaining）到货源库存，已卖出部分不退。
     */
    @Transactional
    public void cancelMyPickup(String listingId) {
        Merchant distributor = resolveCurrentDistributor();
        ResidentMerchantListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "拿货记录不存在"));
        DistributorProduct product = distributorProductRepository.findById(listing.getDistributorProductId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!distributor.getId().equals(product.getDistributorMerchantId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权取消其他经销商商品的拿货");
        }
        int remaining = listing.getQuantityRemaining() == null ? 0 : listing.getQuantityRemaining();
        if (remaining > 0) {
            product.setStock((product.getStock() != null ? product.getStock() : 0) + remaining);
            distributorProductRepository.save(product);
        }
        listingRepository.delete(listing);
    }

    private DistributorProduct loadOwnedProduct(String distributorMerchantId, String productId) {
        DistributorProduct product = distributorProductRepository.findById(productId)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!distributorMerchantId.equals(product.getDistributorMerchantId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其他经销商的商品");
        }
        return product;
    }

    /** 已软删除（status=deleted）的商品视为不存在，禁止继续编辑/补货/重复删除 */
    private void assertNotDeleted(DistributorProduct product, String action) {
        if ("deleted".equals(product.getStatus())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "商品已删除，无法" + action);
        }
    }

    private Map<String, Long> countPickups(List<String> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return listingRepository.findByDistributorProductIdInOrderByCreatedAtDesc(productIds).stream()
                .collect(Collectors.groupingBy(ResidentMerchantListing::getDistributorProductId,
                        Collectors.counting()));
    }

    private int defaultStock(DistributorProduct product) {
        return product.getStock() != null ? product.getStock() : 0;
    }

    private DistributorMyProductItemResponse toItem(DistributorProduct product, long pickupCount) {
        return DistributorMyProductItemResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .coverUrl(product.getCoverUrl())
                .description(product.getDescription())
                .wholesalePrice(product.getWholesalePrice())
                .suggestedRetailPrice(product.getSuggestedRetailPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .serviceScope(product.getServiceScope())
                .category(product.getCategory())
                .pickupCount(pickupCount)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
