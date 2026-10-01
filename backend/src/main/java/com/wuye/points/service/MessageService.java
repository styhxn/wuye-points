package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.message.BroadcastMessageRequest;
import com.wuye.points.dto.message.ConversationItemResponse;
import com.wuye.points.dto.message.CreateMessageRequest;
import com.wuye.points.dto.message.MarkMessageReadResponse;
import com.wuye.points.dto.message.MessageDetailResponse;
import com.wuye.points.dto.message.MessageListItemResponse;
import com.wuye.points.dto.message.NeighborBlockRequest;
import com.wuye.points.dto.message.NeighborBlockResponse;
import com.wuye.points.dto.message.NeighborChatRequest;
import com.wuye.points.dto.message.NeighborChatResponse;
import com.wuye.points.dto.message.NeighborLimitResponse;
import com.wuye.points.dto.message.NeighborSearchResponse;
import com.wuye.points.dto.message.UnreadCountResponse;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.NeighborBlock;
import com.wuye.points.entity.NeighborContactLimit;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.NeighborBlockRepository;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final int DEFAULT_NEIGHBOR_DAILY_LIMIT = 2;
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt");
    private static final Set<String> CONVERSATION_TYPES = Set.of(
            "neighbor", "official", "service_request", "merchant_ad");

    private final MessageRepository messageRepository;
    private final NeighborBlockRepository neighborBlockRepository;
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
        roleGuard.requireAnyRole(
                "resident", "property_admin", "platform_admin", "coordinator",
                "sector_leader", "individual_leader", "technician", "courier",
                "merchant", "activity_leader");
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

        if (roleGuard.isPlatformAdmin()) {
            propertyCompanyId = null;
        } else if (roleGuard.isResident() || roleGuard.isActivityLeader()) {
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
        roleGuard.requireAnyRole("resident", "property_admin", "platform_admin", "technician", "courier", "merchant");
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
            if (!roleGuard.isPlatformAdmin()) {
                assertSameProperty(propertyCompanyId, receiver.getPropertyCompanyId());
            }
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

        if ("technician".equals(toRole)) {
            List<Resident> technicians = residentRepository
                    .findByPropertyCompanyIdAndRole(sender.getPropertyCompanyId(), "technician");
            if (technicians.isEmpty()) {
                throw new BizException(ErrorCode.PARAM_INVALID, "当前物业下没有技术员，无法广播");
            }
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

    @Transactional(readOnly = true)
    public PageResponse<NeighborSearchResponse> searchNeighbors(int page, int pageSize, String keyword,
            String communityId, String building, String sort) {
        roleGuard.requireAnyRole("resident");

        Resident currentResident = getActiveResident(UserContext.currentUserId());
        String propertyCompanyId = currentResident.getPropertyCompanyId();

        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1,
                Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Resident> neighbors = residentRepository
                .findByPropertyCompanyIdAndStatus(propertyCompanyId, "active");

        List<NeighborSearchResponse> results = neighbors.stream()
                .filter(r -> !r.getId().equals(currentResident.getId()))
                .filter(r -> {
                    if (StringUtils.hasText(keyword)) {
                        String kw = keyword.toLowerCase();
                        return (r.getName() != null && r.getName().toLowerCase().contains(kw))
                                || (r.getRoom() != null && r.getRoom().toLowerCase().contains(kw));
                    }
                    return true;
                })
                .filter(r -> {
                    if (StringUtils.hasText(communityId)) {
                        return communityId.equals(r.getCommunityId());
                    }
                    return true;
                })
                .filter(r -> {
                    if (StringUtils.hasText(building)) {
                        return building.equals(r.getBuilding());
                    }
                    return true;
                })
                .map(r -> NeighborSearchResponse.builder()
                        .residentId(r.getId())
                        .name(r.getName())
                        .avatarUrl(r.getAvatarUrl())
                        .building(r.getBuilding())
                        .unit(r.getUnit())
                        .room(r.getRoom())
                        .communityName(null)
                        .chatStatus("not_chatted")
                        .build())
                .toList();

        int total = results.size();
        int from = (int) pageable.getOffset();
        int to = Math.min(from + pageable.getPageSize(), total);
        List<NeighborSearchResponse> pagedList = from < total ? results.subList(from, to) : List.of();

        return PageResponse.of(pagedList, page, pageSize, total);
    }

    @Transactional
    public NeighborBlockResponse blockNeighbor(NeighborBlockRequest request) {
        roleGuard.requireAnyRole("resident");

        Resident currentResident = getActiveResident(UserContext.currentUserId());
        String blockedId = request.getBlockedId();
        validateResidentId(blockedId);

        if (currentResident.getId().equals(blockedId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "不能屏蔽自己");
        }

        Resident blockedResident = getActiveResident(blockedId);
        assertSameProperty(currentResident.getPropertyCompanyId(), blockedResident.getPropertyCompanyId());

        boolean alreadyBlocked = neighborBlockRepository
                .existsByBlockerIdAndBlockedId(currentResident.getId(), blockedId);
        if (alreadyBlocked) {
            return NeighborBlockResponse.builder()
                    .success(true)
                    .action("blocked")
                    .blockedId(blockedId)
                    .message("已经屏蔽过该邻居")
                    .build();
        }

        NeighborBlock block = new NeighborBlock();
        block.setId(idGenerator.generateId("nb"));
        block.setBlockerId(currentResident.getId());
        block.setBlockedId(blockedId);
        block.setPropertyCompanyId(currentResident.getPropertyCompanyId());
        block.setBlockedAt(java.time.LocalDateTime.now());
        neighborBlockRepository.save(block);

        return NeighborBlockResponse.builder()
                .success(true)
                .action("blocked")
                .blockedId(blockedId)
                .message("已屏蔽该邻居")
                .build();
    }

    @Transactional
    public NeighborBlockResponse unblockNeighbor(NeighborBlockRequest request) {
        roleGuard.requireAnyRole("resident");

        Resident currentResident = getActiveResident(UserContext.currentUserId());
        String blockedId = request.getBlockedId();
        validateResidentId(blockedId);

        NeighborBlock block = neighborBlockRepository
                .findByBlockerIdAndBlockedId(currentResident.getId(), blockedId)
                .orElse(null);

        if (block == null) {
            return NeighborBlockResponse.builder()
                    .success(true)
                    .action("unblocked")
                    .blockedId(blockedId)
                    .message("未屏蔽该邻居")
                    .build();
        }

        neighborBlockRepository.delete(block);

        return NeighborBlockResponse.builder()
                .success(true)
                .action("unblocked")
                .blockedId(blockedId)
                .message("已取消屏蔽该邻居")
                .build();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        roleGuard.requireAnyRole("resident", "property_admin", "platform_admin", "merchant", "courier", "technician");

        String currentUserId = UserContext.currentUserId();
        if (currentUserId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }

        long totalUnread = messageRepository.countUnreadByReceiverId(currentUserId);
        long systemUnread = messageRepository.countUnreadByReceiverIdAndMessageType(currentUserId, "system_message");
        long propertyUnread = messageRepository.countUnreadByReceiverIdAndMessageType(currentUserId, "property_message");
        long merchantUnread = messageRepository.countUnreadByReceiverIdAndMessageType(currentUserId, "community_message");
        long neighborUnread = messageRepository.countUnreadSingleByReceiverId(currentUserId);

        return UnreadCountResponse.builder()
                .totalUnread((int) totalUnread)
                .systemUnread((int) systemUnread)
                .propertyUnread((int) propertyUnread)
                .merchantUnread((int) merchantUnread)
                .neighborUnread((int) neighborUnread)
                .build();
    }

    /**
     * 当前用户会话列表（§30.2 GET /messages/conversations）。
     * 按对端聚合本人参与的私聊（含官方会话），显示最后一条消息与未读数。
     * 支持 resident / activity_leader / merchant 等以居民账号存在的角色。
     */
    @Transactional(readOnly = true)
    public PageResponse<ConversationItemResponse> listMyConversations(
            int page, int pageSize, String conversationType) {
        roleGuard.requireAnyRole(
                "resident", "activity_leader", "merchant", "technician", "courier",
                "coordinator", "sector_leader", "individual_leader",
                "property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(conversationType)
                && !CONVERSATION_TYPES.contains(conversationType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "conversationType值无效");
        }

        String currentUserId = UserContext.currentUserId();
        Resident viewer = getActiveResident(currentUserId);
        String propertyCompanyId = viewer.getPropertyCompanyId();
        String officialName = StringUtils.hasText(propertyCompanyId)
                ? propertyCompanyRepository.findById(propertyCompanyId)
                        .map(PropertyCompany::getName).orElse(null)
                : null;

        List<Message> messages = messageRepository.findMyConversationMessages(propertyCompanyId, currentUserId);

        Map<String, Message> latestByPeer = new LinkedHashMap<>();
        for (Message m : messages) {
            String peerId = resolvePeerId(m, currentUserId);
            if (peerId == null) {
                continue;
            }
            if (StringUtils.hasText(conversationType)
                    && !conversationType.equals(resolveConversationType(m))) {
                continue;
            }
            latestByPeer.putIfAbsent(peerId, m);
        }

        Map<String, Long> unreadByPeer = messages.stream()
                .filter(m -> currentUserId.equals(m.getToId()) && "unread".equals(m.getReadStatus()))
                .collect(Collectors.groupingBy(Message::getFromId, Collectors.counting()));

        Map<String, Resident> residentMap = residentRepository.findAllById(latestByPeer.keySet()).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));

        List<ConversationItemResponse> all = latestByPeer.entrySet().stream()
                .map(entry -> {
                    String peerId = entry.getKey();
                    Message m = entry.getValue();
                    String conversationTypeResolved = resolveConversationType(m);
                    Resident peer = peerId.startsWith("res_") ? residentMap.get(peerId) : null;
                    String name = peer != null ? maskName(peer.getName()) : null;
                    boolean fromMe = currentUserId.equals(m.getFromId());
                    return ConversationItemResponse.builder()
                            .peerId(peerId)
                            .peerName(name)
                            .peerAvatarUrl(peer != null ? peer.getAvatarUrl() : null)
                            .conversationType(conversationTypeResolved)
                            .officialSenderType("official".equals(conversationTypeResolved)
                                    ? m.getOfficialSenderType() : null)
                            .officialDisplayName("official".equals(conversationTypeResolved)
                                    ? resolveOfficialDisplayName(m.getOfficialSenderType(), officialName) : null)
                            .lastMessage(m.getContent())
                            .lastMessageTime(DateTimeUtil.toIsoUtc(m.getCreatedAt()))
                            .lastMessageFromMe(fromMe)
                            .unreadCount(unreadByPeer.getOrDefault(peerId, 0L).intValue())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        int total = all.size();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        int from = (safePage - 1) * safeSize;
        int to = Math.min(from + safeSize, total);
        List<ConversationItemResponse> paged = from < total ? all.subList(from, to) : List.of();
        return PageResponse.of(paged, safePage, safeSize, total);
    }

    /**
     * 会话聊天记录（§30.3 GET /messages/conversations/{peerId}）。
     * peerId 支持住户 res_ 或官方会话键 official_；打开会话自动将对方发来的未读标记为已读。
     */
    @Transactional
    public PageResponse<MessageListItemResponse> getConversationMessages(
            String peerId, int page, int pageSize) {
        roleGuard.requireAnyRole(
                "resident", "activity_leader", "merchant", "technician", "courier",
                "coordinator", "sector_leader", "individual_leader",
                "property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validatePeerId(peerId);

        String currentUserId = UserContext.currentUserId();
        Resident viewer = getActiveResident(currentUserId);
        String propertyCompanyId = viewer.getPropertyCompanyId();

        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1, Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messagePage = messageRepository.findConversationBetween(
                currentUserId, peerId, propertyCompanyId, pageable);

        // 打开会话：把对方发给我的未读标记为已读
        List<Message> unreadInPage = messagePage.getContent().stream()
                .filter(m -> currentUserId.equals(m.getToId()) && "unread".equals(m.getReadStatus()))
                .toList();
        if (!unreadInPage.isEmpty()) {
            unreadInPage.forEach(m -> m.setReadStatus("read"));
            messageRepository.saveAll(unreadInPage);
        }

        return buildListPage(messagePage, page, pageSize);
    }

    private String resolveOfficialDisplayName(String officialSenderType, String propertyCompanyName) {
        if ("property".equals(officialSenderType)) {
            return StringUtils.hasText(propertyCompanyName) ? propertyCompanyName : "物业通知";
        }
        if ("platform".equals(officialSenderType)) {
            return "平台通知";
        }
        if ("system".equals(officialSenderType)) {
            return "系统通知";
        }
        return null;
    }

    private String resolveConversationType(Message message) {
        return switch (message.getMessageType()) {
            case "official_message" -> "official";
            case "service_request" -> "service_request";
            case "merchant_ad" -> "merchant_ad";
            default -> "neighbor";
        };
    }

    /** 姓名脱敏：李四 → 李*；李华明 → 李*明 */
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

    private void validatePeerId(String peerId) {
        if (!StringUtils.hasText(peerId)
                || (!peerId.startsWith("res_") && !peerId.startsWith("official_"))) {
            throw new BizException(ErrorCode.PARAM_INVALID, "peerId格式不正确");
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ConversationItemResponse> listAdminConversations(int page, int pageSize, String keyword) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        String currentUserId = UserContext.currentUserId();
        String propertyCompanyId = resolveAdminPropertyCompanyId();

        List<Message> messages = messageRepository.findConversationMessages(
                propertyCompanyId, List.of("private", "broadcast"));

        // 按对端住户聚合，保留最新一条（查询已按 createdAt 倒序）
        Map<String, Message> latestByPeer = new LinkedHashMap<>();
        for (Message m : messages) {
            String peerId = resolvePeerId(m, currentUserId);
            if (peerId == null) {
                continue;
            }
            latestByPeer.putIfAbsent(peerId, m);
        }

        // 来自各住户的未读（发给当前管理员）计数
        Map<String, Long> unreadByPeer = messages.stream()
                .filter(m -> currentUserId.equals(m.getToId()) && "unread".equals(m.getReadStatus()))
                .collect(Collectors.groupingBy(Message::getFromId, Collectors.counting()));

        Map<String, Resident> residentMap = residentRepository.findAllById(latestByPeer.keySet()).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));

        String officialName = propertyCompanyId != null
                ? propertyCompanyRepository.findById(propertyCompanyId).map(PropertyCompany::getName).orElse(null)
                : null;

        List<ConversationItemResponse> all = latestByPeer.entrySet().stream()
                .map(entry -> {
                    String peerId = entry.getKey();
                    Message m = entry.getValue();
                    Resident peer = residentMap.get(peerId);
                    String name = peer != null ? peer.getName() : null;
                    if (StringUtils.hasText(keyword)) {
                        String kw = keyword.toLowerCase();
                        boolean hit = (name != null && name.toLowerCase().contains(kw))
                                || peerId.toLowerCase().contains(kw);
                        if (!hit) {
                            return null;
                        }
                    }
                    boolean fromMe = currentUserId.equals(m.getFromId());
                    return ConversationItemResponse.builder()
                            .peerId(peerId)
                            .peerName(name)
                            .peerAvatarUrl(peer != null ? peer.getAvatarUrl() : null)
                            .conversationType("official")
                            .officialSenderType("property")
                            .officialDisplayName(officialName)
                            .lastMessage(m.getContent())
                            .lastMessageTime(DateTimeUtil.toIsoUtc(m.getCreatedAt()))
                            .lastMessageFromMe(fromMe)
                            .unreadCount(unreadByPeer.getOrDefault(peerId, 0L).intValue())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        int total = all.size();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        int from = (safePage - 1) * safeSize;
        int to = Math.min(from + safeSize, total);
        List<ConversationItemResponse> paged = from < total ? all.subList(from, to) : List.of();
        return PageResponse.of(paged, safePage, safeSize, total);
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageListItemResponse> getAdminConversationMessages(
            String residentId, int page, int pageSize) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateResidentId(residentId);
        String currentUserId = UserContext.currentUserId();
        String propertyCompanyId = resolveAdminPropertyCompanyId();

        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1, Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messagePage = messageRepository.findConversationBetween(
                currentUserId, residentId, propertyCompanyId, pageable);
        return buildListPage(messagePage, page, pageSize);
    }

    @Transactional
    public MessageDetailResponse sendAdminMessage(String residentId, String content) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateResidentId(residentId);
        if (!StringUtils.hasText(content)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "消息内容不能为空");
        }

        Resident sender = getActiveResident(UserContext.currentUserId());
        Resident receiver = getActiveResident(residentId);
        String propertyCompanyId = roleGuard.isPropertyAdmin()
                ? UserContext.currentPropertyCompanyId()
                : sender.getPropertyCompanyId();
        if (!roleGuard.isPlatformAdmin()) {
            assertSameProperty(propertyCompanyId, receiver.getPropertyCompanyId());
        }

        Message message = buildMessage(
                sender.getId(),
                receiver.getId(),
                null,
                "property_message",
                content,
                "private",
                null,
                propertyCompanyId);
        message.setOfficialSenderType("property");
        messageRepository.save(message);
        return messageConverter.toDetail(message, sender, receiver);
    }

    private String resolveAdminPropertyCompanyId() {
        if (roleGuard.isPlatformAdmin()) {
            return null;
        }
        return UserContext.currentPropertyCompanyId();
    }

    private String resolvePeerId(Message message, String currentUserId) {
        if (currentUserId.equals(message.getFromId())) {
            return message.getToId();
        }
        if (currentUserId.equals(message.getToId())) {
            return message.getFromId();
        }
        return null;
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
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
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

    /** 异步发送消息（独立事务失败不影响主流程，由调用方捕获） */
    @Async
    @Transactional
    public void sendMessageAsync(CreateMessageRequest request) {
        sendMessage(request);
    }
}
