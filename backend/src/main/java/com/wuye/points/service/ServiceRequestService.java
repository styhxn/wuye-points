package com.wuye.points.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.servicerequest.CreateServiceRequestRequest;
import com.wuye.points.dto.servicerequest.MerchantPendingServiceRequestResponse;
import com.wuye.points.dto.servicerequest.PayServiceRequestQuoteRequest;
import com.wuye.points.dto.servicerequest.ServiceCategoryItemResponse;
import com.wuye.points.dto.servicerequest.ServiceRequestDetailResponse;
import com.wuye.points.dto.servicerequest.ServiceRequestItemResponse;
import com.wuye.points.dto.servicerequest.ServiceRequestQueueItemResponse;
import com.wuye.points.dto.servicerequest.ServiceRequestQuoteInfo;
import com.wuye.points.dto.servicerequest.ServiceRequestQuoteRequest;
import com.wuye.points.entity.CommunityService;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantMessageServiceConfig;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.ServiceOrder;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ServiceCategoryDict;
import com.wuye.points.entity.ServiceRequestMerchantQueue;
import com.wuye.points.entity.ServiceRequestOrder;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityServiceRepository;
import com.wuye.points.repository.MerchantMessageServiceConfigRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.ServiceCategoryDictRepository;
import com.wuye.points.repository.ServiceOrderRepository;
import com.wuye.points.repository.ServiceRequestMerchantQueueRepository;
import com.wuye.points.repository.ServiceRequestOrderRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.ResidentAddressUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.01");
    private static final Set<String> CANCELLABLE_STATUSES = Set.of("matching", "negotiating");
    private static final Set<String> PAYMENT_METHODS = Set.of("point", "property_coin", "mixed");

    private static final Set<String> CANCELLABLE_QUEUE_STATUSES = Set.of("pending", "waiting", "accepted");

    private static final Map<String, String> STATUS_LABELS = Map.ofEntries(
            Map.entry("matching", "匹配中"),
            Map.entry("negotiating", "协商中"),
            Map.entry("quoted", "待支付"),
            Map.entry("paid", "已支付"),
            Map.entry("completed", "已完成"),
            Map.entry("closed", "已关闭"),
            Map.entry("cancelled", "已取消"),
            Map.entry("pending", "待响应"),
            Map.entry("waiting", "排队中"),
            Map.entry("accepted", "已接单"),
            Map.entry("skipped", "已跳过"),
            Map.entry("timeout", "已超时"));

    private final RoleGuard roleGuard;
    private final ResidentRepository residentRepository;
    private final MerchantRepository merchantRepository;
    private final ServiceCategoryDictRepository serviceCategoryDictRepository;
    private final ServiceRequestOrderRepository serviceRequestOrderRepository;
    private final ServiceRequestMerchantQueueRepository serviceRequestMerchantQueueRepository;
    private final MerchantMessageServiceConfigRepository merchantMessageServiceConfigRepository;
    private final MessageRepository messageRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final CommunityServiceRepository communityServiceRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final AdminSupport adminSupport;

    @Transactional(readOnly = true)
    public List<ServiceCategoryItemResponse> listCategories() {
        return serviceCategoryDictRepository.findByStatusOrderBySortOrderAsc("active").stream()
                .map(this::toCategoryItem)
                .toList();
    }

    @Transactional
    public ServiceRequestItemResponse create(CreateServiceRequestRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(resident.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "请先绑定所属物业公司");
        }

        String communityId = StringUtils.hasText(request.getCommunityId())
                ? request.getCommunityId()
                : resident.getCommunityId();

        ServiceCategoryDict matched = matchCategory(request.getDescription());
        List<Merchant> candidates = resolveCandidateMerchants(
                resident.getPropertyCompanyId(), matched == null ? null : matched.getId());

        ServiceRequestOrder order = new ServiceRequestOrder();
        order.setId(idGenerator.generateId("sro"));
        order.setResidentId(resident.getId());
        order.setPropertyCompanyId(resident.getPropertyCompanyId());
        order.setCommunityId(communityId);
        order.setDescription(request.getDescription().trim());
        order.setContactPhone(StringUtils.hasText(request.getContactPhone())
                ? request.getContactPhone()
                : resident.getPhone());
        order.setAddress(StringUtils.hasText(request.getAddress())
                ? request.getAddress().trim()
                : ResidentAddressUtil.buildDefaultAddress(resident));
        if (StringUtils.hasText(request.getPreferredTime())) {
            order.setPreferredTime(adminSupport.parseIsoDateTime(request.getPreferredTime()));
        }
        order.setMatchedCategoryId(matched == null ? null : matched.getId());
        order.setStatus(candidates.isEmpty() ? "closed" : "matching");
        if (candidates.isEmpty()) {
            order.setClosedReason("暂无可用商家，请稍后重试");
        }

        LocalDateTime now = LocalDateTime.now();
        List<ServiceRequestMerchantQueue> queueRows = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Merchant merchant = candidates.get(i);
            ServiceRequestMerchantQueue row = new ServiceRequestMerchantQueue();
            row.setId(idGenerator.generateId("srmq"));
            row.setRequestId(order.getId());
            row.setMerchantId(merchant.getId());
            row.setQueueOrder(i);
            if (i == 0) {
                row.setStatus("pending");
                row.setNotifiedAt(now);
                row.setTimeoutAt(now.plusMinutes(5));
                order.setCurrentMerchantId(merchant.getId());
            } else {
                row.setStatus("waiting");
            }
            queueRows.add(row);
        }

        serviceRequestOrderRepository.save(order);
        if (!queueRows.isEmpty()) {
            serviceRequestMerchantQueueRepository.saveAll(queueRows);
        }

        String merchantName = null;
        if (order.getCurrentMerchantId() != null) {
            merchantName = merchantRepository.findById(order.getCurrentMerchantId())
                    .map(Merchant::getName).orElse(null);
        }
        return toItem(order, matched == null ? null : matched.getName(), merchantName);
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestItemResponse> listMy(int page, int pageSize) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        PageRequest pageable = PageRequest.of(page - 1, pageSize);
        Page<ServiceRequestOrder> result =
                serviceRequestOrderRepository.findByResidentIdOrderByCreatedAtDesc(resident.getId(), pageable);

        Map<String, String> categoryNames = loadCategoryNames(
                result.getContent().stream().map(ServiceRequestOrder::getMatchedCategoryId).toList());
        Map<String, String> merchantNames = loadMerchantNames(
                result.getContent().stream().map(ServiceRequestOrder::getCurrentMerchantId).toList());

        List<ServiceRequestItemResponse> list = result.getContent().stream()
                .map(o -> toItem(o,
                        categoryNames.get(o.getMatchedCategoryId()),
                        merchantNames.get(o.getCurrentMerchantId())))
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<MerchantPendingServiceRequestResponse> listMerchantPending(int page, int pageSize) {
        Merchant merchant = requireCurrentMerchant();
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        PageRequest pageable = PageRequest.of(page - 1, pageSize);
        Page<ServiceRequestMerchantQueue> queuePage =
                serviceRequestMerchantQueueRepository.findByMerchantIdAndStatusOrderByNotifiedAtDesc(
                        merchant.getId(), "pending", pageable);

        Set<String> requestIds = queuePage.getContent().stream()
                .map(ServiceRequestMerchantQueue::getRequestId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, ServiceRequestOrder> orderMap = requestIds.isEmpty()
                ? Map.of()
                : serviceRequestOrderRepository.findAllById(requestIds).stream()
                .collect(Collectors.toMap(ServiceRequestOrder::getId, o -> o, (a, b) -> a));
        Map<String, String> categoryNames = loadCategoryNames(
                orderMap.values().stream().map(ServiceRequestOrder::getMatchedCategoryId).toList());

        List<MerchantPendingServiceRequestResponse> list = queuePage.getContent().stream()
                .map(q -> {
                    ServiceRequestOrder order = orderMap.get(q.getRequestId());
                    return MerchantPendingServiceRequestResponse.builder()
                            .queueId(q.getId())
                            .requestId(q.getRequestId())
                            .description(order == null ? null : order.getDescription())
                            .contactPhone(order == null ? null : order.getContactPhone())
                            .address(order == null ? null : order.getAddress())
                            .matchedCategoryName(order == null
                                    ? null
                                    : categoryNames.get(order.getMatchedCategoryId()))
                            .status(STATUS_LABELS.getOrDefault(q.getStatus(), q.getStatus()))
                            .statusCode(q.getStatus())
                            .timeoutAt(DateTimeUtil.toLocalDateTimeString(q.getTimeoutAt()))
                            .notifiedAt(DateTimeUtil.toLocalDateTimeString(q.getNotifiedAt()))
                            .build();
                })
                .toList();
        return PageResponse.of(list, page, pageSize, queuePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ServiceRequestDetailResponse getDetail(String id) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        ServiceRequestOrder order = getResidentRequest(id, resident.getId());
        return toDetail(order);
    }

    @Transactional
    public ServiceRequestItemResponse cancel(String id) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        ServiceRequestOrder order = getResidentRequest(id, resident.getId());
        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
        order.setStatus("cancelled");
        order.setClosedReason("用户取消");
        serviceRequestOrderRepository.save(order);
        cancelActiveQueueRows(order.getId());
        return toItem(order, loadCategoryName(order.getMatchedCategoryId()),
                loadMerchantName(order.getCurrentMerchantId()));
    }

    @Transactional
    public ServiceRequestItemResponse payQuote(String id, PayServiceRequestQuoteRequest request) {
        roleGuard.requireAnyRole("resident");
        validatePaymentMethod(request.getPaymentMethod());
        Resident resident = getActiveResident(UserContext.currentUserId());
        ServiceRequestOrder order = getResidentRequest(id, resident.getId());
        if (!"quoted".equals(order.getStatus()) || order.getQuoteAmount() == null) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        BigDecimal amount = order.getQuoteAmount();
        PaymentBreakdown breakdown = resolvePaymentBreakdown(
                request.getPaymentMethod(), amount, request.getPointUsed(), request.getCoinUsed(),
                resident.getPropertyCompanyId());
        executePaymentBreakdown(resident, order.getId(), breakdown, "服务需求报价支付");

        Merchant merchant = merchantRepository.findById(order.getCurrentMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        String providerId = resolveMerchantProviderResidentId(merchant);
        String serviceId = resolveServiceIdForRequest(order, providerId);

        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setId(idGenerator.generateId("so"));
        serviceOrder.setServiceId(serviceId);
        serviceOrder.setResidentId(resident.getId());
        serviceOrder.setProviderId(providerId);
        serviceOrder.setPropertyCompanyId(order.getPropertyCompanyId());
        serviceOrder.setAmount(amount);
        serviceOrder.setPaymentMethod(request.getPaymentMethod());
        serviceOrder.setServiceTime(order.getPreferredTime());
        serviceOrder.setContactPhone(order.getContactPhone());
        serviceOrder.setAddress(order.getAddress());
        serviceOrder.setRemark(order.getDescription());
        serviceOrder.setOrderStatus("confirmed");
        serviceOrderRepository.save(serviceOrder);

        order.setStatus("paid");
        order.setQuotePaymentMethod(request.getPaymentMethod());
        order.setServiceOrderId(serviceOrder.getId());
        serviceRequestOrderRepository.save(order);

        return toItem(order, loadCategoryName(order.getMatchedCategoryId()),
                loadMerchantName(order.getCurrentMerchantId()));
    }

    @Transactional
    public ServiceRequestItemResponse merchantAccept(String requestId) {
        Merchant merchant = requireCurrentMerchant();
        ServiceRequestOrder order = getActiveMerchantRequest(requestId, merchant.getId());
        ServiceRequestMerchantQueue queue = getPendingQueue(requestId, merchant.getId());
        assertNotTimedOut(queue);

        LocalDateTime now = LocalDateTime.now();
        queue.setStatus("accepted");
        queue.setRespondedAt(now);
        serviceRequestMerchantQueueRepository.save(queue);

        order.setStatus("negotiating");
        serviceRequestOrderRepository.save(order);
        return toItem(order, loadCategoryName(order.getMatchedCategoryId()), merchant.getName());
    }

    @Transactional
    public ServiceRequestItemResponse merchantSkip(String requestId) {
        Merchant merchant = requireCurrentMerchant();
        ServiceRequestOrder order = getActiveMerchantRequest(requestId, merchant.getId());
        ServiceRequestMerchantQueue queue = getPendingQueue(requestId, merchant.getId());

        LocalDateTime now = LocalDateTime.now();
        queue.setStatus("skipped");
        queue.setRespondedAt(now);
        serviceRequestMerchantQueueRepository.save(queue);

        advanceQueueOrClose(order);
        String merchantName = loadMerchantName(order.getCurrentMerchantId());
        return toItem(order, loadCategoryName(order.getMatchedCategoryId()), merchantName);
    }

    @Transactional
    public ServiceRequestItemResponse merchantQuote(String requestId, ServiceRequestQuoteRequest request) {
        Merchant merchant = requireCurrentMerchant();
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        ServiceRequestOrder order = getActiveMerchantRequest(requestId, merchant.getId());
        if (!"negotiating".equals(order.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "报价金额无效");
        }

        order.setQuoteAmount(request.getAmount());
        order.setQuotePaymentMethod(StringUtils.hasText(request.getCurrencyType())
                ? request.getCurrencyType() : "property_coin");
        order.setStatus("quoted");
        serviceRequestOrderRepository.save(order);

        sendQuoteMessage(order, merchantUser.getId(), request);
        return toItem(order, loadCategoryName(order.getMatchedCategoryId()), merchant.getName());
    }

    @Transactional
    public int processTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        List<ServiceRequestMerchantQueue> timedOut =
                serviceRequestMerchantQueueRepository.findByStatusAndTimeoutAtBefore("pending", now);
        int count = 0;
        for (ServiceRequestMerchantQueue queue : timedOut) {
            queue.setStatus("timeout");
            queue.setRespondedAt(now);
            serviceRequestMerchantQueueRepository.save(queue);

            ServiceRequestOrder order = serviceRequestOrderRepository.findById(queue.getRequestId()).orElse(null);
            if (order != null && "matching".equals(order.getStatus())) {
                advanceQueueOrClose(order);
                count++;
            }
        }
        return count;
    }

    private List<Merchant> resolveCandidateMerchants(String propertyCompanyId, String categoryId) {
        List<Merchant> merchants = merchantRepository.findByPropertyCompanyIdAndStatus(propertyCompanyId, "active").stream()
                .filter(m -> "approved".equals(m.getAuditStatus()))
                .toList();

        List<Merchant> enabled = new ArrayList<>();
        for (Merchant merchant : merchants) {
            MerchantMessageServiceConfig config =
                    merchantMessageServiceConfigRepository.findByMerchantId(merchant.getId()).orElse(null);
            if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
                continue;
            }
            if (StringUtils.hasText(categoryId) && StringUtils.hasText(config.getCategoryIds())) {
                try {
                    List<String> ids = objectMapper.readValue(config.getCategoryIds(), new TypeReference<>() {});
                    if (!ids.isEmpty() && !ids.contains(categoryId)) {
                        continue;
                    }
                } catch (Exception e) {
                    // 保持既有语义：解析失败视为"不限类目"（否则商家会因脏配置收不到任何需求）。
                    // 但必须记录，让脏配置能被发现并修复。
                    log.warn("[服务需求] 商家服务类目解析失败，按不限类目处理 merchantId={} raw={}",
                            merchant.getId(), config.getCategoryIds(), e);
                }
            }
            enabled.add(merchant);
        }
        enabled.sort(Comparator.comparing(Merchant::getRankOrder, Comparator.nullsLast(Integer::compareTo)));
        return enabled;
    }

    private ServiceCategoryDict matchCategory(String description) {
        if (!StringUtils.hasText(description)) {
            return null;
        }
        String text = description.toLowerCase();
        for (ServiceCategoryDict dict : serviceCategoryDictRepository.findByStatusOrderBySortOrderAsc("active")) {
            List<String> keywords = parseKeywords(dict.getKeywords());
            for (String keyword : keywords) {
                if (StringUtils.hasText(keyword) && text.contains(keyword.toLowerCase())) {
                    return dict;
                }
            }
            if (text.contains(dict.getName().toLowerCase())) {
                return dict;
            }
        }
        return null;
    }

    private ServiceCategoryItemResponse toCategoryItem(ServiceCategoryDict dict) {
        return ServiceCategoryItemResponse.builder()
                .id(dict.getId())
                .name(dict.getName())
                .keywords(parseKeywords(dict.getKeywords()))
                .sortOrder(dict.getSortOrder())
                .build();
    }

    private List<String> parseKeywords(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            // 关键词损坏会导致类目永远匹配不上，必须留痕
            log.error("[服务需求] 类目关键词解析失败，该类目将不会命中任何需求 raw={}", json, e);
            return List.of();
        }
    }

    private ServiceRequestItemResponse toItem(ServiceRequestOrder order, String categoryName, String merchantName) {
        return ServiceRequestItemResponse.builder()
                .id(order.getId())
                .description(order.getDescription())
                .status(STATUS_LABELS.getOrDefault(order.getStatus(), order.getStatus()))
                .statusCode(order.getStatus())
                .matchedCategoryName(categoryName)
                .currentMerchantName(merchantName)
                .contactPhone(order.getContactPhone())
                .address(order.getAddress())
                .quoteAmount(order.getQuoteAmount())
                .createdAt(DateTimeUtil.toLocalDateTimeString(order.getCreatedAt()))
                .build();
    }

    private ServiceRequestDetailResponse toDetail(ServiceRequestOrder order) {
        List<ServiceRequestMerchantQueue> queues =
                serviceRequestMerchantQueueRepository.findByRequestIdOrderByQueueOrderAsc(order.getId());
        Map<String, String> merchantNames = loadMerchantNames(
                queues.stream().map(ServiceRequestMerchantQueue::getMerchantId).toList());

        List<ServiceRequestQueueItemResponse> queueItems = queues.stream()
                .map(q -> ServiceRequestQueueItemResponse.builder()
                        .queueId(q.getId())
                        .merchantId(q.getMerchantId())
                        .merchantName(merchantNames.get(q.getMerchantId()))
                        .status(STATUS_LABELS.getOrDefault(q.getStatus(), q.getStatus()))
                        .statusCode(q.getStatus())
                        .queueOrder(q.getQueueOrder())
                        .notifiedAt(DateTimeUtil.toLocalDateTimeString(q.getNotifiedAt()))
                        .respondedAt(DateTimeUtil.toLocalDateTimeString(q.getRespondedAt()))
                        .build())
                .toList();

        ServiceRequestQuoteInfo quote = null;
        if (order.getQuoteAmount() != null) {
            Message quoteMessage = messageRepository.findByRelatedTaskId(order.getId()).stream()
                    .filter(m -> "service_request".equals(m.getMessageType()))
                    .findFirst()
                    .orElse(null);
            quote = ServiceRequestQuoteInfo.builder()
                    .amount(order.getQuoteAmount())
                    .currencyType(order.getQuotePaymentMethod())
                    .description(quoteMessage == null ? null : quoteMessage.getContent())
                    .build();
        }

        return ServiceRequestDetailResponse.builder()
                .id(order.getId())
                .description(order.getDescription())
                .status(STATUS_LABELS.getOrDefault(order.getStatus(), order.getStatus()))
                .statusCode(order.getStatus())
                .matchedCategoryName(loadCategoryName(order.getMatchedCategoryId()))
                .currentMerchantName(loadMerchantName(order.getCurrentMerchantId()))
                .contactPhone(order.getContactPhone())
                .address(order.getAddress())
                .preferredTime(DateTimeUtil.toLocalDateTimeString(order.getPreferredTime()))
                .quoteAmount(order.getQuoteAmount())
                .quotePaymentMethod(order.getQuotePaymentMethod())
                .closedReason(order.getClosedReason())
                .createdAt(DateTimeUtil.toLocalDateTimeString(order.getCreatedAt()))
                .queue(queueItems)
                .quote(quote)
                .build();
    }

    private ServiceRequestOrder getResidentRequest(String id, String residentId) {
        ServiceRequestOrder order = serviceRequestOrderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND));
        if (!residentId.equals(order.getResidentId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return order;
    }

    private ServiceRequestOrder getActiveMerchantRequest(String requestId, String merchantId) {
        ServiceRequestOrder order = serviceRequestOrderRepository.findById(requestId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND));
        if (!merchantId.equals(order.getCurrentMerchantId())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (!"matching".equals(order.getStatus()) && !"negotiating".equals(order.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
        return order;
    }

    private ServiceRequestMerchantQueue getPendingQueue(String requestId, String merchantId) {
        ServiceRequestMerchantQueue queue = serviceRequestMerchantQueueRepository
                .findByRequestIdAndMerchantId(requestId, merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.ORDER_STATUS_INVALID));
        if (!"pending".equals(queue.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
        return queue;
    }

    private void assertNotTimedOut(ServiceRequestMerchantQueue queue) {
        if (queue.getTimeoutAt() != null && queue.getTimeoutAt().isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.SERVICE_REQUEST_TIMEOUT);
        }
    }

    private void cancelActiveQueueRows(String requestId) {
        List<ServiceRequestMerchantQueue> queues =
                serviceRequestMerchantQueueRepository.findByRequestIdOrderByQueueOrderAsc(requestId);
        LocalDateTime now = LocalDateTime.now();
        List<ServiceRequestMerchantQueue> toUpdate = new ArrayList<>();
        for (ServiceRequestMerchantQueue queue : queues) {
            if (CANCELLABLE_QUEUE_STATUSES.contains(queue.getStatus())) {
                queue.setStatus("cancelled");
                queue.setRespondedAt(now);
                toUpdate.add(queue);
            }
        }
        if (!toUpdate.isEmpty()) {
            serviceRequestMerchantQueueRepository.saveAll(toUpdate);
        }
    }

    private void advanceQueueOrClose(ServiceRequestOrder order) {
        List<ServiceRequestMerchantQueue> queues =
                serviceRequestMerchantQueueRepository.findByRequestIdOrderByQueueOrderAsc(order.getId());
        ServiceRequestMerchantQueue next = queues.stream()
                .filter(q -> "waiting".equals(q.getStatus()))
                .findFirst()
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        if (next == null) {
            order.setStatus("closed");
            order.setClosedReason("暂无商家响应，请稍后重试");
            order.setCurrentMerchantId(null);
            serviceRequestOrderRepository.save(order);
            return;
        }

        next.setStatus("pending");
        next.setNotifiedAt(now);
        next.setTimeoutAt(now.plusMinutes(5));
        serviceRequestMerchantQueueRepository.save(next);

        order.setCurrentMerchantId(next.getMerchantId());
        order.setStatus("matching");
        serviceRequestOrderRepository.save(order);
    }

    private void sendQuoteMessage(ServiceRequestOrder order, String fromId, ServiceRequestQuoteRequest request) {
        Message message = new Message();
        message.setId(idGenerator.generateId("msg"));
        message.setFromId(fromId);
        message.setToId(order.getResidentId());
        message.setMessageType("service_request");
        message.setRelatedTaskId(order.getId());
        StringBuilder content = new StringBuilder();
        content.append("服务报价：").append(request.getAmount()).append("元");
        if (StringUtils.hasText(request.getDescription())) {
            content.append("\n").append(request.getDescription().trim());
        }
        if (request.getValidHours() != null && request.getValidHours() > 0) {
            content.append("\n有效期：").append(request.getValidHours()).append("小时");
        }
        message.setContent(content.toString());
        message.setChatType("private");
        message.setReadStatus("unread");
        message.setPropertyCompanyId(order.getPropertyCompanyId());
        messageRepository.save(message);
    }

    private void validatePaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod) || !PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod须为 point/property_coin/mixed");
        }
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
                if (pointValue.add(coins).add(cashAmount).compareTo(amount) < 0) {
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
            PaymentBreakdown breakdown,
            String remark) {
        if (breakdown.pointUsed() > 0) {
            pointService.spendPoints(
                    resident, breakdown.pointUsed(), "service_request", referenceId, remark, null, false);
        }
        if (breakdown.coinUsed().compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(
                    resident, breakdown.coinUsed(), "service_request", referenceId, remark, null, false);
        }
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

    private String resolveServiceIdForRequest(ServiceRequestOrder order, String providerId) {
        if (StringUtils.hasText(providerId)) {
            Optional<CommunityService> byProvider = communityServiceRepository
                    .findFirstByPropertyCompanyIdAndProviderIdAndStatus(
                            order.getPropertyCompanyId(), providerId, "active");
            if (byProvider.isPresent()) {
                return byProvider.get().getId();
            }
        }
        return communityServiceRepository
                .findFirstByPropertyCompanyIdAndStatus(order.getPropertyCompanyId(), "active")
                .map(CommunityService::getId)
                .orElseThrow(() -> new BizException(ErrorCode.SERVICE_NOT_FOUND, "暂无可关联的社区服务"));
    }

    private String resolveMerchantProviderResidentId(Merchant merchant) {
        if (!StringUtils.hasText(merchant.getContactPhone())) {
            return null;
        }
        return residentRepository.findByPhone(merchant.getContactPhone())
                .map(Resident::getId)
                .orElse(null);
    }

    private String loadCategoryName(String categoryId) {
        if (!StringUtils.hasText(categoryId)) {
            return null;
        }
        return serviceCategoryDictRepository.findById(categoryId).map(ServiceCategoryDict::getName).orElse(null);
    }

    private String loadMerchantName(String merchantId) {
        if (!StringUtils.hasText(merchantId)) {
            return null;
        }
        return merchantRepository.findById(merchantId).map(Merchant::getName).orElse(null);
    }

    private record PaymentBreakdown(int pointUsed, BigDecimal coinUsed, BigDecimal cashAmount) {
    }

    private Map<String, String> loadCategoryNames(List<String> ids) {
        List<String> filtered = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (filtered.isEmpty()) {
            return Map.of();
        }
        Map<String, String> map = new HashMap<>();
        serviceCategoryDictRepository.findAllById(filtered)
                .forEach(c -> map.put(c.getId(), c.getName()));
        return map;
    }

    private Map<String, String> loadMerchantNames(List<String> ids) {
        List<String> filtered = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (filtered.isEmpty()) {
            return Map.of();
        }
        Map<String, String> map = new HashMap<>();
        merchantRepository.findAllById(filtered).forEach(m -> map.put(m.getId(), m.getName()));
        return map;
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

    private Merchant requireCurrentMerchant() {
        roleGuard.requireAnyRole("merchant");
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND, "当前账号未绑定手机号，无法识别商家");
        }
        Optional<Merchant> byPhoneAndCompany = merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId());
        Merchant merchant = byPhoneAndCompany.orElseGet(() -> merchantRepository
                .findFirstByContactPhoneOrderByCreatedAtDesc(merchantUser.getPhone())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND,
                        "未找到与当前账号手机号关联的商家，请确认 merchants.contact_phone 已配置")));

        // 强制校验商家资质，未通过审核或状态异常不能接单
        if (!"approved".equals(merchant.getAuditStatus())) {
            if ("pending".equals(merchant.getAuditStatus()) || "pending_audit".equals(merchant.getAuditStatus())) {
                throw new BizException(ErrorCode.MERCHANT_AUDITING, "商家入驻审核中，暂不能接单");
            }
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND, "商家入驻审核未通过，无法接单");
        }
        if (!"active".equals(merchant.getStatus())) {
            if ("kicked".equals(merchant.getStatus())) {
                throw new BizException(ErrorCode.MERCHANT_ALREADY_KICKED);
            }
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND, "商家状态异常，无法接单");
        }
        return merchant;
    }
}
