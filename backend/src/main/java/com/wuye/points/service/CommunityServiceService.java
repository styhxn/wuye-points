package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.service.BookServiceRequest;
import com.wuye.points.dto.service.CommunityServiceDetailResponse;
import com.wuye.points.dto.service.CommunityServiceListItemResponse;
import com.wuye.points.dto.service.CreateCommunityServiceRequest;
import com.wuye.points.dto.service.DeleteCommunityServiceResponse;
import com.wuye.points.dto.service.ServiceOrderDetailResponse;
import com.wuye.points.dto.service.ServiceSubscriptionItemResponse;
import com.wuye.points.dto.service.SubscribeServiceRequest;
import com.wuye.points.dto.service.SubscribeServiceResponse;
import com.wuye.points.dto.service.UpdateCommunityServiceRequest;
import com.wuye.points.entity.CommunityService;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ServiceOrder;
import com.wuye.points.entity.ServiceSubscription;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityServiceRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.ServiceOrderRepository;
import com.wuye.points.repository.ServiceSubscriptionRepository;
import com.wuye.points.repository.spec.CommunityServiceSpecifications;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceService {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "rankOrder", "price", "name");

    private final CommunityServiceRepository communityServiceRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceSubscriptionRepository serviceSubscriptionRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final CommunityServiceConverter communityServiceConverter;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<CommunityServiceListItemResponse> listServices(
            int page,
            int pageSize,
            String category,
            Boolean mine,
            String sort) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateCategory(category);

        String propertyCompanyId;
        String providerId = null;
        String status = "active";

        if (UserContext.currentUserId() == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }

        if (roleGuard.isPropertyAdmin() || roleGuard.isPlatformAdmin()) {
            propertyCompanyId = roleGuard.isPlatformAdmin()
                    ? null
                    : UserContext.currentPropertyCompanyId();
            status = null;
        } else {
            Resident viewer = getActiveResident(UserContext.currentUserId());
            propertyCompanyId = viewer.getPropertyCompanyId();
            if (Boolean.TRUE.equals(mine)) {
                providerId = viewer.getId();
            }
        }

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<CommunityService> servicePage = communityServiceRepository.findAll(
                CommunityServiceSpecifications.withFilters(propertyCompanyId, category, status, providerId),
                pageable);

        return buildServiceListPage(servicePage, page, pageSize);
    }

    @Transactional(readOnly = true)
    public CommunityServiceDetailResponse getService(String id) {
        validateServiceId(id);
        CommunityService service = communityServiceRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.SERVICE_NOT_FOUND));
        assertCanViewService(service);
        Resident provider = loadProvider(service.getProviderId());
        return communityServiceConverter.toDetail(service, provider);
    }

    @Transactional
    public CommunityServiceDetailResponse createService(CreateCommunityServiceRequest request) {
        roleGuard.requireAnyRole("merchant", "individual_leader", "resident");
        validateCategory(request.getCategory());

        Resident provider = getActiveResident(UserContext.currentUserId());
        assertCanPublishService(provider);

        CommunityService service = new CommunityService();
        service.setId(idGenerator.generateId("svc"));
        applyCreateFields(service, request);
        service.setProviderId(provider.getId());
        service.setProviderType(resolveProviderType(provider.getRole()));
        service.setPropertyCompanyId(provider.getPropertyCompanyId());
        service.setStatus("active");
        if (service.getRankOrder() == null) {
            service.setRankOrder(0);
        }
        communityServiceRepository.save(service);

        return communityServiceConverter.toDetail(service, provider);
    }

    @Transactional
    public CommunityServiceDetailResponse updateService(String id, UpdateCommunityServiceRequest request) {
        validateServiceId(id);
        CommunityService service = getManageableService(id);
        applyUpdateFields(service, request);
        communityServiceRepository.save(service);
        return communityServiceConverter.toDetail(service, loadProvider(service.getProviderId()));
    }

    @Transactional
    public DeleteCommunityServiceResponse deleteService(String id) {
        validateServiceId(id);
        CommunityService service = getManageableService(id);
        service.setStatus("inactive");
        communityServiceRepository.save(service);
        return DeleteCommunityServiceResponse.builder()
                .id(service.getId())
                .status(service.getStatus())
                .build();
    }

    @Transactional
    public ServiceOrderDetailResponse bookService(String id, BookServiceRequest request) {
        roleGuard.requireAnyRole("resident");
        validateServiceId(id);
        validatePaymentMethod(request.getPaymentMethod());

        Resident resident = getActiveResident(UserContext.currentUserId());
        CommunityService service = getReadableService(id, resident);
        assertServiceBookable(service);

        BigDecimal amount = resolveBookAmount(service, resident);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该服务暂不可预约");
        }

        ServiceOrder order = new ServiceOrder();
        order.setId(idGenerator.generateId("so"));
        order.setServiceId(service.getId());
        order.setResidentId(resident.getId());
        order.setProviderId(service.getProviderId());
        order.setPropertyCompanyId(resident.getPropertyCompanyId());
        order.setAmount(amount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setServiceTime(parseServiceTime(request.getServiceTime()));
        order.setAddress(request.getAddress().trim());
        order.setContactPhone(request.getContactPhone().trim());
        order.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        order.setOrderStatus("pending");

        PaymentBreakdown breakdown = resolvePaymentBreakdown(
                request.getPaymentMethod(), amount, request.getPointUsed(), request.getCoinUsed(),
                resident.getPropertyCompanyId());
        executePaymentBreakdown(resident, service.getId(), amount, breakdown, "服务预约");
        if (!"wechat".equals(request.getPaymentMethod()) || breakdown.cashAmount().compareTo(BigDecimal.ZERO) == 0) {
            order.setOrderStatus("confirmed");
        }

        serviceOrderRepository.save(order);
        return toOrderDetail(order, service, resident, loadProvider(service.getProviderId()));
    }

    @Transactional
    public SubscribeServiceResponse subscribe(String id, SubscribeServiceRequest request) {
        roleGuard.requireAnyRole("resident");
        validateServiceId(id);
        validateSubType(request.getSubType());
        validatePaymentMethod(request.getPaymentMethod());

        Resident resident = getActiveResident(UserContext.currentUserId());
        CommunityService service = getReadableService(id, resident);
        if (!Boolean.TRUE.equals(service.getIsSubscription())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该服务不支持订阅");
        }

        LocalDate today = LocalDate.now();
        if (serviceSubscriptionRepository.existsByServiceIdAndResidentIdAndStatusAndEndDateGreaterThanEqual(
                service.getId(), resident.getId(), "active", today)) {
            throw new BizException(ErrorCode.SERVICE_ALREADY_SUBSCRIBED);
        }

        BigDecimal amount = resolveSubscriptionAmount(service, request.getSubType(), resident);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订阅金额无效");
        }

        PaymentBreakdown breakdown = resolvePaymentBreakdown(
                request.getPaymentMethod(), amount, request.getPointUsed(), request.getCoinUsed(),
                resident.getPropertyCompanyId());
        executePaymentBreakdown(resident, service.getId(), amount, breakdown, "服务订阅");

        LocalDate startDate = today;
        LocalDate endDate = "yearly".equals(request.getSubType())
                ? startDate.plusYears(1)
                : startDate.plusMonths(1);

        ServiceSubscription subscription = new ServiceSubscription();
        subscription.setId(idGenerator.generateId("ss"));
        subscription.setServiceId(service.getId());
        subscription.setResidentId(resident.getId());
        subscription.setPropertyCompanyId(resident.getPropertyCompanyId());
        subscription.setSubType(request.getSubType());
        subscription.setAmount(amount);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus("active");
        subscription.setAutoRenew(Boolean.TRUE.equals(request.getAutoRenew()));
        serviceSubscriptionRepository.save(subscription);

        return SubscribeServiceResponse.builder()
                .id(subscription.getId())
                .serviceId(service.getId())
                .serviceName(service.getName())
                .residentId(resident.getId())
                .subType(subscription.getSubType())
                .amount(subscription.getAmount())
                .startDate(subscription.getStartDate().toString())
                .endDate(subscription.getEndDate().toString())
                .autoRenew(subscription.getAutoRenew())
                .status(subscription.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(subscription.getCreatedAt()))
                .build();
    }

    @Transactional
    public SubscribeServiceResponse unsubscribe(String id) {
        roleGuard.requireAnyRole("resident");
        validateServiceId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        CommunityService service = getReadableService(id, resident);

        ServiceSubscription subscription = serviceSubscriptionRepository
                .findByServiceIdAndResidentIdAndStatus(service.getId(), resident.getId(), "active");
        if (subscription == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "未找到有效订阅");
        }

        subscription.setStatus("cancelled");
        serviceSubscriptionRepository.save(subscription);

        return SubscribeServiceResponse.builder()
                .id(subscription.getId())
                .serviceId(service.getId())
                .serviceName(service.getName())
                .residentId(resident.getId())
                .subType(subscription.getSubType())
                .amount(subscription.getAmount())
                .startDate(subscription.getStartDate().toString())
                .endDate(subscription.getEndDate().toString())
                .autoRenew(false)
                .status("cancelled")
                .createdAt(DateTimeUtil.toIsoUtc(subscription.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceSubscriptionItemResponse> listMySubscriptions(
            int page,
            int pageSize,
            String status) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateSubscriptionStatus(status);

        Resident resident = getActiveResident(UserContext.currentUserId());
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ServiceSubscription> subscriptionPage = StringUtils.hasText(status)
                ? serviceSubscriptionRepository.findByResidentIdAndStatus(resident.getId(), status, pageable)
                : serviceSubscriptionRepository.findByResidentId(resident.getId(), pageable);

        List<ServiceSubscription> subscriptions = subscriptionPage.getContent();
        Map<String, CommunityService> serviceMap = loadServices(subscriptions.stream()
                .map(ServiceSubscription::getServiceId)
                .toList());

        List<ServiceSubscriptionItemResponse> list = subscriptions.stream()
                .map(subscription -> {
                    refreshSubscriptionStatus(subscription);
                    CommunityService service = serviceMap.get(subscription.getServiceId());
                    return communityServiceConverter.toSubscriptionItem(subscription, service);
                })
                .toList();

        return PageResponse.of(list, page, pageSize, subscriptionPage.getTotalElements());
    }

    ServiceOrderDetailResponse toOrderDetail(
            ServiceOrder order,
            CommunityService service,
            Resident resident,
            Resident provider) {
        return ServiceOrderDetailResponse.builder()
                .id(order.getId())
                .serviceId(order.getServiceId())
                .serviceName(service != null ? service.getName() : null)
                .serviceCategory(service != null ? service.getCategory() : null)
                .residentId(order.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .providerId(order.getProviderId())
                .providerName(provider != null ? provider.getName() : null)
                .propertyCompanyId(order.getPropertyCompanyId())
                .orderStatus(order.getOrderStatus())
                .price(order.getAmount())
                .memberPrice(service != null ? service.getMemberPrice() : null)
                .paidAmount(order.getAmount())
                .paymentMethod(order.getPaymentMethod())
                .serviceTime(DateTimeUtil.toIsoUtc(order.getServiceTime()))
                .address(order.getAddress())
                .contactPhone(order.getContactPhone())
                .remark(order.getRemark())
                .createdAt(DateTimeUtil.toIsoUtc(order.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(order.getUpdatedAt()))
                .build();
    }

    private PageResponse<CommunityServiceListItemResponse> buildServiceListPage(
            Page<CommunityService> servicePage,
            int page,
            int pageSize) {
        List<CommunityService> services = servicePage.getContent();
        Map<String, Resident> providerMap = loadProviders(services.stream()
                .map(CommunityService::getProviderId)
                .toList());

        List<CommunityServiceListItemResponse> list = services.stream()
                .map(service -> communityServiceConverter.toListItem(
                        service, providerMap.get(service.getProviderId())))
                .toList();
        return PageResponse.of(list, page, pageSize, servicePage.getTotalElements());
    }

    private void applyCreateFields(CommunityService service, CreateCommunityServiceRequest request) {
        service.setName(request.getName().trim());
        service.setDescription(request.getDescription());
        service.setCoverUrls(communityServiceConverter.toJson(request.getCoverUrls()));
        service.setCategory(request.getCategory());
        service.setPrice(request.getPrice());
        service.setMemberPrice(request.getMemberPrice());
        service.setPriceUnit(request.getPriceUnit());
        service.setIsSubscription(Boolean.TRUE.equals(request.getIsSubscription()));
        service.setRankOrder(request.getRankOrder());
    }

    private void applyUpdateFields(CommunityService service, UpdateCommunityServiceRequest request) {
        if (StringUtils.hasText(request.getName())) {
            service.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getCoverUrls() != null) {
            service.setCoverUrls(communityServiceConverter.toJson(request.getCoverUrls()));
        }
        if (StringUtils.hasText(request.getCategory())) {
            validateCategory(request.getCategory());
            service.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }
        if (request.getMemberPrice() != null) {
            service.setMemberPrice(request.getMemberPrice());
        }
        if (request.getPriceUnit() != null) {
            service.setPriceUnit(request.getPriceUnit());
        }
        if (request.getIsSubscription() != null) {
            service.setIsSubscription(request.getIsSubscription());
        }
        if (request.getRankOrder() != null) {
            service.setRankOrder(request.getRankOrder());
        }
        if (StringUtils.hasText(request.getStatus())) {
            validateServiceStatus(request.getStatus());
            service.setStatus(request.getStatus());
        }
    }

    private CommunityService getReadableService(String id, Resident resident) {
        CommunityService service = communityServiceRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.SERVICE_NOT_FOUND));
        assertSameProperty(resident.getPropertyCompanyId(), service.getPropertyCompanyId());
        if (!"active".equals(service.getStatus()) && !service.getProviderId().equals(resident.getId())) {
            if (!roleGuard.isPropertyAdmin() && !roleGuard.isPlatformAdmin()) {
                throw new BizException(ErrorCode.SERVICE_NOT_FOUND);
            }
        }
        return service;
    }

    private CommunityService getManageableService(String id) {
        CommunityService service = communityServiceRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.SERVICE_NOT_FOUND));
        String currentUserId = UserContext.currentUserId();
        if (currentUserId != null && currentUserId.equals(service.getProviderId())) {
            return service;
        }
        if (roleGuard.isPlatformAdmin()) {
            return service;
        }
        if (roleGuard.isPropertyAdmin()) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (propertyCompanyId != null && propertyCompanyId.equals(service.getPropertyCompanyId())) {
                return service;
            }
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertCanViewService(CommunityService service) {
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (roleGuard.isPropertyAdmin()) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (propertyCompanyId != null && propertyCompanyId.equals(service.getPropertyCompanyId())) {
                return;
            }
            throw new BizException(ErrorCode.SERVICE_NOT_FOUND);
        }
        if (UserContext.currentUserId() == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident viewer = getActiveResident(UserContext.currentUserId());
        assertSameProperty(viewer.getPropertyCompanyId(), service.getPropertyCompanyId());
        if (!"active".equals(service.getStatus()) && !viewer.getId().equals(service.getProviderId())) {
            throw new BizException(ErrorCode.SERVICE_NOT_FOUND);
        }
    }

    private void assertCanPublishService(Resident provider) {
        if ("merchant".equals(provider.getRole()) || "resident".equals(provider.getRole())) {
            return;
        }
        if ("individual_leader".equals(provider.getRole())) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertServiceBookable(CommunityService service) {
        if (!"active".equals(service.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "服务不可预约");
        }
        if (Boolean.TRUE.equals(service.getIsSubscription())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订阅类服务请使用 /services/:id/subscribe");
        }
    }

    private String resolveProviderType(String role) {
        if ("merchant".equals(role)) {
            return "merchant";
        }
        if ("individual_leader".equals(role)) {
            return "individual_leader";
        }
        return "resident";
    }

    private BigDecimal resolveBookAmount(CommunityService service, Resident resident) {
        if ("owner".equals(resident.getUserType())
                && service.getMemberPrice() != null
                && service.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
            return service.getMemberPrice();
        }
        return service.getPrice() != null ? service.getPrice() : BigDecimal.ZERO;
    }

    private BigDecimal resolveSubscriptionAmount(CommunityService service, String subType, Resident resident) {
        BigDecimal base = resolveBookAmount(service, resident);
        if ("yearly".equals(subType)) {
            if ("年".equals(service.getPriceUnit())) {
                return base;
            }
            return base.multiply(BigDecimal.valueOf(12));
        }
        if ("月".equals(service.getPriceUnit())) {
            return base;
        }
        return base;
    }

    private PaymentBreakdown resolvePaymentBreakdown(
            String paymentMethod,
            BigDecimal amount,
            Integer pointUsed,
            BigDecimal coinUsed,
            String propertyCompanyId) {
        return switch (paymentMethod) {
            case "property_coin" -> new PaymentBreakdown(0, amount, BigDecimal.ZERO);
            case "point" -> {
                BigDecimal rate = resolvePointToFeeRate(propertyCompanyId);
                int points = amount.divide(rate, 0, RoundingMode.UP).intValue();
                yield new PaymentBreakdown(points, BigDecimal.ZERO, BigDecimal.ZERO);
            }
            case "wechat" -> new PaymentBreakdown(0, BigDecimal.ZERO, amount);
            case "mixed" -> {
                int points = pointUsed != null ? pointUsed : 0;
                BigDecimal coins = coinUsed != null ? coinUsed : BigDecimal.ZERO;
                BigDecimal rate = resolvePointToFeeRate(propertyCompanyId);
                BigDecimal pointValue = rate.multiply(BigDecimal.valueOf(points))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal cashAmount = amount.subtract(pointValue).subtract(coins);
                if (cashAmount.compareTo(BigDecimal.ZERO) < 0) {
                    cashAmount = BigDecimal.ZERO;
                }
                BigDecimal covered = pointValue.add(coins).add(cashAmount);
                if (covered.compareTo(amount) < 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "混合支付金额不足");
                }
                yield new PaymentBreakdown(points, coins, cashAmount);
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        };
    }

    private void executePaymentBreakdown(
            Resident resident,
            String referenceId,
            BigDecimal amount,
            PaymentBreakdown breakdown,
            String remark) {
        if (breakdown.pointUsed() > 0) {
            pointService.spendPoints(
                    resident, breakdown.pointUsed(), "manual", referenceId, remark, null, false);
        }
        if (breakdown.coinUsed().compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(
                    resident, breakdown.coinUsed(), "manual", referenceId, remark, null, false);
        }
        if (breakdown.cashAmount().compareTo(BigDecimal.ZERO) > 0) {
            // 微信支付由前端完成，后端先创建订单
        }
    }

    private record PaymentBreakdown(int pointUsed, BigDecimal coinUsed, BigDecimal cashAmount) {
    }

    private BigDecimal resolvePointToFeeRate(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return DEFAULT_POINT_TO_FEE_RATE;
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getPointToFeeRate)
                .filter(rate -> rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                .orElse(DEFAULT_POINT_TO_FEE_RATE);
    }

    private void refreshSubscriptionStatus(ServiceSubscription subscription) {
        if ("cancelled".equals(subscription.getStatus())) {
            return;
        }
        if (subscription.getEndDate() != null
                && subscription.getEndDate().isBefore(LocalDate.now())
                && "active".equals(subscription.getStatus())) {
            subscription.setStatus("expired");
        }
    }

    private LocalDateTime parseServiceTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZONE_SHANGHAI);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "serviceTime格式不正确");
        }
    }

    private Map<String, Resident> loadProviders(List<String> providerIds) {
        return residentRepository.findAllById(providerIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));
    }

    private Map<String, CommunityService> loadServices(List<String> serviceIds) {
        return communityServiceRepository.findAllById(serviceIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(CommunityService::getId, Function.identity(), (a, b) -> a));
    }

    private Resident loadProvider(String providerId) {
        if (!StringUtils.hasText(providerId)) {
            return null;
        }
        return residentRepository.findById(providerId).orElse(null);
    }

    private void assertSameProperty(String left, String right) {
        if (left == null || right == null || !left.equals(right)) {
            throw new BizException(ErrorCode.SERVICE_NOT_FOUND);
        }
    }

    private void validateServiceId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("svc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "服务ID格式不正确");
        }
    }

    private void validateCategory(String category) {
        if (StringUtils.hasText(category) && !RoleGuard.SERVICE_CATEGORIES.contains(category)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "category值无效");
        }
    }

    private void validateServiceStatus(String status) {
        if (!RoleGuard.COMMUNITY_SERVICE_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
    }

    private void validateSubType(String subType) {
        if (!StringUtils.hasText(subType) || !RoleGuard.SUBSCRIPTION_TYPES.contains(subType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "subType值无效");
        }
    }

    private void validatePaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod) || !RoleGuard.PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        }
    }

    private void validateSubscriptionStatus(String status) {
        if (StringUtils.hasText(status) && !RoleGuard.ACTIVITY_SUBSCRIPTION_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
    }

    private Resident getActiveResident(String userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.ASC, "rankOrder").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        Sort.Direction direction = Sort.Direction.DESC;
        String field = sort;
        if (sort.startsWith("-")) {
            field = sort.substring(1);
            direction = Sort.Direction.DESC;
        } else if (sort.startsWith("+")) {
            field = sort.substring(1);
            direction = Sort.Direction.ASC;
        }
        if (!SORTABLE_FIELDS.contains(field)) {
            return Sort.by(Sort.Direction.ASC, "rankOrder").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return Sort.by(direction, field);
    }
}
