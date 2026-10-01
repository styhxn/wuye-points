package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.residentmerchant.AuditResidentMerchantApplicationRequest;
import com.wuye.points.dto.admin.residentmerchant.ConfirmResidentMerchantDepositRequest;
import com.wuye.points.dto.admin.residentmerchant.CreateDistributorProductRequest;
import com.wuye.points.dto.admin.residentmerchant.CreateDistributorProductResponse;
import com.wuye.points.dto.admin.residentmerchant.DeductResidentMerchantDepositRequest;
import com.wuye.points.dto.admin.residentmerchant.DistributorProductListItemResponse;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantApplicationListItemResponse;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantDepositListItemResponse;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantSettingsResponse;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantSettlementItemResponse;
import com.wuye.points.dto.admin.residentmerchant.UpdateBillingCycleRequest;
import com.wuye.points.dto.admin.residentmerchant.UpdateDistributorProductRequest;
import com.wuye.points.dto.admin.residentmerchant.UpdateDistributorProductResponse;
import com.wuye.points.dto.admin.residentmerchant.UpdateBillingCycleResponse;
import com.wuye.points.dto.admin.residentmerchant.UpdateResidentMerchantSettingsRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.DistributorProduct;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantApplication;
import com.wuye.points.entity.ResidentMerchantDeposit;
import com.wuye.points.entity.ResidentMerchantSettlement;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.ResidentMerchantApplicationRepository;
import com.wuye.points.repository.ResidentMerchantDepositRepository;
import com.wuye.points.repository.ResidentMerchantSettlementRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.ResidentMerchantApplicationSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminResidentMerchantService {

    private static final Set<String> AUDIT_STATUSES = Set.of("approved", "rejected");
    private static final Set<String> BILLING_CYCLE_VALUES = Set.of("one_time", "monthly", "quarterly", "yearly");

    private final ResidentMerchantApplicationRepository applicationRepository;
    private final ResidentMerchantDepositRepository depositRepository;
    private final ResidentMerchantSettlementRepository settlementRepository;
    private final DistributorProductRepository distributorProductRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final PlatformSettingService platformSettingService;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<ResidentMerchantApplicationListItemResponse> listApplications(
            int page, int pageSize, String status, String keyword, String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        String resolvedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        if (!StringUtils.hasText(status)) {
            status = "pending_audit";
        }

        List<String> residentIdsByKeyword = null;
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            residentIdsByKeyword = residentRepository.findAll((root, query, cb) -> cb.or(
                    cb.like(root.get("name"), like),
                    cb.like(root.get("phone"), like)))
                    .stream()
                    .map(Resident::getId)
                    .toList();
        }

        Specification<ResidentMerchantApplication> spec = ResidentMerchantApplicationSpecifications.withFilters(
                resolvedPropertyCompanyId, status, residentIdsByKeyword);
        Page<ResidentMerchantApplication> result = applicationRepository.findAll(spec, pageable);

        Map<String, String> residentNames = residentRepository.findAllById(
                result.getContent().stream().map(ResidentMerchantApplication::getResidentId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Resident::getId, r -> StringUtils.hasText(r.getName())
                        ? r.getName() : r.getPhone(), (a, b) -> a));

        List<ResidentMerchantApplicationListItemResponse> list = result.getContent().stream()
                .map(a -> toApplicationItem(a, residentNames.get(a.getResidentId())))
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public ResidentMerchantApplicationListItemResponse auditApplication(
            String id, AuditResidentMerchantApplicationRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String auditStatus = request.resolveAuditStatus();
        if (!StringUtils.hasText(auditStatus) || !AUDIT_STATUSES.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditResult须为 approved/rejected");
        }

        ResidentMerchantApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "申请不存在"));
        adminSupport.assertSameProperty(application.getPropertyCompanyId());
        if (!"pending_audit".equals(application.getStatus())) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
        }

        application.setAuditRemark(request.resolveAuditRemark());
        application.setAuditedBy(UserContext.currentUserId());
        application.setAuditedAt(LocalDateTime.now());
        if ("approved".equals(auditStatus)) {
            application.setStatus("active");
        } else {
            application.setStatus("closed");
        }
        applicationRepository.save(application);

        Resident resident = residentRepository.findById(application.getResidentId()).orElse(null);
        return toApplicationItem(application, resident == null ? null : resident.getName());
    }

    /**
     * 管理端确认保证金已缴纳（线下收款通道）。
     *
     * <p>线上微信支付不可用或用户付款失败时，申请会长期停在 {@code pending_deposit}，
     * 既无法继续支付也无法进入审核。物业线下收取保证金后调用本接口确认收款，
     * 保证金置 frozen，申请推进到 {@code pending_audit}（{@code directActivate=true} 时直接 active）。
     *
     * <p>幂等：已处于 pending_audit/active 时直接返回当前状态。
     */
    @Transactional
    public ResidentMerchantApplicationListItemResponse confirmDeposit(
            String id, ConfirmResidentMerchantDepositRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        ResidentMerchantApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "申请不存在"));
        adminSupport.assertSameProperty(application.getPropertyCompanyId());

        String status = application.getStatus();
        if ("pending_audit".equals(status) || "active".equals(status)) {
            return toApplicationItem(application, loadResidentName(application.getResidentId()));
        }
        if (!"pending_deposit".equals(status)) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED,
                    "仅待缴保证金的申请可确认收款（当前状态：" + status + "）");
        }

        // 作废该申请下未完成的线上预下单流水，避免线下收款后又收到微信回调造成重复入账
        closePendingDepositPayments(application.getId());

        String paymentMethod = request.resolvePaymentMethod();
        ResidentMerchantDeposit deposit = depositRepository.findByApplicationId(application.getId()).orElse(null);
        if (deposit == null) {
            deposit = new ResidentMerchantDeposit();
            deposit.setId(idGenerator.generateId("rmd"));
            deposit.setApplicationId(application.getId());
            deposit.setResidentId(application.getResidentId());
            deposit.setAmount(application.getDepositAmount());
        }
        deposit.setStatus("frozen");
        deposit.setPaymentMethod(paymentMethod);
        deposit.setPaidAt(LocalDateTime.now());
        depositRepository.save(deposit);

        boolean directActivate = Boolean.TRUE.equals(request.getDirectActivate());
        String remark = StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null;
        if (directActivate) {
            application.setStatus("active");
            application.setAuditRemark(remark != null ? remark : "线下缴纳保证金，管理端确认并直接开通");
            application.setAuditedBy(UserContext.currentUserId());
            application.setAuditedAt(LocalDateTime.now());
        } else {
            application.setStatus("pending_audit");
        }
        applicationRepository.save(application);
        log.info("管理端确认保证金 applicationId={} residentId={} method={} directActivate={} remark={}",
                application.getId(), application.getResidentId(), paymentMethod, directActivate, remark);

        return toApplicationItem(application, loadResidentName(application.getResidentId()));
    }

    private void closePendingDepositPayments(String applicationId) {
        List<PaymentRecord> pending = paymentRecordRepository.findByBizTypeAndBizIdAndStatus(
                PaymentBizType.RESIDENT_SHOP_DEPOSIT, applicationId, "pending");
        for (PaymentRecord record : pending) {
            record.setStatus("closed");
            record.setErrorMsg("管理端已线下确认收款，原线上流水作废");
            paymentRecordRepository.save(record);
        }
    }

    private String loadResidentName(String residentId) {
        return residentRepository.findById(residentId)
                .map(r -> StringUtils.hasText(r.getName()) ? r.getName() : r.getPhone())
                .orElse(null);
    }

    private ResidentMerchantApplicationListItemResponse toApplicationItem(
            ResidentMerchantApplication application, String residentName) {
        return ResidentMerchantApplicationListItemResponse.builder()
                .id(application.getId())
                .residentId(application.getResidentId())
                .residentName(residentName)
                .propertyCompanyId(application.getPropertyCompanyId())
                .depositAmount(application.getDepositAmount())
                .status(application.getStatus())
                .statusCode(application.getStatus())
                .visibility(application.getVisibility())
                .createdAt(DateTimeUtil.toLocalDateTimeString(application.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ResidentMerchantDepositListItemResponse> listDeposits(
            int page, int pageSize, String status, String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        String resolvedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);

        Specification<ResidentMerchantDeposit> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (StringUtils.hasText(resolvedPropertyCompanyId)) {
                var appSub = query.from(ResidentMerchantApplication.class);
                predicates.add(cb.and(
                        cb.equal(root.get("applicationId"), appSub.get("id")),
                        cb.equal(appSub.get("propertyCompanyId"), resolvedPropertyCompanyId)));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<ResidentMerchantDeposit> result = depositRepository.findAll(spec, pageable);

        Map<String, String> residentNames = residentRepository.findAllById(
                result.getContent().stream().map(ResidentMerchantDeposit::getResidentId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Resident::getId, r -> StringUtils.hasText(r.getName())
                        ? r.getName() : r.getPhone(), (a, b) -> a));

        List<ResidentMerchantDepositListItemResponse> list = result.getContent().stream()
                .map(d -> ResidentMerchantDepositListItemResponse.builder()
                        .id(d.getId())
                        .applicationId(d.getApplicationId())
                        .residentId(d.getResidentId())
                        .residentName(residentNames.get(d.getResidentId()))
                        .amount(d.getAmount())
                        .status(d.getStatus())
                        .paymentMethod(d.getPaymentMethod())
                        .paidAt(DateTimeUtil.toLocalDateTimeString(d.getPaidAt()))
                        .deductedAmount(d.getDeductedAmount())
                        .build())
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public ResidentMerchantDepositListItemResponse deductDeposit(
            String id, DeductResidentMerchantDepositRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        ResidentMerchantDeposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_MERCHANT_DEPOSIT_INVALID));
        if (!"frozen".equals(deposit.getStatus())) {
            throw new BizException(ErrorCode.RESIDENT_MERCHANT_DEPOSIT_INVALID);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "扣除金额无效");
        }

        BigDecimal deducted = deposit.getDeductedAmount() == null ? BigDecimal.ZERO : deposit.getDeductedAmount();
        deposit.setDeductedAmount(deducted.add(request.getAmount()));
        deposit.setDeductReason(request.getReason());
        if (deposit.getDeductedAmount().compareTo(deposit.getAmount()) >= 0) {
            deposit.setStatus("deducted");
        }
        depositRepository.save(deposit);

        Resident resident = residentRepository.findById(deposit.getResidentId()).orElse(null);
        return ResidentMerchantDepositListItemResponse.builder()
                .id(deposit.getId())
                .applicationId(deposit.getApplicationId())
                .residentId(deposit.getResidentId())
                .residentName(resident == null ? null : StringUtils.hasText(resident.getName())
                        ? resident.getName() : resident.getPhone())
                .amount(deposit.getAmount())
                .status(deposit.getStatus())
                .paymentMethod(deposit.getPaymentMethod())
                .paidAt(DateTimeUtil.toLocalDateTimeString(deposit.getPaidAt()))
                .deductedAmount(deposit.getDeductedAmount())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ResidentMerchantSettlementItemResponse> listSettlements(
            int page, int pageSize, String startDate, String endDate) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        LocalDateTime start = startDate == null ? null : adminSupport.parseIsoDateTime(startDate);
        LocalDateTime end = endDate == null ? null : adminSupport.parseIsoDateTime(endDate);
        Page<ResidentMerchantSettlement> result;
        if (start != null && end != null) {
            result = settlementRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end, pageable);
        } else {
            result = settlementRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        List<ResidentMerchantSettlementItemResponse> list = result.getContent().stream()
                .map(s -> ResidentMerchantSettlementItemResponse.builder()
                        .id(s.getId())
                        .orderId(s.getOrderId())
                        .listingId(s.getListingId())
                        .residentId(s.getResidentId())
                        .orderAmount(s.getOrderAmount())
                        .resellerProfit(s.getResellerProfit())
                        .status(s.getStatus())
                        .settleAt(DateTimeUtil.toLocalDateTimeString(s.getSettleAt()))
                        .createdAt(DateTimeUtil.toLocalDateTimeString(s.getCreatedAt()))
                        .build())
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ResidentMerchantSettingsResponse getSettings() {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        return ResidentMerchantSettingsResponse.builder()
                .defaultDepositAmount(platformSettingService.getResidentMerchantDefaultDeposit())
                .platformCommissionRate(platformSettingService.getResidentMerchantCommissionRate())
                .distributorProductCommissionRate(
                        platformSettingService.getResidentMerchantDistributorCommissionRate())
                .refundWindowDays(platformSettingService.getResidentMerchantRefundWindowDays())
                .markupMinRate(platformSettingService.getResidentMerchantMarkupMinRate())
                .markupMaxRate(platformSettingService.getResidentMerchantMarkupMaxRate())
                .build();
    }

    @Transactional
    public ResidentMerchantSettingsResponse updateSettings(UpdateResidentMerchantSettingsRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        try {
            platformSettingService.updateResidentMerchantSettings(
                    request.getDefaultDepositAmount(),
                    request.getPlatformCommissionRate(),
                    request.getRefundWindowDays(),
                    request.getMarkupMinRate(),
                    request.getMarkupMaxRate());
            if (request.getDistributorProductCommissionRate() != null) {
                platformSettingService.setResidentMerchantDistributorCommissionRate(
                        request.getDistributorProductCommissionRate());
            }
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, ex.getMessage());
        }
        return getSettings();
    }

    @Transactional
    public CreateDistributorProductResponse createDistributorProduct(CreateDistributorProductRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Merchant distributor = merchantRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("merchantLevel"), "official_certified"),
                cb.equal(root.get("auditStatus"), "approved")))
                .stream()
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "未找到可用的一级经销商商家"));

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
        distributorProductRepository.save(product);
        return CreateDistributorProductResponse.builder().id(product.getId()).build();
    }

    @Transactional(readOnly = true)
    public PageResponse<DistributorProductListItemResponse> listDistributorProducts(
            int page, int pageSize, String keyword, String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);

        List<String> merchantIds = null;
        if (StringUtils.hasText(scopedPropertyCompanyId)) {
            String scoped = scopedPropertyCompanyId;
            merchantIds = merchantRepository.findAll((root, query, cb) ->
                            cb.equal(root.get("propertyCompanyId"), scoped))
                    .stream()
                    .map(Merchant::getId)
                    .toList();
            if (merchantIds.isEmpty()) {
                return PageResponse.of(List.of(), page, pageSize, 0);
            }
        }

        final List<String> scopedMerchantIds = merchantIds;
        Specification<DistributorProduct> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (scopedMerchantIds != null) {
                predicates.add(root.get("distributorMerchantId").in(scopedMerchantIds));
            }
            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(root.get("name"), "%" + keyword.trim() + "%"));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<DistributorProduct> result = distributorProductRepository.findAll(spec, pageable);

        Map<String, String> merchantPropertyMap = loadMerchantPropertyMap(result.getContent());
        List<DistributorProductListItemResponse> list = result.getContent().stream()
                .map(product -> DistributorProductListItemResponse.builder()
                        .id(product.getId())
                        .distributorMerchantId(product.getDistributorMerchantId())
                        .propertyCompanyId(merchantPropertyMap.get(product.getDistributorMerchantId()))
                        .name(product.getName())
                        .coverUrl(product.getCoverUrl())
                        .wholesalePrice(product.getWholesalePrice())
                        .suggestedRetailPrice(product.getSuggestedRetailPrice())
                        .stock(product.getStock())
                        .billingCycle(product.getBillingCycle())
                        .category(product.getCategory())
                        .status(product.getStatus())
                        .createdAt(DateTimeUtil.toIsoUtc(product.getCreatedAt()))
                        .build())
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    private Map<String, String> loadMerchantPropertyMap(List<DistributorProduct> products) {
        List<String> ids = products.stream()
                .map(DistributorProduct::getDistributorMerchantId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return merchantRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Merchant::getId, Merchant::getPropertyCompanyId, (a, b) -> a));
    }

    @Transactional
    public UpdateBillingCycleResponse updateBillingCycle(String id, UpdateBillingCycleRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "merchant");
        if (!BILLING_CYCLE_VALUES.contains(request.getBillingCycle())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "billingCycle不合法");
        }
        if (!StringUtils.hasText(id) || !id.startsWith("dpr_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "分销商品ID格式不正确");
        }

        DistributorProduct product = distributorProductRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "分销商品不存在"));

        product.setBillingCycle(request.getBillingCycle());
        distributorProductRepository.save(product);
        return UpdateBillingCycleResponse.builder()
                .id(product.getId())
                .billingCycle(product.getBillingCycle())
                .build();
    }

    /**
     * 更新分销商品（2026-09-07 前端问题 H）：改名称/封面/价格/库存/描述/服务区间/上下架。
     * 全部字段可选，仅更新传入字段。
     */
    @Transactional
    public UpdateDistributorProductResponse updateDistributorProduct(
            String id, UpdateDistributorProductRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        DistributorProduct product = getDistributorProductOrThrow(id);

        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName().trim());
        }
        if (request.getCoverUrl() != null) {
            product.setCoverUrl(request.getCoverUrl());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getWholesalePrice() != null) {
            if (request.getWholesalePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "wholesalePrice不能为负数");
            }
            product.setWholesalePrice(request.getWholesalePrice());
        }
        if (request.getSuggestedRetailPrice() != null) {
            if (request.getSuggestedRetailPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "suggestedRetailPrice不能为负数");
            }
            product.setSuggestedRetailPrice(request.getSuggestedRetailPrice());
        }
        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "stock不能为负数");
            }
            product.setStock(request.getStock());
        }
        if (request.getServiceScope() != null) {
            product.setServiceScope(request.getServiceScope());
        }
        if (request.getInactive() != null) {
            product.setStatus(Boolean.TRUE.equals(request.getInactive()) ? "inactive" : "active");
        }
        distributorProductRepository.save(product);
        return UpdateDistributorProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .wholesalePrice(product.getWholesalePrice())
                .suggestedRetailPrice(product.getSuggestedRetailPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .build();
    }

    /**
     * 删除分销商品（2026-09-07 前端问题 H）：软删（status=inactive），保留历史拿货记录可追溯。
     */
    @Transactional
    public UpdateDistributorProductResponse deleteDistributorProduct(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        DistributorProduct product = getDistributorProductOrThrow(id);
        product.setStatus("inactive");
        distributorProductRepository.save(product);
        return UpdateDistributorProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .status(product.getStatus())
                .build();
    }

    private DistributorProduct getDistributorProductOrThrow(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("dpr_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "分销商品ID格式不正确");
        }
        return distributorProductRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND, "分销商品不存在"));
    }
}
