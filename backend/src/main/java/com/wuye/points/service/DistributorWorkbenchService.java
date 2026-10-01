package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.residentmerchant.CreateDistributorProductRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distributor.AdjustDistributorProductStockRequest;
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
 * 一级经销商手机端工作台
 * 经销商 = merchantLevel=official_certified 且审核通过的商家，以当前登录商家身份操作自己的货源商品。
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

    /**
     * 解析当前登录商家的一级经销商身份。
     */
    private Merchant resolveCurrentDistributor() {
        roleGuard.requireAnyRole("merchant");
        List<Merchant> merchants = merchantRepository.findByResidentId(UserContext.currentUserId());
        return merchants.stream()
                .filter(m -> "official_certified".equals(m.getMerchantLevel()))
                .filter(m -> "approved".equals(m.getAuditStatus()))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "当前商家不是已认证的一级经销商"));
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
     * 经销商取消某业主商户的拿货：名额释放后其他业主商户可继续拿货。
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
        product.setStock((product.getStock() != null ? product.getStock() : 0) + 1);
        distributorProductRepository.save(product);
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
