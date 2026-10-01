package com.wuye.points.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.directedmessage.AgeBracketItemResponse;
import com.wuye.points.dto.directedmessage.AgeBracketListResponse;
import com.wuye.points.dto.directedmessage.CreateDirectedMessageRequest;
import com.wuye.points.dto.directedmessage.CreateDirectedMessageResponse;
import com.wuye.points.dto.directedmessage.DirectedMessageDetailResponse;
import com.wuye.points.dto.directedmessage.DirectedMessageListItemResponse;
import com.wuye.points.dto.directedmessage.DirectedMessageRecipientItemResponse;
import com.wuye.points.dto.directedmessage.UpdateAgeBracketsRequest;
import com.wuye.points.entity.AgeBracketConfig;
import com.wuye.points.entity.DirectedMessageRecipient;
import com.wuye.points.entity.DirectedMessageTask;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.AgeBracketConfigRepository;
import com.wuye.points.repository.DirectedMessageRecipientRepository;
import com.wuye.points.repository.DirectedMessageTaskRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.DirectedMessageRecipientSpecifications;
import com.wuye.points.repository.spec.DirectedMessageTaskSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.OfficialPeerIdUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectedMessageService {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final int USER_DAILY_LIMIT = 3;
    private static final int COMPANY_DAILY_TASK_LIMIT = 10;
    private static final Set<String> OFFICIAL_SENDER_TYPES = Set.of(
            "community", "property", "platform", "coordinator");
    private static final Set<String> GENDER_FILTERS = Set.of("male", "female", "all");
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final DirectedMessageTaskRepository taskRepository;
    private final DirectedMessageRecipientRepository recipientRepository;
    private final AgeBracketConfigRepository ageBracketConfigRepository;
    private final ResidentRepository residentRepository;
    private final MessageRepository messageRepository;
    private final OrderRepository orderRepository;
    private final PermissionGuard permissionGuard;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public CreateDirectedMessageResponse createAndSend(CreateDirectedMessageRequest request) {
        permissionGuard.requirePermissionOrAdmin("directed_message.send");
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");

        String propertyCompanyId = resolvePropertyCompanyId(request.getPropertyCompanyId());
        adminSupport.assertSameProperty(propertyCompanyId);
        validateCreateRequest(request);

        LocalDateTime dayStart = LocalDate.now(ZONE_SHANGHAI).atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        long companyTaskCount = taskRepository.countByPropertyCompanyIdAndCreatedAtGreaterThanEqual(
                propertyCompanyId, dayStart);
        if (companyTaskCount >= COMPANY_DAILY_TASK_LIMIT) {
            throw new BizException(ErrorCode.DIRECTED_MSG_COMPANY_DAILY_LIMIT);
        }

        String senderId = UserContext.currentUserId();
        List<Resident> targets = filterResidents(propertyCompanyId, request);
        if (targets.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "没有符合筛选条件的住户");
        }

        for (Resident target : targets) {
            long sentToday = recipientRepository.countBySenderAndRecipientToday(
                    senderId, target.getId(), dayStart, dayEnd);
            if (sentToday >= USER_DAILY_LIMIT) {
                throw new BizException(ErrorCode.DIRECTED_MSG_USER_DAILY_LIMIT);
            }
        }

        DirectedMessageTask task = new DirectedMessageTask();
        task.setId(idGenerator.generateId("dmt"));
        task.setSenderId(senderId);
        task.setPropertyCompanyId(propertyCompanyId);
        task.setOfficialSenderType(request.getOfficialSenderType());
        task.setTitle(request.getTitle().trim());
        task.setContent(request.getContent().trim());
        task.setImageUrls(toJson(request.getImageUrls()));
        task.setFilterBuildings(toJson(request.getFilterBuildings()));
        task.setFilterGender(normalizeGenderFilter(request.getFilterGender()));
        task.setFilterAgeBracketIds(toJson(request.getFilterAgeBracketIds()));
        task.setMerchantIds(toJson(request.getMerchantIds()));
        task.setProductIds(toJson(request.getProductIds()));
        task.setRecipientCount(0);
        task.setReadCount(0);
        task.setStatus("sent");
        taskRepository.save(task);

        String messageContent = buildMessageContent(task);
        String officialPeerId = OfficialPeerIdUtil.build(
                task.getOfficialSenderType(), propertyCompanyId);
        int sentCount = 0;
        for (Resident target : targets) {
            Message message = new Message();
            message.setId(idGenerator.generateId("msg"));
            message.setFromId(officialPeerId);
            message.setToId(target.getId());
            message.setMessageType("official_message");
            message.setSenderIdentity("official");
            message.setOfficialSenderType(task.getOfficialSenderType());
            message.setRelatedTaskId(task.getId());
            message.setContent(messageContent);
            message.setChatType("official");
            message.setReadStatus("unread");
            message.setPropertyCompanyId(propertyCompanyId);
            messageRepository.save(message);

            DirectedMessageRecipient recipient = new DirectedMessageRecipient();
            recipient.setId(idGenerator.generateId("dmr"));
            recipient.setTaskId(task.getId());
            recipient.setRecipientId(target.getId());
            recipient.setMessageId(message.getId());
            recipient.setReadStatus("unread");
            recipientRepository.save(recipient);
            sentCount++;
        }

        task.setRecipientCount(sentCount);
        taskRepository.save(task);

        return CreateDirectedMessageResponse.builder()
                .taskId(task.getId())
                .recipientCount(sentCount)
                .status("已发送")
                .statusCode(task.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(task.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<DirectedMessageListItemResponse> listTasks(
            int page,
            int pageSize,
            String startDate,
            String endDate,
            String officialSenderType,
            String propertyCompanyId) {
        permissionGuard.requirePermissionOrAdmin("directed_message.read");
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");

        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        LocalDateTime start = StringUtils.hasText(startDate)
                ? adminSupport.parseDate(startDate).atStartOfDay() : null;
        LocalDateTime end = StringUtils.hasText(endDate)
                ? adminSupport.parseDate(endDate).plusDays(1).atStartOfDay() : null;
        if (StringUtils.hasText(officialSenderType) && !OFFICIAL_SENDER_TYPES.contains(officialSenderType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "officialSenderType不合法");
        }

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<DirectedMessageTask> taskPage = taskRepository.findAll(
                DirectedMessageTaskSpecifications.withFilters(
                        scopedPropertyCompanyId, officialSenderType, start, end),
                pageable);

        List<DirectedMessageListItemResponse> list = taskPage.getContent().stream()
                .map(this::toListItem)
                .toList();
        return PageResponse.of(list, page, pageSize, taskPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public DirectedMessageDetailResponse getTask(String id) {
        permissionGuard.requirePermissionOrAdmin("directed_message.read");
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
        adminSupport.validateIdPrefix(id, "dmt_");

        DirectedMessageTask task = getAccessibleTask(id);
        return toDetail(task);
    }

    @Transactional(readOnly = true)
    public PageResponse<DirectedMessageRecipientItemResponse> listRecipients(
            String taskId,
            int page,
            int pageSize,
            String readStatus,
            String buildingNo,
            String gender,
            String ageBracketId) {
        permissionGuard.requirePermissionOrAdmin("directed_message.read");
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
        adminSupport.validateIdPrefix(taskId, "dmt_");

        DirectedMessageTask task = getAccessibleTask(taskId);
        if (StringUtils.hasText(readStatus) && !Set.of("read", "unread").contains(readStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "readStatus不合法");
        }
        Integer genderValue = parseGenderFilter(gender);
        List<String> ageBracketResidentIds = resolveAgeBracketResidentIds(
                task.getPropertyCompanyId(), ageBracketId);

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<DirectedMessageRecipient> recipientPage = recipientRepository.findAll(
                DirectedMessageRecipientSpecifications.withFilters(
                        taskId, readStatus, buildingNo, genderValue, ageBracketResidentIds),
                pageable);

        Map<String, Resident> residentMap = loadResidents(recipientPage.getContent().stream()
                .map(DirectedMessageRecipient::getRecipientId)
                .toList());

        List<DirectedMessageRecipientItemResponse> list = recipientPage.getContent().stream()
                .map(item -> toRecipientItem(item, residentMap.get(item.getRecipientId())))
                .toList();
        return PageResponse.of(list, page, pageSize, recipientPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AgeBracketListResponse listAgeBrackets() {
        // 统筹发定向推送时需读取年龄段筛选项；写入仍仅限物业/平台
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
        String propertyCompanyId = roleGuard.isPlatformAdmin()
                ? null : UserContext.currentPropertyCompanyId();

        List<AgeBracketConfig> configs;
        if (propertyCompanyId == null) {
            configs = ageBracketConfigRepository.findByPropertyCompanyIdIsNullOrderBySortOrderAsc();
        } else {
            configs = ageBracketConfigRepository
                    .findByPropertyCompanyIdOrPropertyCompanyIdIsNullOrderBySortOrderAsc(propertyCompanyId);
        }

        List<AgeBracketItemResponse> list = configs.stream()
                .filter(config -> !"inactive".equals(config.getStatus()))
                .map(this::toAgeBracketItem)
                .toList();
        return AgeBracketListResponse.builder().list(list).build();
    }

    @Transactional
    public AgeBracketListResponse updateAgeBrackets(UpdateAgeBracketsRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        for (UpdateAgeBracketsRequest.AgeBracketUpdateItem item : request.getItems()) {
            if (!StringUtils.hasText(item.getId())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "年龄段ID不能为空");
            }
            AgeBracketConfig config = ageBracketConfigRepository.findById(item.getId())
                    .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "年龄段配置不存在"));
            if (!roleGuard.isPlatformAdmin()) {
                if (config.getPropertyCompanyId() != null) {
                    adminSupport.assertSameProperty(config.getPropertyCompanyId());
                }
            }
            if (StringUtils.hasText(item.getLabel())) {
                config.setLabel(item.getLabel().trim());
            }
            if (item.getMinAge() != null) {
                config.setMinAge(item.getMinAge());
            }
            if (item.getMaxAge() != null) {
                config.setMaxAge(item.getMaxAge());
            }
            if (item.getSortOrder() != null) {
                config.setSortOrder(item.getSortOrder());
            }
            ageBracketConfigRepository.save(config);
        }
        return listAgeBrackets();
    }

    private DirectedMessageTask getAccessibleTask(String id) {
        DirectedMessageTask task = taskRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "定向消息任务不存在"));
        adminSupport.assertSameProperty(task.getPropertyCompanyId());
        return task;
    }

    private String resolvePropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (!StringUtils.hasText(requestedPropertyCompanyId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
            }
            adminSupport.validateIdPrefix(requestedPropertyCompanyId, "pc_");
            return requestedPropertyCompanyId;
        }
        String current = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(current)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "当前用户未关联物业公司");
        }
        return current;
    }

    private void validateCreateRequest(CreateDirectedMessageRequest request) {
        if (!OFFICIAL_SENDER_TYPES.contains(request.getOfficialSenderType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "officialSenderType不合法");
        }
        if (request.getImageUrls() != null && request.getImageUrls().size() > 9) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "图片最多9张");
        }
        String gender = normalizeGenderFilter(request.getFilterGender());
        if (!GENDER_FILTERS.contains(gender)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "filterGender不合法");
        }
    }

    private List<Resident> filterResidents(String propertyCompanyId, CreateDirectedMessageRequest request) {
        List<Resident> residents = residentRepository.findByPropertyCompanyIdAndStatus(propertyCompanyId, "active");
        residents = residents.stream()
                .filter(r -> r.getDeletedAt() == null)
                .filter(r -> !UserContext.currentUserId().equals(r.getId()))
                .toList();

        if (request.getFilterBuildings() != null && !request.getFilterBuildings().isEmpty()) {
            Set<String> buildings = new HashSet<>(request.getFilterBuildings());
            residents = residents.stream()
                    .filter(r -> StringUtils.hasText(r.getBuilding()) && buildings.contains(r.getBuilding()))
                    .toList();
        }

        String genderFilter = normalizeGenderFilter(request.getFilterGender());
        if ("male".equals(genderFilter)) {
            residents = residents.stream().filter(r -> Integer.valueOf(1).equals(r.getGender())).toList();
        } else if ("female".equals(genderFilter)) {
            residents = residents.stream().filter(r -> Integer.valueOf(2).equals(r.getGender())).toList();
        }

        if (request.getFilterAgeBracketIds() != null && !request.getFilterAgeBracketIds().isEmpty()) {
            List<AgeBracketConfig> brackets = ageBracketConfigRepository.findAllById(
                    request.getFilterAgeBracketIds());
            if (brackets.isEmpty()) {
                throw new BizException(ErrorCode.PARAM_INVALID, "年龄段配置不存在");
            }
            residents = residents.stream()
                    .filter(r -> matchesAnyBracket(r.getAge(), brackets))
                    .toList();
        }

        if (request.getMerchantIds() != null && !request.getMerchantIds().isEmpty()) {
            Set<String> merchantIdSet = new HashSet<>(request.getMerchantIds());
            residents = residents.stream()
                    .filter(r -> {
                        List<String> consumedMerchantIds = orderRepository
                                .findDistinctMerchantIdsByResidentId(r.getId());
                        return consumedMerchantIds.stream().anyMatch(merchantIdSet::contains);
                    })
                    .toList();
        }

        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            Set<String> productIdSet = new HashSet<>(request.getProductIds());
            residents = residents.stream()
                    .filter(r -> {
                        List<String> purchasedProductIds = orderRepository
                                .findDistinctProductIdsByResidentId(r.getId());
                        return purchasedProductIds.stream().anyMatch(productIdSet::contains);
                    })
                    .toList();
        }

        return residents;
    }

    private boolean matchesAnyBracket(Integer age, List<AgeBracketConfig> brackets) {
        if (age == null) {
            return false;
        }
        for (AgeBracketConfig bracket : brackets) {
            int min = bracket.getMinAge() != null ? bracket.getMinAge() : 0;
            Integer max = bracket.getMaxAge();
            if (age >= min && (max == null || age <= max)) {
                return true;
            }
        }
        return false;
    }

    private List<String> resolveAgeBracketResidentIds(String propertyCompanyId, String ageBracketId) {
        if (!StringUtils.hasText(ageBracketId)) {
            return null;
        }
        AgeBracketConfig bracket = ageBracketConfigRepository.findById(ageBracketId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "年龄段配置不存在"));
        return residentRepository.findByPropertyCompanyIdAndStatus(propertyCompanyId, "active").stream()
                .filter(r -> matchesAnyBracket(r.getAge(), List.of(bracket)))
                .map(Resident::getId)
                .toList();
    }

    private DirectedMessageListItemResponse toListItem(DirectedMessageTask task) {
        int recipientCount = task.getRecipientCount() != null ? task.getRecipientCount() : 0;
        int readCount = task.getReadCount() != null ? task.getReadCount() : 0;
        return DirectedMessageListItemResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .officialSenderType(task.getOfficialSenderType())
                .recipientCount(recipientCount)
                .readCount(readCount)
                .readRate(calcReadRate(recipientCount, readCount))
                .status("sent".equals(task.getStatus()) ? "已发送" : task.getStatus())
                .statusCode(task.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(task.getCreatedAt()))
                .build();
    }

    private DirectedMessageDetailResponse toDetail(DirectedMessageTask task) {
        int recipientCount = task.getRecipientCount() != null ? task.getRecipientCount() : 0;
        int readCount = task.getReadCount() != null ? task.getReadCount() : 0;
        return DirectedMessageDetailResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .content(task.getContent())
                .filterSummary(buildFilterSummary(task))
                .recipientCount(recipientCount)
                .readCount(readCount)
                .unreadCount(Math.max(recipientCount - readCount, 0))
                .readRate(calcReadRate(recipientCount, readCount))
                .status("sent".equals(task.getStatus()) ? "已发送" : task.getStatus())
                .statusCode(task.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(task.getCreatedAt()))
                .build();
    }

    /**
     * 住户消息标记已读后，回写定向推送收件人与任务已读统计。
     * 幂等：仅处理仍为 unread 的收件人。
     */
    @Transactional
    public void markRecipientsReadByMessageIds(Collection<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        List<String> ids = messageIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return;
        }

        List<DirectedMessageRecipient> recipients =
                recipientRepository.findByMessageIdInAndReadStatus(ids, "unread");
        if (recipients.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Map<String, Integer> readIncByTask = new HashMap<>();
        for (DirectedMessageRecipient recipient : recipients) {
            recipient.setReadStatus("read");
            recipient.setReadAt(now);
            readIncByTask.merge(recipient.getTaskId(), 1, Integer::sum);
        }
        recipientRepository.saveAll(recipients);

        for (Map.Entry<String, Integer> entry : readIncByTask.entrySet()) {
            DirectedMessageTask task = taskRepository.findById(entry.getKey()).orElse(null);
            if (task == null) {
                continue;
            }
            int current = task.getReadCount() != null ? task.getReadCount() : 0;
            task.setReadCount(current + entry.getValue());
            taskRepository.save(task);
        }
    }

    private DirectedMessageRecipientItemResponse toRecipientItem(
            DirectedMessageRecipient recipient, Resident resident) {
        String readStatus = StringUtils.hasText(recipient.getReadStatus())
                ? recipient.getReadStatus()
                : "unread";
        return DirectedMessageRecipientItemResponse.builder()
                .id(recipient.getId())
                .recipientId(recipient.getRecipientId())
                .recipientName(resident != null ? resident.getName() : null)
                .building(resident != null ? resident.getBuilding() : null)
                .gender(resident != null ? resident.getGender() : null)
                .age(resident != null ? resident.getAge() : null)
                .readStatus(readStatus)
                .readStatusCode(readStatus)
                .readAt(DateTimeUtil.toIsoUtc(recipient.getReadAt()))
                .createdAt(DateTimeUtil.toIsoUtc(recipient.getCreatedAt()))
                .build();
    }

    private AgeBracketItemResponse toAgeBracketItem(AgeBracketConfig config) {
        return AgeBracketItemResponse.builder()
                .id(config.getId())
                .label(config.getLabel())
                .minAge(config.getMinAge())
                .maxAge(config.getMaxAge())
                .sortOrder(config.getSortOrder())
                .build();
    }

    private String buildFilterSummary(DirectedMessageTask task) {
        List<String> parts = new ArrayList<>();
        List<String> buildings = parseStringList(task.getFilterBuildings());
        if (buildings.isEmpty()) {
            parts.add("全部楼栋");
        } else {
            parts.add(String.join("、", buildings));
        }
        String gender = task.getFilterGender() != null ? task.getFilterGender() : "all";
        parts.add(switch (gender) {
            case "male" -> "男性";
            case "female" -> "女性";
            default -> "全部性别";
        });
        List<String> bracketIds = parseStringList(task.getFilterAgeBracketIds());
        if (bracketIds.isEmpty()) {
            parts.add("全部年龄段");
        } else {
            List<AgeBracketConfig> brackets = ageBracketConfigRepository.findAllById(bracketIds);
            parts.add(brackets.stream().map(AgeBracketConfig::getLabel).collect(Collectors.joining("、")));
        }
        List<String> productIds = parseStringList(task.getProductIds());
        if (!productIds.isEmpty()) {
            parts.add("指定产品" + productIds.size() + "个");
        }
        return String.join(" · ", parts);
    }

    private String buildMessageContent(DirectedMessageTask task) {
        return task.getTitle() + "\n" + task.getContent();
    }

    private Double calcReadRate(int recipientCount, int readCount) {
        if (recipientCount <= 0) {
            return 0.0;
        }
        return Math.round(readCount * 1000.0 / recipientCount) / 1000.0;
    }

    private String normalizeGenderFilter(String gender) {
        return StringUtils.hasText(gender) ? gender : "all";
    }

    private Integer parseGenderFilter(String gender) {
        if (!StringUtils.hasText(gender)) {
            return null;
        }
        return switch (gender) {
            case "male" -> 1;
            case "female" -> 2;
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "gender不合法");
        };
    }

    private Map<String, Resident> loadResidents(List<String> residentIds) {
        if (residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, r -> r));
    }

    private String toJson(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "JSON序列化失败");
        }
    }

    private List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (Exception e) {
            return List.of();
        }
    }
}
