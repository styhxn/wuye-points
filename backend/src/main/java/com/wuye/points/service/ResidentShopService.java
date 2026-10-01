package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distributor.DistributorProductItemResponse;
import com.wuye.points.dto.residentmerchant.ApplyResidentMerchantRequest;
import com.wuye.points.dto.residentmerchant.ApplyResidentMerchantResponse;
import com.wuye.points.dto.residentmerchant.CreateResidentMerchantListingRequest;
import com.wuye.points.dto.residentmerchant.MyResidentMerchantResponse;
import com.wuye.points.dto.residentmerchant.PayResidentMerchantDepositRequest;
import com.wuye.points.dto.residentmerchant.PayResidentMerchantDepositResponse;
import com.wuye.points.dto.residentmerchant.PublicResidentMerchantListingFlatItemResponse;
import com.wuye.points.dto.residentmerchant.PublicResidentMerchantListingItemResponse;
import com.wuye.points.dto.residentmerchant.PublicResidentMerchantShopResponse;
import com.wuye.points.dto.residentmerchant.ResidentMerchantListingItemResponse;
import com.wuye.points.dto.residentmerchant.ShareListingResponse;
import com.wuye.points.dto.residentmerchant.UpdateResidentMerchantListingRequest;
import com.wuye.points.dto.residentmerchant.UpdateResidentMerchantVisibilityRequest;
import com.wuye.points.entity.DistributorProduct;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantApplication;
import com.wuye.points.entity.ResidentMerchantDeposit;
import com.wuye.points.entity.ResidentMerchantListing;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.ResidentMerchantApplicationRepository;
import com.wuye.points.repository.ResidentMerchantDepositRepository;
import com.wuye.points.repository.ResidentMerchantListingRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 业主商户（二期）：路径 /resident-merchants
 * 与「消费过的商家」ResidentMerchantService 区分。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentShopService {

    private static final BigDecimal DEFAULT_DEPOSIT = new BigDecimal("500.00");
    private static final Set<String> DEPOSIT_PAYMENT_METHODS = Set.of("wechat", "mock");
    private static final String VISIBILITY_PUBLIC = "public";
    private static final String VISIBILITY_PRIVATE = "private";

    /**
     * 微信预下单流水超过该时长仍 pending，视为用户未付款成功，允许重新发起。
     * 定时兜底（PendingPaymentScheduler，30 分钟）太慢，此处做一次即时兜底，
     * 避免用户被「保证金支付处理中，请勿重复提交」长期卡死。
     */
    private static final int STALE_PENDING_MINUTES = 5;

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "pending_deposit", "待缴保证金",
            "pending_audit", "待审核",
            "active", "营业中",
            "suspended", "已暂停",
            "quitting", "退出冷却中",
            "closed", "已关闭");

    private final RoleGuard roleGuard;
    private final ResidentRepository residentRepository;
    private final ResidentMerchantApplicationRepository applicationRepository;
    private final ResidentMerchantDepositRepository depositRepository;
    private final ResidentMerchantListingRepository listingRepository;
    private final DistributorProductRepository distributorProductRepository;
    private final OrderRepository orderRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final PayChannel wechatPayService;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PlatformSettingService platformSettingService;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.share.base-url:https://test.yuanxusoftware.top/h5/shop}")
    private String shareBaseUrl;

    /** 模拟支付开关：生产必须关闭，否则任何人可零成本获得业主商户资格。 */
    @Value("${app.payment.mock-enabled:true}")
    private boolean mockPayEnabled;

    @Transactional
    public ApplyResidentMerchantResponse apply(ApplyResidentMerchantRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (!"owner".equals(resident.getUserType())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "仅业主可申请成为业主商户");
        }
        if (!StringUtils.hasText(resident.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "请先绑定所属物业公司");
        }

        ResidentMerchantApplication existing = applicationRepository.findByResidentId(resident.getId()).orElse(null);
        if (existing != null && !"closed".equals(existing.getStatus())) {
            return ApplyResidentMerchantResponse.builder()
                    .applicationId(existing.getId())
                    .depositAmount(existing.getDepositAmount())
                    .status(STATUS_LABELS.getOrDefault(existing.getStatus(), existing.getStatus()))
                    .statusCode(existing.getStatus())
                    .visibility(defaultVisibility(existing.getVisibility()))
                    .build();
        }

        ResidentMerchantApplication application = existing == null ? new ResidentMerchantApplication() : existing;
        if (existing == null) {
            application.setId(idGenerator.generateId("rma"));
            application.setResidentId(resident.getId());
        }
        application.setPropertyCompanyId(resident.getPropertyCompanyId());
        application.setDepositAmount(resolveDepositAmount());
        application.setStatus("pending_deposit");
        application.setVisibility(VISIBILITY_PRIVATE);
        application.setAuditRemark(null);
        application.setAuditedBy(null);
        application.setAuditedAt(null);
        application.setQuitRequestedAt(null);
        applicationRepository.save(application);

        return ApplyResidentMerchantResponse.builder()
                .applicationId(application.getId())
                .depositAmount(application.getDepositAmount())
                .status(STATUS_LABELS.get("pending_deposit"))
                .statusCode("pending_deposit")
                .visibility(VISIBILITY_PRIVATE)
                .build();
    }

    public PayResidentMerchantDepositResponse payDeposit(PayResidentMerchantDepositRequest request) {
        roleGuard.requireAnyRole("resident");
        if (!DEPOSIT_PAYMENT_METHODS.contains(request.getPaymentMethod())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod须为 wechat 或 mock");
        }
        if ("mock".equals(request.getPaymentMethod()) && !mockPayEnabled) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "当前环境未开启模拟支付，请使用微信支付，或联系物业线下缴纳保证金后由管理端确认");
        }
        Resident resident = getActiveResident(UserContext.currentUserId());
        ResidentMerchantApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "申请不存在"));
        if (!resident.getId().equals(application.getResidentId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        if (!"pending_deposit".equals(application.getStatus())) {
            throw new BizException(ErrorCode.RESIDENT_MERCHANT_DEPOSIT_INVALID);
        }

        // 微信真实支付：微信小程序预下单（事务外 HTTP，与订单支付同构），保证金保持待缴，支付成功后由回调确认
        if ("wechat".equals(request.getPaymentMethod())) {
            if (!wechatPayService.isEnabled()) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "在线支付未开通，请联系物业线下缴纳保证金后由管理端确认");
            }
            // 上一次预下单若已超过 STALE_PENDING_MINUTES 仍未支付，先作废旧流水，避免用户被永久卡在「支付处理中」
            closeStalePendingDeposits(application.getId());
            // 防重复提交：该申请已有 pending 流水时拒绝，避免重复扣款
            if (!paymentRecordRepository.findByBizTypeAndBizIdAndStatus(
                    PaymentBizType.RESIDENT_SHOP_DEPOSIT, application.getId(), "pending").isEmpty()) {
                throw new BizException(ErrorCode.PARAM_INVALID, "保证金支付处理中，请勿重复提交");
            }
            String openId = resolveOpenId(request.getOpenid(), resident); // 前置校验：openid 缺失时不落任何数据
            PaymentRecord record = wechatPayService.createIntent(
                    PaymentBizType.RESIDENT_SHOP_DEPOSIT, application.getId(), resident.getId(),
                    resident.getPropertyCompanyId(), "wechat",
                    application.getDepositAmount(), 0, BigDecimal.ZERO, application.getDepositAmount());
            WechatJsPayParams payParams = wechatPayService.miniPay(record, openId, "业主商户保证金");
            return PayResidentMerchantDepositResponse.builder()
                    .status(STATUS_LABELS.get("pending_deposit"))
                    .statusCode("pending_deposit")
                    .payStatus("pending")
                    .paymentRecordId(record.getId())
                    .payParams(payParams)
                    .build();
        }

        transactionTemplate.execute(status -> {
            ResidentMerchantDeposit deposit = depositRepository.findByApplicationId(application.getId()).orElse(null);
            if (deposit == null) {
                deposit = new ResidentMerchantDeposit();
                deposit.setId(idGenerator.generateId("rmd"));
                deposit.setApplicationId(application.getId());
                deposit.setResidentId(resident.getId());
                deposit.setAmount(application.getDepositAmount());
            }
            deposit.setStatus("frozen");
            deposit.setPaymentMethod(request.getPaymentMethod());
            deposit.setPaidAt(LocalDateTime.now());
            depositRepository.save(deposit);

            application.setStatus("pending_audit");
            applicationRepository.save(application);
            return null;
        });

        ResidentMerchantDeposit deposit = depositRepository.findByApplicationId(application.getId()).orElse(null);

        return PayResidentMerchantDepositResponse.builder()
                .depositId(deposit != null ? deposit.getId() : null)
                .status(STATUS_LABELS.get("pending_audit"))
                .statusCode("pending_audit")
                .payStatus("frozen")
                .build();
    }

    /**
     * 微信支付回调确认后冻结保证金并转待审核（幂等）。由 PaymentCompletionService 调用，无登录上下文。
     */
    @Transactional
    public void completeChannelPayment(PaymentRecord record) {
        ResidentMerchantApplication application = applicationRepository.findById(record.getBizId())
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND, "申请不存在"));
        if ("pending_audit".equals(application.getStatus())
                || "active".equals(application.getStatus())) {
            return; // 幂等
        }
        if (!"pending_deposit".equals(application.getStatus())) {
            throw new BizException(ErrorCode.RESIDENT_MERCHANT_DEPOSIT_INVALID);
        }

        ResidentMerchantDeposit deposit = depositRepository.findByApplicationId(application.getId()).orElse(null);
        if (deposit == null) {
            deposit = new ResidentMerchantDeposit();
            deposit.setId(idGenerator.generateId("rmd"));
            deposit.setApplicationId(application.getId());
            deposit.setResidentId(record.getResidentId());
            deposit.setAmount(application.getDepositAmount());
        }
        deposit.setStatus("frozen");
        deposit.setPaymentMethod("wechat");
        deposit.setPaidAt(LocalDateTime.now());
        depositRepository.save(deposit);

        application.setStatus("pending_audit");
        applicationRepository.save(application);
        log.info("微信回调完成保证金缴纳 applicationId={} recordId={}", application.getId(), record.getId());
    }

    /**
     * 保证金金额：优先取平台配置（管理端「业主商户配置」可调整），未配置时回落 500 元。
     */
    private BigDecimal resolveDepositAmount() {
        BigDecimal configured = platformSettingService.getResidentMerchantDefaultDeposit();
        if (configured == null || configured.compareTo(BigDecimal.ZERO) < 0) {
            return DEFAULT_DEPOSIT;
        }
        return configured;
    }

    /**
     * 作废该申请下超时未支付的保证金预下单流水，让用户可重新发起支付。
     * 兜底于 {@code PendingPaymentScheduler}（30 分钟），这里给出更短的即时重试窗口。
     */
    public void closeStalePendingDeposits(String applicationId) {
        List<PaymentRecord> stale = paymentRecordRepository.findByBizTypeAndBizIdAndStatus(
                        PaymentBizType.RESIDENT_SHOP_DEPOSIT, applicationId, "pending").stream()
                .filter(r -> r.getCreatedAt() != null
                        && r.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(STALE_PENDING_MINUTES)))
                .toList();
        for (PaymentRecord record : stale) {
            record.setStatus("closed");
            record.setErrorMsg("重新发起支付，原流水作废");
            paymentRecordRepository.save(record);
        }
        if (!stale.isEmpty()) {
            log.info("作废超时保证金支付流水 applicationId={} count={}", applicationId, stale.size());
        }
    }

    private String resolveOpenId(String requestOpenId, Resident resident) {
        if (StringUtils.hasText(requestOpenId)) {
            return requestOpenId;
        }
        if (StringUtils.hasText(resident.getOpenid())) {
            return resident.getOpenid();
        }
        throw new BizException(ErrorCode.PARAM_MISSING, "openid不能为空（微信小程序支付需要）");
    }

    @Transactional
    public MyResidentMerchantResponse quit() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        ResidentMerchantApplication application = applicationRepository.findByResidentId(resident.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_MERCHANT_DEPOSIT_INVALID));
        if (!"active".equals(application.getStatus()) && !"suspended".equals(application.getStatus())) {
            throw new BizException(ErrorCode.RESIDENT_MERCHANT_DEPOSIT_INVALID);
        }
        if (orderRepository.countPendingOrdersByResidentId(resident.getId()) > 0) {
            throw new BizException(ErrorCode.RESIDENT_MERCHANT_HAS_ORDERS);
        }

        application.setStatus("quitting");
        application.setQuitRequestedAt(LocalDateTime.now());
        applicationRepository.save(application);

        return myStatus();
    }

    @Transactional
    public ResidentMerchantListingItemResponse createListing(CreateResidentMerchantListingRequest request) {
        ResidentMerchantApplication application = requireActiveMerchantApplication();
        Resident resident = getActiveResident(UserContext.currentUserId());
        DistributorProduct product = distributorProductRepository.findById(request.getDistributorProductId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!"active".equals(product.getStatus())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        // 拿货名额：产品数量即该区域可拿货的业主商户数量
        int stock = product.getStock() != null ? product.getStock() : 0;
        if (stock <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该产品拿货名额已满");
        }
        product.setStock(stock - 1);
        distributorProductRepository.save(product);
        assertRetailPriceInMarkupRange(product, request.getRetailPrice());

        ResidentMerchantListing listing = new ResidentMerchantListing();
        listing.setId(idGenerator.generateId("rml"));
        listing.setResidentMerchantId(application.getId());
        listing.setResidentId(resident.getId());
        listing.setDistributorProductId(product.getId());
        listing.setRetailPrice(request.getRetailPrice());
        listing.setEnabled(request.getEnabled() == null || request.getEnabled());
        listing.setShareCount(0);
        listingRepository.save(listing);

        return toListingItem(listing, product);
    }

    @Transactional(readOnly = true)
    public List<ResidentMerchantListingItemResponse> listMyListings() {
        requireActiveMerchantApplication();
        Resident resident = getActiveResident(UserContext.currentUserId());
        List<ResidentMerchantListing> listings =
                listingRepository.findByResidentIdOrderByCreatedAtDesc(resident.getId());
        Map<String, DistributorProduct> productMap = distributorProductRepository.findAllById(
                listings.stream().map(ResidentMerchantListing::getDistributorProductId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(DistributorProduct::getId, p -> p));

        return listings.stream()
                .map(l -> toListingItem(l, productMap.get(l.getDistributorProductId())))
                .toList();
    }

    /**
     * 取消拿货：业主商户删除自己从一级经销商拿货的上架记录。
     */
    @Transactional
    public void cancelListing(String id) {
        requireActiveMerchantApplication();
        Resident resident = getActiveResident(UserContext.currentUserId());
        ResidentMerchantListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!resident.getId().equals(listing.getResidentId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权取消其他商户的拿货记录");
        }
        releasePickupQuota(listing.getDistributorProductId());
        listingRepository.delete(listing);
    }

    /**
     * 释放拿货名额（产品数量 +1），其他业主商户可继续拿货。
     */
    private void releasePickupQuota(String distributorProductId) {
        distributorProductRepository.findById(distributorProductId).ifPresent(product -> {
            product.setStock((product.getStock() != null ? product.getStock() : 0) + 1);
            distributorProductRepository.save(product);
        });
    }

    @Transactional
    public ResidentMerchantListingItemResponse updateListing(String id, UpdateResidentMerchantListingRequest request) {
        requireActiveMerchantApplication();
        Resident resident = getActiveResident(UserContext.currentUserId());
        ResidentMerchantListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!resident.getId().equals(listing.getResidentId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        DistributorProduct product = distributorProductRepository.findById(listing.getDistributorProductId())
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getRetailPrice() != null) {
            assertRetailPriceInMarkupRange(product, request.getRetailPrice());
            listing.setRetailPrice(request.getRetailPrice());
        }
        if (request.getEnabled() != null) {
            listing.setEnabled(request.getEnabled());
        }
        listingRepository.save(listing);
        return toListingItem(listing, product);
    }

    @Transactional
    public ShareListingResponse shareListing(String id) {
        ResidentMerchantApplication application = requireActiveMerchantApplication();
        Resident resident = getActiveResident(UserContext.currentUserId());
        ResidentMerchantListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!resident.getId().equals(listing.getResidentId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }

        listing.setShareCount((listing.getShareCount() == null ? 0 : listing.getShareCount()) + 1);
        listingRepository.save(listing);

        String shareUrl = shareBaseUrl + "?listingId=" + listing.getId() + "&resellerId=" + resident.getId();
        return ShareListingResponse.builder()
                .shareUrl(shareUrl)
                .posterImageUrl(shareBaseUrl.replace("/h5/shop", "/api/v1/static/posters/" + listing.getId() + ".png"))
                .qrcodeUrl(shareUrl + "&format=qrcode")
                .visibility(defaultVisibility(application.getVisibility()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<DistributorProductItemResponse> listDistributorProducts(
            String categoryId, String keyword, int page, int pageSize) {
        roleGuard.requireAnyRole("resident");
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        PageRequest pageable = PageRequest.of(page - 1, Math.min(Math.max(pageSize, 1), 100));

        Specification<DistributorProduct> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.equal(root.get("status"), "active"));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates = cb.and(predicates, cb.like(root.get("name"), like));
            }
            return predicates;
        };

        Page<DistributorProduct> result = distributorProductRepository.findAll(spec, pageable);
        List<DistributorProductItemResponse> list = result.getContent().stream()
                .map(this::toDistributorItem)
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public MyResidentMerchantResponse updateVisibility(UpdateResidentMerchantVisibilityRequest request) {
        ResidentMerchantApplication application = requireActiveMerchantApplication();
        application.setVisibility(request.getVisibility());
        applicationRepository.save(application);
        return myStatus();
    }

    /**
     * 公开橱窗列表：仅返回对外(public)且营业中(active)的业主商户，任何人可访问。
     */
    @Transactional(readOnly = true)
    public PageResponse<PublicResidentMerchantShopResponse> listPublicShops(
            String keyword, String propertyCompanyId, int page, int pageSize) {
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        PageRequest pageable = PageRequest.of(page - 1, Math.min(Math.max(pageSize, 1), 50));

        Specification<ResidentMerchantApplication> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("visibility"), VISIBILITY_PUBLIC));
            predicates.add(cb.equal(root.get("status"), "active"));
            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                var residentSub = query.subquery(String.class);
                var residentRoot = residentSub.from(Resident.class);
                residentSub.select(residentRoot.get("id"));
                residentSub.where(cb.or(
                        cb.like(residentRoot.get("name"), like),
                        cb.like(residentRoot.get("phone"), like)));
                predicates.add(root.get("residentId").in(residentSub));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ResidentMerchantApplication> result = applicationRepository.findAll(spec, pageable);
        List<ResidentMerchantApplication> shops = result.getContent();
        if (shops.isEmpty()) {
            return PageResponse.of(List.of(), page, pageSize, 0);
        }

        Map<String, String> residentNames = loadResidentNames(shops.stream()
                .map(ResidentMerchantApplication::getResidentId)
                .distinct()
                .toList());
        Map<String, Long> listingCounts = listingRepository.findByResidentIdInAndEnabledTrue(
                        shops.stream().map(ResidentMerchantApplication::getResidentId).toList())
                .stream()
                .collect(Collectors.groupingBy(ResidentMerchantListing::getResidentId, Collectors.counting()));

        List<PublicResidentMerchantShopResponse> list = shops.stream()
                .map(a -> PublicResidentMerchantShopResponse.builder()
                        .residentId(a.getResidentId())
                        .residentName(residentNames.get(a.getResidentId()))
                        .propertyCompanyId(a.getPropertyCompanyId())
                        .visibility(defaultVisibility(a.getVisibility()))
                        .status(a.getStatus())
                        .depositAmount(a.getDepositAmount())
                        .listings(null)
                        .listingCount(listingCounts.getOrDefault(a.getResidentId(), 0L).intValue())
                        .build())
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    /**
     * 公开橱窗详情：对外且营业中的业主商户 + 启用中的上架商品（不含进货价）。
     */
    @Transactional(readOnly = true)
    public PublicResidentMerchantShopResponse getPublicShop(String residentId) {
        ResidentMerchantApplication application = applicationRepository
                .findByResidentIdAndVisibilityAndStatus(residentId, VISIBILITY_PUBLIC, "active")
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_MERCHANT_NOT_PUBLIC));

        List<ResidentMerchantListing> listings = listingRepository
                .findByResidentIdAndEnabledTrueOrderByCreatedAtDesc(residentId);
        Map<String, DistributorProduct> productMap = listings.isEmpty() ? Map.of()
                : distributorProductRepository.findAllById(listings.stream()
                                .map(ResidentMerchantListing::getDistributorProductId)
                                .distinct()
                                .toList())
                        .stream()
                        .collect(Collectors.toMap(DistributorProduct::getId, p -> p));

        List<PublicResidentMerchantListingItemResponse> items = listings.stream()
                .map(l -> {
                    DistributorProduct dp = productMap.get(l.getDistributorProductId());
                    return PublicResidentMerchantListingItemResponse.builder()
                            .id(l.getId())
                            .productName(dp == null ? null : dp.getName())
                            .coverUrl(dp == null ? null : dp.getCoverUrl())
                            .category(dp == null ? null : dp.getCategory())
                            .enabled(l.getEnabled() == null || l.getEnabled())
                            .retailPrice(l.getRetailPrice())
                            .shareCount(l.getShareCount() == null ? 0 : l.getShareCount())
                            .build();
                })
                .toList();

        return PublicResidentMerchantShopResponse.builder()
                .residentId(application.getResidentId())
                .residentName(residentRepository.findById(residentId)
                        .map(this::publicResidentName)
                        .orElse(null))
                .propertyCompanyId(application.getPropertyCompanyId())
                .visibility(defaultVisibility(application.getVisibility()))
                .status(application.getStatus())
                .depositAmount(application.getDepositAmount())
                .listings(items)
                .listingCount(items.size())
                .build();
    }

    /**
     * 业主商城·扁平公开货源列表（v7.8）：单接口按商品分页，返回对外(public)且营业中(active)店铺的
     * 启用中上架商品。消除原 public 列表 + 逐店详情的 N+1 请求。category 来自一级货源 distributor_products.category。
     * 按物业公司过滤（propertyCompanyId 必填，避免跨物业串货）。
     */
    @Transactional(readOnly = true)
    public PageResponse<PublicResidentMerchantListingFlatItemResponse> listPublicListings(
            String keyword, String propertyCompanyId, String category, int page, int pageSize) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        int size = Math.min(Math.max(pageSize, 1), 50);

        // 1) 该物业下，对外且营业中的业主商户 residentId 列表
        Specification<ResidentMerchantApplication> appSpec = (root, query, cb) -> cb.and(
                cb.equal(root.get("visibility"), VISIBILITY_PUBLIC),
                cb.equal(root.get("status"), "active"),
                cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
        List<String> residentIds = applicationRepository.findAll(appSpec).stream()
                .map(ResidentMerchantApplication::getResidentId)
                .distinct()
                .toList();
        if (residentIds.isEmpty()) {
            return PageResponse.of(List.of(), page, size, 0);
        }

        // 2) 这些店铺的启用中上架商品，按 keyword(商品名) / category 过滤
        Specification<ResidentMerchantListing> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("enabled"), true));
            predicates.add(root.get("residentId").in(residentIds));

            if (StringUtils.hasText(keyword) || StringUtils.hasText(category)) {
                var dpSub = query.subquery(String.class);
                var dpRoot = dpSub.from(DistributorProduct.class);
                dpSub.select(dpRoot.get("id"));
                var dpPredicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                dpPredicates.add(cb.equal(dpRoot.get("id"), root.get("distributorProductId")));
                if (StringUtils.hasText(keyword)) {
                    dpPredicates.add(cb.like(dpRoot.get("name"), "%" + keyword.trim() + "%"));
                }
                if (StringUtils.hasText(category)) {
                    dpPredicates.add(cb.equal(dpRoot.get("category"), category));
                }
                dpSub.where(cb.and(dpPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
                predicates.add(cb.exists(dpSub));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ResidentMerchantListing> result = listingRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        if (result.getContent().isEmpty()) {
            return PageResponse.of(List.of(), page, size, 0);
        }

        Map<String, DistributorProduct> productMap = result.getContent().isEmpty() ? Map.of()
                : distributorProductRepository.findAllById(result.getContent().stream()
                                .map(ResidentMerchantListing::getDistributorProductId)
                                .distinct()
                                .toList())
                        .stream()
                        .collect(Collectors.toMap(DistributorProduct::getId, p -> p));
        Map<String, String> residentNames = loadResidentNames(result.getContent().stream()
                .map(ResidentMerchantListing::getResidentId)
                .distinct()
                .toList());

        List<PublicResidentMerchantListingFlatItemResponse> list = result.getContent().stream()
                .map(l -> {
                    DistributorProduct dp = productMap.get(l.getDistributorProductId());
                    return PublicResidentMerchantListingFlatItemResponse.builder()
                            .listingId(l.getId())
                            .residentId(l.getResidentId())
                            .residentName(residentNames.get(l.getResidentId()))
                            .category(dp == null ? null : dp.getCategory())
                            .productName(dp == null ? null : dp.getName())
                            .coverUrl(dp == null ? null : dp.getCoverUrl())
                            .retailPrice(l.getRetailPrice())
                            .shareCount(l.getShareCount() == null ? 0 : l.getShareCount())
                            .build();
                })
                .toList();
        return PageResponse.of(list, page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public MyResidentMerchantResponse myStatus() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        ResidentMerchantApplication application = applicationRepository.findByResidentId(resident.getId())
                .orElse(null);
        if (application == null) {
            return MyResidentMerchantResponse.builder()
                    .applicationId(null)
                    .status("未申请")
                    .statusCode("none")
                    .depositStatus("none")
                    .depositAmount(resolveDepositAmount())
                    .balance(resident.getCoinBalance() == null ? BigDecimal.ZERO : resident.getCoinBalance())
                    .listingCount(0L)
                    .build();
        }

        ResidentMerchantDeposit deposit = depositRepository.findByApplicationId(application.getId()).orElse(null);
        String depositStatus = deposit == null ? "unpaid" : deposit.getStatus();
        long listingCount = listingRepository.countByResidentIdAndEnabledTrue(resident.getId());

        return MyResidentMerchantResponse.builder()
                .applicationId(application.getId())
                .status(STATUS_LABELS.getOrDefault(application.getStatus(), application.getStatus()))
                .statusCode(application.getStatus())
                .visibility(defaultVisibility(application.getVisibility()))
                .depositStatus(depositStatus)
                .depositAmount(application.getDepositAmount())
                .balance(resident.getCoinBalance() == null ? BigDecimal.ZERO : resident.getCoinBalance())
                .listingCount(listingCount)
                .build();
    }

    private ResidentMerchantApplication requireActiveMerchantApplication() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        ResidentMerchantApplication application = applicationRepository.findByResidentId(resident.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_MERCHANT_DEPOSIT_INVALID));
        if (!"active".equals(application.getStatus())) {
            throw new BizException(ErrorCode.RESIDENT_MERCHANT_DEPOSIT_INVALID, "业主商户未营业");
        }
        return application;
    }

    private ResidentMerchantListingItemResponse toListingItem(
            ResidentMerchantListing listing, DistributorProduct product) {
        return ResidentMerchantListingItemResponse.builder()
                .id(listing.getId())
                .distributorProductId(listing.getDistributorProductId())
                .productName(product == null ? null : product.getName())
                .coverUrl(product == null ? null : product.getCoverUrl())
                .wholesalePrice(product == null ? null : product.getWholesalePrice())
                .retailPrice(listing.getRetailPrice())
                .enabled(Boolean.TRUE.equals(listing.getEnabled()))
                .shareCount(listing.getShareCount() == null ? 0 : listing.getShareCount())
                .createdAt(DateTimeUtil.toLocalDateTimeString(listing.getCreatedAt()))
                .build();
    }

    /**
     * 定价规则：业主商户自定义售价，但不能低于在一级经销商处的进货价。
     */
    private void assertRetailPriceInMarkupRange(DistributorProduct product, BigDecimal retailPrice) {
        if (retailPrice == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "零售价不能为空");
        }
        BigDecimal wholesale = product.getWholesalePrice() != null ? product.getWholesalePrice() : BigDecimal.ZERO;
        // 需求：业主商户自定义售价，但不能低于在一级经销商处的进货价
        if (retailPrice.compareTo(wholesale) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "零售价不能低于进货价");
        }
    }

    private DistributorProductItemResponse toDistributorItem(DistributorProduct product) {
        return DistributorProductItemResponse.builder()
                .distributorProductId(product.getId())
                .name(product.getName())
                .coverUrl(product.getCoverUrl())
                .wholesalePrice(product.getWholesalePrice())
                .suggestedRetailPrice(product.getSuggestedRetailPrice())
                .stock(product.getStock())
                .build();
    }

    private Map<String, String> loadResidentNames(List<String> residentIds) {
        if (residentIds == null || residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, this::publicResidentName, (a, b) -> a));
    }

    /**
     * 公开橱窗展示的店主名：优先姓名；无姓名时用脱敏手机号，避免泄露完整手机号。
     */
    private String publicResidentName(Resident resident) {
        if (StringUtils.hasText(resident.getName())) {
            return resident.getName();
        }
        return StringUtils.hasText(resident.getPhone()) ? PhoneUtil.mask(resident.getPhone()) : null;
    }

    private String defaultVisibility(String visibility) {
        return StringUtils.hasText(visibility) ? visibility : VISIBILITY_PRIVATE;
    }

    private Resident getActiveResident(String id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }
}
