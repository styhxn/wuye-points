package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.technician.TechnicianMyResponse;
import com.wuye.points.dto.technician.TechnicianTaskItemResponse;
import com.wuye.points.dto.technician.UpdateTechnicianTaskStatusRequest;
import com.wuye.points.entity.CommunityService;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ServiceOrder;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.payment.WechatSplitOwnerTypes;
import com.wuye.points.payment.WalletCardService;
import com.wuye.points.payment.WalletCardsDTO;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityServiceRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.ServiceOrderRepository;
import com.wuye.points.repository.spec.ServiceOrderSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.support.PageSupport;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicianWorkbenchService {

    private static final Set<String> TASK_STATUSES = Set.of(
            "pending", "assigned", "in_progress", "completed", "cancelled");
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "serviceTime", "updatedAt");

    /**
     * 前端技工工单状态 ↔ 服务订单 order_status：
     * assigned ↔ confirmed；其余同名。
     */
    private static final Map<String, Set<String>> TASK_TRANSITIONS = Map.of(
            "pending", Set.of("assigned", "cancelled"),
            "assigned", Set.of("in_progress", "completed", "cancelled"),
            "in_progress", Set.of("completed", "cancelled"),
            "completed", Set.of(),
            "cancelled", Set.of());

    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final CommunityServiceRepository communityServiceRepository;
    private final MessageRepository messageRepository;
    private final RoleGuard roleGuard;
    private final WalletCardService walletCardService;

    @Transactional(readOnly = true)
    public TechnicianMyResponse getMyInfo() {
        Resident technician = requireTechnician();
        String propertyName = propertyCompanyRepository.findById(technician.getPropertyCompanyId())
                .map(PropertyCompany::getName)
                .orElse(null);

        String specialty = communityServiceRepository
                .findAll((root, query, cb) -> cb.and(
                        cb.equal(root.get("providerId"), technician.getId()),
                        cb.equal(root.get("status"), "active")))
                .stream()
                .map(CommunityService::getCategory)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);

        long openCount = countByProviderAndStatuses(technician.getId(),
                List.of("pending", "confirmed", "in_progress"));
        long completedCount = countByProviderAndStatuses(technician.getId(), List.of("completed"));

        return TechnicianMyResponse.builder()
                .id(technician.getId())
                .name(technician.getName())
                .phone(PhoneUtil.mask(technician.getPhone()))
                .specialty(specialty)
                .status(technician.getStatus())
                .propertyCompanyId(technician.getPropertyCompanyId())
                .propertyName(propertyName)
                .taskCount(openCount)
                .completedCount(completedCount)
                .build();
    }

    /**
     * 技工(商家)钱包入口（HTML ②-6）：返回当前技工的两张卡视图。
     * 技工作为商家型收益主体，按 {@code MERCHANT} 主体聚合钱包余额与 微信 冻结资金。
     * 纯只读，不影响资金流。
     */
    @Transactional(readOnly = true)
    public WalletCardsDTO getWalletCards() {
        Resident technician = requireTechnician();
        return walletCardService.getWalletCards(WechatSplitOwnerTypes.MERCHANT.name(), technician.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<TechnicianTaskItemResponse> listMyTasks(
            int page, int pageSize, String status, String sort) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Resident technician = requireTechnician();
        String orderStatus = toOrderStatus(status);
        if (StringUtils.hasText(status) && orderStatus == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<ServiceOrder> orderPage = serviceOrderRepository.findAll(
                ServiceOrderSpecifications.withFilters(
                        technician.getPropertyCompanyId(), null, technician.getId(), orderStatus),
                pageable);

        Map<String, CommunityService> serviceMap = communityServiceRepository.findAllById(
                        orderPage.getContent().stream().map(ServiceOrder::getServiceId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(CommunityService::getId, Function.identity(), (a, b) -> a));
        Map<String, Resident> residentMap = residentRepository.findAllById(
                        orderPage.getContent().stream().map(ServiceOrder::getResidentId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));

        // 批量统计各住户发给我的未读私聊数（防 N+1）
        Map<String, Long> unreadBySender = messageRepository
                .countUnreadGroupBySender(technician.getId())
                .stream()
                .collect(Collectors.toMap(
                        MessageRepository.SenderUnreadCount::getSenderId,
                        MessageRepository.SenderUnreadCount::getCount,
                        (a, b) -> a));

        List<TechnicianTaskItemResponse> list = orderPage.getContent().stream()
                .map(order -> toTaskItem(
                        order,
                        serviceMap.get(order.getServiceId()),
                        residentMap.get(order.getResidentId()),
                        unreadBySender.getOrDefault(order.getResidentId(), 0L).intValue()))
                .toList();

        return PageResponse.of(list, page, pageSize, orderPage.getTotalElements());
    }

    @Transactional
    public TechnicianTaskItemResponse updateTaskStatus(String id, UpdateTechnicianTaskStatusRequest request) {
        if (!StringUtils.hasText(id) || (!id.startsWith("so_") && !id.startsWith("svc_ord"))) {
            throw new BizException(ErrorCode.PARAM_INVALID, "工单ID格式不正确");
        }
        String targetTaskStatus = normalizeTaskStatus(request.getStatus());
        if (targetTaskStatus == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }

        Resident technician = requireTechnician();
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        if (!technician.getId().equals(order.getProviderId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作该工单");
        }

        String currentTaskStatus = toTaskStatus(order.getOrderStatus());
        assertTaskTransition(currentTaskStatus, targetTaskStatus);

        order.setOrderStatus(toOrderStatus(targetTaskStatus));
        if (StringUtils.hasText(request.getRemark())) {
            order.setRemark(request.getRemark().trim());
        }
        serviceOrderRepository.save(order);

        CommunityService service = communityServiceRepository.findById(order.getServiceId()).orElse(null);
        Resident resident = residentRepository.findById(order.getResidentId()).orElse(null);
        int unread = (int) messageRepository.countUnreadByReceiverIdAndSenderId(
                technician.getId(), order.getResidentId());
        return toTaskItem(order, service, resident, unread);
    }

    private Resident requireTechnician() {
        roleGuard.requireAnyRole("technician");
        String userId = UserContext.currentUserId();
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!"technician".equals(resident.getRole())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "当前用户不是技工");
        }
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }

    private long countByProviderAndStatuses(String providerId, List<String> orderStatuses) {
        return serviceOrderRepository.count((root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("providerId"), providerId));
            predicates.add(root.get("orderStatus").in(orderStatuses));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
    }

    private TechnicianTaskItemResponse toTaskItem(
            ServiceOrder order, CommunityService service, Resident resident, int unreadCount) {
        String taskStatus = toTaskStatus(order.getOrderStatus());
        boolean completed = "completed".equals(taskStatus);
        return TechnicianTaskItemResponse.builder()
                .id(order.getId())
                .title(service != null ? service.getName() : "服务工单")
                .description(StringUtils.hasText(order.getRemark())
                        ? order.getRemark()
                        : (service != null ? service.getDescription() : null))
                .address(order.getAddress())
                .contactPhone(order.getContactPhone())
                .contactName(resident != null ? resident.getName() : null)
                .residentId(resident != null ? resident.getId() : null)
                .residentAvatarUrl(resident != null ? resident.getAvatarUrl() : null)
                .unreadCount(unreadCount)
                .status(taskStatus)
                .statusCode(taskStatus)
                .preferredTime(DateTimeUtil.toIsoUtc(order.getServiceTime()))
                .createdAt(DateTimeUtil.toIsoUtc(order.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(order.getUpdatedAt()))
                .completedAt(completed ? DateTimeUtil.toIsoUtc(order.getUpdatedAt()) : null)
                .build();
    }

    private void assertTaskTransition(String current, String target) {
        if (current == null || target == null || current.equals(target)) {
            return;
        }
        Set<String> allowed = TASK_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "工单状态不允许操作");
        }
    }

    /** 前端 status → 库内 order_status；非法返回 null */
    private String toOrderStatus(String taskStatus) {
        if (!StringUtils.hasText(taskStatus)) {
            return null;
        }
        String normalized = normalizeTaskStatus(taskStatus);
        if (normalized == null) {
            return null;
        }
        return "assigned".equals(normalized) ? "confirmed" : normalized;
    }

    /** 库内 order_status → 前端 status */
    private String toTaskStatus(String orderStatus) {
        if (!StringUtils.hasText(orderStatus)) {
            return "pending";
        }
        return "confirmed".equals(orderStatus) ? "assigned" : orderStatus;
    }

    private String normalizeTaskStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String value = status.trim();
        // 兼容直接传库内 confirmed
        if ("confirmed".equals(value)) {
            return "assigned";
        }
        return TASK_STATUSES.contains(value) ? value : null;
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        return PageSupport.build(page, pageSize, sort, SORTABLE_FIELDS, PageSupport.DEFAULT_MAX_PAGE_SIZE);
    }
}
