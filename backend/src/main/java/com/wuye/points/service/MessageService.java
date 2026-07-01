package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.message.BroadcastMessageRequest;
import com.wuye.points.dto.message.CreateMessageRequest;
import com.wuye.points.dto.message.MarkMessageReadResponse;
import com.wuye.points.dto.message.MessageDetailResponse;
import com.wuye.points.dto.message.MessageListItemResponse;
import com.wuye.points.dto.message.NeighborChatRequest;
import com.wuye.points.dto.message.NeighborChatResponse;
import com.wuye.points.dto.message.NeighborLimitResponse;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.NeighborContactLimit;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.NeighborContactLimitRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.MessageSpecifications;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final int DEFAULT_NEIGHBOR_DAILY_LIMIT = 2;
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt");

    private final MessageRepository messageRepository;
    private final NeighborContactLimitRepository neighborContactLimitRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ResidentRepository residentRepository;
    private final MessageConverter messageConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<MessageListItemResponse> listMessages(
            int page,
            int pageSize,
            String messageType,
            String chatType,
            String readStatus,
            String fromId,
            String sort) {
        roleGuard.requireAnyRole("resident", "property_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateOptionalEnum(messageType, RoleGuard.MESSAGE_TYPES, "messageType");
        validateOptionalEnum(chatType, RoleGuard.CHAT_TYPES, "chatType");
        validateOptionalEnum(readStatus, RoleGuard.READ_STATUS, "readStatus");
        validateOptionalId(fromId, "res_");

        String propertyCompanyId;
        String currentUserId = UserContext.currentUserId();
        String currentRole = UserContext.currentRole();
        boolean propertyAdminView = roleGuard.isPropertyAdmin();

        if (roleGuard.isResident()) {
            Resident resident = getActiveResident(currentUserId);
            propertyCompanyId = resident.getPropertyCompanyId();
            currentRole = resident.getRole();
        } else {
            propertyCompanyId = UserContext.currentPropertyCompanyId();
        }

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<Message> messagePage = messageRepository.findAll(
                MessageSpecifications.withFilters(
                        propertyCompanyId,
                        currentUserId,
                        currentRole,
                        propertyAdminView,
                        messageType,
                        chatType,
                        readStatus,
                        fromId),
                pageable);

        return buildListPage(messagePage, page, pageSize);
    }

    @Transactional(readOnly = true)
    public MessageDetailResponse getMessage(String id) {
        roleGuard.requireAnyRole("resident", "property_admin", "technician", "courier", "merchant");
        validateMessageId(id);

        Message message = getReadableMessage(id);
        Resident sender = residentRepository.findById(message.getFromId()).orElse(null);
        Resident receiver = StringUtils.hasText(message.getToId())
                ? residentRepository.findById(message.getToId()).orElse(null)
                : null;
        return messageConverter.toDetail(message, sender, receiver);
    }

    @Transactional
    public MessageDetailResponse sendMessage(CreateMessageRequest request) {
        roleGuard.requireAnyRole(
                "resident", "property_admin", "platform_admin", "coordinator",
                "merchant", "courier", "technician", "activity_leader");

        validateMessageType(request.getMessageType());
        validateChatType(request.getChatType());
        validateSendPayload(request);

        Resident sender = getActiveResident(UserContext.currentUserId());
        assertMessageTypeAllowed(sender.getRole(), request.getMessageType(), request.getChatType());

        String propertyCompanyId = sender.getPropertyCompanyId();
        if (roleGuard.isPropertyAdmin()) {
            propertyCompanyId = UserContext.currentPropertyCompanyId();
        }

        if ("private".equals(request.getChatType())
                && "resident_message".equals(request.getMessageType())
                && roleGuard.isResident()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "业主私聊请使用 /messages/neighbor-chat 接口");
        }

        Resident receiver = null;
        if (StringUtils.hasText(request.getToId())) {
            receiver = getActiveResident(request.getToId());
            assertSameProperty(propertyCompanyId, receiver.getPropertyCompanyId());
        }

        Message message = buildMessage(
                sender.getId(),
                request.getToId(),
                request.getToRole(),
                request.getMessageType(),
                request.getContent(),
                request.getChatType(),
                request.getGroupId(),
                propertyCompanyId);

        messageRepository.save(message);
        return messageConverter.toDetail(message, sender, receiver);
    }

    @Transactional
    public MessageDetailResponse broadcastMessage(BroadcastMessageRequest request) {
        roleGuard.requireAnyRole("resident");

        Resident sender = getActiveResident(UserContext.currentUserId());
        String toRole = StringUtils.hasText(request.getToRole()) ? request.getToRole() : "technician";
        if (!RoleGuard.BROADCAST_TARGET_ROLES.contains(toRole)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "toRole值无效");
        }

        Message message = buildMessage(
                sender.getId(),
                null,
                toRole,
                "technician_message",
                request.getContent(),
                "broadcast",
                null,
                sender.getPropertyCompanyId());

        messageRepository.save(message);
        return messageConverter.toDetail(message, sender, null);
    }

    @Transactional
    public MarkMessageReadResponse markRead(String id) {
        roleGuard.requireAnyRole("resident", "property_admin", "technician", "courier", "merchant");

        validateMessageId(id);
        Message message = getReadableMessage(id);
        assertCanMarkRead(message);

        message.setReadStatus("read");
        messageRepository.save(message);

        return MarkMessageReadResponse.builder()
                .id(message.getId())
                .readStatus(message.getReadStatus())
                .updatedAt(DateTimeUtil.toIsoUtc(message.getUpdatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public NeighborLimitResponse getNeighborLimit() {
        roleGuard.requireAnyRole("resident");

        Resident resident = getActiveResident(UserContext.currentUserId());
        int dailyLimit = resolveDailyLimit(resident.getPropertyCompanyId());
        int usedToday = resolveUsedToday(resident.getId());
        int remaining = Math.max(dailyLimit - usedToday, 0);

        return NeighborLimitResponse.builder()
                .residentId(resident.getId())
                .dailyLimit(dailyLimit)
                .usedToday(usedToday)
                .remaining(remaining)
                .resetAt(nextDayResetAt())
                .build();
    }

    @Transactional
    public NeighborChatResponse neighborChat(NeighborChatRequest request) {
        roleGuard.requireAnyRole("resident");

        validateResidentId(request.getToId());

        Resident sender = getActiveResident(UserContext.currentUserId());
        if (sender.getId().equals(request.getToId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "不能向自己发送消息");
        }

        Resident receiver = getActiveResident(request.getToId());
        assertSameProperty(sender.getPropertyCompanyId(), receiver.getPropertyCompanyId());

        int dailyLimit = resolveDailyLimit(sender.getPropertyCompanyId());
        NeighborContactLimit limitRecord = getOrCreateLimitRecord(sender);
        int usedToday = defaultCount(limitRecord.getContactCount());
        if (usedToday >= dailyLimit) {
            throw new BizException(
                    ErrorCode.NEIGHBOR_CONTACT_LIMIT_EXCEEDED,
                    "今日邻居私聊次数已达上限（" + dailyLimit + "次/天）");
        }

        limitRecord.setContactCount(usedToday + 1);
        neighborContactLimitRepository.save(limitRecord);

        Message message = buildMessage(
                sender.getId(),
                receiver.getId(),
                null,
                "resident_message",
                request.getContent(),
                "private",
                null,
                sender.getPropertyCompanyId());
        messageRepository.save(message);

        int remaining = Math.max(dailyLimit - limitRecord.getContactCount(), 0);
        return NeighborChatResponse.builder()
                .id(message.getId())
                .fromId(message.getFromId())
                .toId(message.getToId())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .chatType(message.getChatType())
                .readStatus(message.getReadStatus())
                .createdAt(DateTimeUtil.toIsoUtc(message.getCreatedAt()))
                .dailyLimitRemaining(remaining)
                .usedToday(limitRecord.getContactCount())
                .remaining(remaining)
                .build();
    }

    private PageResponse<MessageListItemResponse> buildListPage(Page<Message> messagePage, int page, int pageSize) {
        List<String> senderIds = messagePage.getContent().stream()
                .map(Message::getFromId)
                .distinct()
                .toList();
        Map<String, Resident> senderMap = residentRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));

        List<MessageListItemResponse> list = messagePage.getContent().stream()
                .map(message -> messageConverter.toListItem(message, senderMap.get(message.getFromId())))
                .toList();
        return PageResponse.of(list, page, pageSize, messagePage.getTotalElements());
    }

    private Message getReadableMessage(String id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.MESSAGE_NOT_FOUND));

        if (roleGuard.isPlatformAdmin()) {
            return message;
        }

        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (roleGuard.isPropertyAdmin()) {
            if (propertyCompanyId == null || !propertyCompanyId.equals(message.getPropertyCompanyId())) {
                throw new BizException(ErrorCode.MESSAGE_NOT_FOUND);
            }
            return message;
        }

        Resident viewer = getActiveResident(UserContext.currentUserId());
        if (viewer.getPropertyCompanyId() == null
                || !viewer.getPropertyCompanyId().equals(message.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        if (!canAccessMessage(message, viewer)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return message;
    }

    private boolean canAccessMessage(Message message, Resident viewer) {
        if (viewer.getId().equals(message.getFromId()) || viewer.getId().equals(message.getToId())) {
            return true;
        }
        if ("broadcast".equals(message.getChatType())) {
            if (!StringUtils.hasText(message.getToRole())) {
                return true;
            }
            return message.getToRole().equals(viewer.getRole());
        }
        return roleGuard.isPropertyAdmin();
    }

    private void assertCanMarkRead(Message message) {
        String currentUserId = UserContext.currentUserId();
        if (roleGuard.isPropertyAdmin()) {
            return;
        }
        if (currentUserId != null && currentUserId.equals(message.getToId())) {
            return;
        }
        if ("broadcast".equals(message.getChatType())) {
            Resident viewer = getActiveResident(currentUserId);
            if (!StringUtils.hasText(message.getToRole()) || message.getToRole().equals(viewer.getRole())) {
                return;
            }
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private Message buildMessage(
            String fromId,
            String toId,
            String toRole,
            String messageType,
            String content,
            String chatType,
            String groupId,
            String propertyCompanyId) {
        Message message = new Message();
        message.setId(idGenerator.generateId("msg"));
        message.setFromId(fromId);
        message.setToId(toId);
        message.setToRole(toRole);
        message.setMessageType(messageType);
        message.setContent(content.trim());
        message.setChatType(chatType);
        message.setGroupId(groupId);
        message.setReadStatus("unread");
        message.setPropertyCompanyId(propertyCompanyId);
        return message;
    }

    private NeighborContactLimit getOrCreateLimitRecord(Resident sender) {
        LocalDate today = LocalDate.now(ZONE_SHANGHAI);
        return neighborContactLimitRepository
                .findByResidentIdAndContactDate(sender.getId(), today)
                .orElseGet(() -> {
                    NeighborContactLimit record = new NeighborContactLimit();
                    record.setId(idGenerator.generateId("ncl"));
                    record.setResidentId(sender.getId());
                    record.setPropertyCompanyId(sender.getPropertyCompanyId());
                    record.setContactDate(today);
                    record.setContactCount(0);
                    return record;
                });
    }

    private int resolveUsedToday(String residentId) {
        LocalDate today = LocalDate.now(ZONE_SHANGHAI);
        return neighborContactLimitRepository.findByResidentIdAndContactDate(residentId, today)
                .map(NeighborContactLimit::getContactCount)
                .map(this::defaultCount)
                .orElse(0);
    }

    private int resolveDailyLimit(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return DEFAULT_NEIGHBOR_DAILY_LIMIT;
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getNeighborDailyContactLimit)
                .filter(limit -> limit != null && limit > 0)
                .orElse(DEFAULT_NEIGHBOR_DAILY_LIMIT);
    }

    private String nextDayResetAt() {
        LocalDate tomorrow = LocalDate.now(ZONE_SHANGHAI).plusDays(1);
        return tomorrow.atStartOfDay(ZONE_SHANGHAI).toInstant().toString();
    }

    private void validateSendPayload(CreateMessageRequest request) {
        switch (request.getChatType()) {
            case "private" -> {
                if (!StringUtils.hasText(request.getToId())) {
                    throw new BizException(ErrorCode.PARAM_MISSING, "私聊消息必须指定 toId");
                }
                validateResidentId(request.getToId());
            }
            case "broadcast" -> {
                if (!StringUtils.hasText(request.getToRole())) {
                    throw new BizException(ErrorCode.PARAM_MISSING, "群发消息必须指定 toRole");
                }
                if (!RoleGuard.BROADCAST_TARGET_ROLES.contains(request.getToRole())) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "toRole值无效");
                }
            }
            case "group" -> {
                if (!StringUtils.hasText(request.getGroupId())) {
                    throw new BizException(ErrorCode.PARAM_MISSING, "群聊消息必须指定 groupId");
                }
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "chatType值无效");
        }
    }

    private void assertMessageTypeAllowed(String role, String messageType, String chatType) {
        if (role == null) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        boolean allowed = switch (role) {
            case "resident" -> "community_message".equals(messageType) || "group".equals(chatType);
            case "property_admin" -> "property_message".equals(messageType)
                    || "community_message".equals(messageType);
            case "platform_admin", "coordinator" -> "platform_message".equals(messageType)
                    || "system_message".equals(messageType)
                    || "community_message".equals(messageType);
            case "merchant", "courier", "activity_leader" -> "community_message".equals(messageType);
            case "technician" -> "technician_message".equals(messageType);
            default -> false;
        };
        if (!allowed) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "当前角色不允许发送该类型消息");
        }
    }

    private void assertSameProperty(String senderPropertyId, String receiverPropertyId) {
        if (!StringUtils.hasText(senderPropertyId)
                || !StringUtils.hasText(receiverPropertyId)
                || !senderPropertyId.equals(receiverPropertyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "非同小区邻居，无法发送消息");
        }
    }

    private void validateMessageId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("msg_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "消息ID格式不正确");
        }
    }

    private void validateResidentId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "住户ID格式不正确");
        }
    }

    private void validateOptionalId(String id, String prefix) {
        if (StringUtils.hasText(id) && !id.startsWith(prefix)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ID格式不正确");
        }
    }

    private void validateMessageType(String messageType) {
        if (!StringUtils.hasText(messageType) || !RoleGuard.MESSAGE_TYPES.contains(messageType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "messageType值无效");
        }
    }

    private void validateChatType(String chatType) {
        if (!StringUtils.hasText(chatType) || !RoleGuard.CHAT_TYPES.contains(chatType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "chatType值无效");
        }
    }

    private void validateOptionalEnum(String value, Set<String> allowed, String fieldName) {
        if (StringUtils.hasText(value) && !allowed.contains(value)) {
            throw new BizException(ErrorCode.PARAM_INVALID, fieldName + "值无效");
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
            return Sort.by(Sort.Direction.DESC, "createdAt");
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
            field = "createdAt";
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }

    private int defaultCount(Integer value) {
        return value != null ? value : 0;
    }
}
