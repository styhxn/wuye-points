package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.product.CreateProductRequest;
import com.wuye.points.dto.product.DeleteProductResponse;
import com.wuye.points.dto.product.ProductDetailResponse;
import com.wuye.points.dto.product.ProductListItemResponse;
import com.wuye.points.dto.product.UpdateProductRequest;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.ProductSpecifications;
import com.wuye.points.util.DateTimeUtil;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "price", "stock", "name");

    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ProductConverter productConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<ProductListItemResponse> listProducts(
            int page,
            int pageSize,
            String keyword,
            String merchantId,
            String category,
            String status,
            String propertyCompanyId,
            String sort) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateListFilters(merchantId, status);

        String scopedPropertyCompanyId = resolveBrowsePropertyCompanyId(propertyCompanyId);
        boolean includeMemberPrice = UserContext.currentUserId() != null;
        boolean publicOnly = !canManageAnyProduct();

        if (publicOnly && StringUtils.hasText(status) && !"active".equals(status)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<Product> productPage = productRepository.findAll(
                ProductSpecifications.withFilters(
                        scopedPropertyCompanyId, merchantId, keyword, category,
                        publicOnly ? null : status, publicOnly),
                pageable);

        Map<String, Merchant> merchantMap = loadMerchantMap(productPage.getContent());
        List<ProductListItemResponse> list = productPage.getContent().stream()
                .map(product -> productConverter.toListItem(
                        product, merchantMap.get(product.getMerchantId()), includeMemberPrice))
                .toList();

        return PageResponse.of(list, page, pageSize, productPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(String id) {
        if (!id.startsWith("prd_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品ID格式不正确");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        Merchant merchant = merchantRepository.findById(product.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        assertCanViewProduct(product, merchant);

        boolean includeMemberPrice = UserContext.currentUserId() != null;
        return productConverter.toDetail(product, merchant, includeMemberPrice);
    }

    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request) {
        roleGuard.requireAnyRole("merchant");
        validateCreateRequest(request);

        Merchant merchant = resolveTargetMerchant(request.getMerchantId());
        assertCanManageMerchantProducts(merchant);
        assertMerchantCanSell(merchant);

        Product product = new Product();
        product.setId(idGenerator.generateId("prd"));
        product.setMerchantId(merchant.getId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCoverUrl(request.getCoverUrl());
        product.setPrice(request.getPrice());
        product.setMemberPrice(request.getMemberPrice());
        product.setPointPrice(request.getPointPrice());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setCategory(request.getCategory());
        product.setStatus(resolveStatus(request.getStatus(), product.getStock()));
        productRepository.save(product);

        return productConverter.toDetail(product, merchant, true);
    }

    @Transactional
    public ProductDetailResponse updateProduct(String id, UpdateProductRequest request) {
        if (!id.startsWith("prd_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品ID格式不正确");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        Merchant merchant = merchantRepository.findById(product.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        assertCanManageMerchantProducts(merchant);
        assertProductOwnedByMerchant(product, merchant);
        validateUpdateRequest(request, product);

        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCoverUrl() != null) {
            product.setCoverUrl(request.getCoverUrl());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getMemberPrice() != null) {
            product.setMemberPrice(request.getMemberPrice());
        }
        if (request.getPointPrice() != null) {
            product.setPointPrice(request.getPointPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (StringUtils.hasText(request.getStatus())) {
            product.setStatus(request.getStatus());
        } else if (request.getStock() != null) {
            product.setStatus(resolveStatus(product.getStatus(), product.getStock()));
        }

        productRepository.save(product);
        return productConverter.toDetail(product, merchant, true);
    }

    @Transactional
    public DeleteProductResponse deleteProduct(String id) {
        if (!id.startsWith("prd_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品ID格式不正确");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        Merchant merchant = merchantRepository.findById(product.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        assertCanManageMerchantProducts(merchant);
        assertProductOwnedByMerchant(product, merchant);

        product.setStatus("inactive");
        productRepository.save(product);

        return DeleteProductResponse.builder()
                .id(product.getId())
                .status(product.getStatus())
                .deletedAt(DateTimeUtil.nowIsoUtc())
                .build();
    }

    private void assertCanViewProduct(Product product, Merchant merchant) {
        if (canManageProduct(product, merchant)) {
            return;
        }
        if (!"active".equals(product.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "商品已下架");
        }
        if (!"approved".equals(merchant.getAuditStatus()) || !"active".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        String viewerPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (viewerPropertyCompanyId != null
                && !viewerPropertyCompanyId.equals(merchant.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private void assertCanManageMerchantProducts(Merchant merchant) {
        if (roleGuard.isPlatformAdmin() || roleGuard.isPropertyAdmin()) {
            if (roleGuard.isPropertyAdmin()) {
                roleGuard.assertSamePropertyOrPlatform(merchant.getPropertyCompanyId());
            }
            return;
        }
        if (roleGuard.isMerchant() && isOwnMerchant(merchant)) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertProductOwnedByMerchant(Product product, Merchant merchant) {
        if (!merchant.getId().equals(product.getMerchantId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private boolean canManageProduct(Product product, Merchant merchant) {
        if (roleGuard.isPlatformAdmin()) {
            return merchant.getId().equals(product.getMerchantId());
        }
        if (roleGuard.isPropertyAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (currentPropertyCompanyId == null
                    || !currentPropertyCompanyId.equals(merchant.getPropertyCompanyId())) {
                return false;
            }
            return merchant.getId().equals(product.getMerchantId());
        }
        if (roleGuard.isMerchant() && isOwnMerchant(merchant)) {
            return merchant.getId().equals(product.getMerchantId());
        }
        return false;
    }

    private boolean canManageAnyProduct() {
        return roleGuard.isPlatformAdmin() || roleGuard.isPropertyAdmin() || roleGuard.isMerchant();
    }

    private void assertMerchantCanSell(Merchant merchant) {
        if (!"approved".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_AUDITING);
        }
        if (!"active".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
    }

    private Merchant resolveTargetMerchant(String merchantId) {
        if (StringUtils.hasText(merchantId)) {
            if (!merchantId.startsWith("mch_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "merchantId格式不正确");
            }
            return merchantRepository.findById(merchantId)
                    .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        }
        return resolveOwnedMerchant();
    }

    private Merchant resolveOwnedMerchant() {
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
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

    private void validateListFilters(String merchantId, String status) {
        if (StringUtils.hasText(merchantId) && !merchantId.startsWith("mch_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "merchantId格式不正确");
        }
        if (StringUtils.hasText(status) && !RoleGuard.PRODUCT_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
    }

    private void validateCreateRequest(CreateProductRequest request) {
        if (StringUtils.hasText(request.getStatus())
                && !RoleGuard.PRODUCT_STATUS.contains(request.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
        if (request.getMemberPrice() != null
                && request.getMemberPrice().compareTo(request.getPrice()) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "业主价不能高于原价");
        }
    }

    private void validateUpdateRequest(UpdateProductRequest request, Product product) {
        if (StringUtils.hasText(request.getStatus())
                && !RoleGuard.PRODUCT_STATUS.contains(request.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
        if (request.getMemberPrice() != null) {
            BigDecimal price = request.getPrice() != null ? request.getPrice() : product.getPrice();
            if (request.getMemberPrice().compareTo(price) > 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "业主价不能高于原价");
            }
        }
    }

    private String resolveStatus(String requestedStatus, Integer stock) {
        if (StringUtils.hasText(requestedStatus)) {
            return requestedStatus;
        }
        if (stock != null && stock <= 0) {
            return "sold_out";
        }
        return "active";
    }

    private Map<String, Merchant> loadMerchantMap(List<Product> products) {
        List<String> merchantIds = products.stream()
                .map(Product::getMerchantId)
                .distinct()
                .toList();
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity()));
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        return PageRequest.of(page - 1, pageSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Order.desc("createdAt"));
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
