package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.announcement.AnnouncementCollectListResponse;
import com.wuye.points.dto.announcement.AnnouncementDetailResponse;
import com.wuye.points.dto.announcement.AnnouncementListItemResponse;
import com.wuye.points.dto.announcement.CollectFieldRequest;
import com.wuye.points.dto.announcement.CollectFieldResponse;
import com.wuye.points.dto.announcement.ActivityAnnouncementRequest;
import com.wuye.points.dto.announcement.CreateAnnouncementRequest;
import com.wuye.points.dto.announcement.DeleteAnnouncementResponse;
import com.wuye.points.dto.announcement.SubmitCollectRequest;
import com.wuye.points.dto.announcement.SubmitCollectResponse;
import com.wuye.points.dto.announcement.UpdateAnnouncementRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Announcement;
import com.wuye.points.entity.AnnouncementCollect;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.AnnouncementCollectRepository;
import com.wuye.points.repository.AnnouncementRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.AnnouncementSpecifications;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "publishedAt");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCollectRepository announcementCollectRepository;
    private final ResidentRepository residentRepository;
    private final AnnouncementConverter announcementConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<AnnouncementListItemResponse> listAnnouncements(
            int page,
            int pageSize,
            String announcementType,
            String communityId,
            String merchantId,
            String readStatus,
            String sort) {
        roleGuard.requireAnyRole("resident", "activity_leader", "property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateOptionalId(communityId, "com_");
        validateOptionalId(merchantId, "mch_");
        validateAnnouncementType(announcementType);

        String propertyCompanyId = null;
        String residentRole = null;
        String residentBuilding = null;
        String residentId = UserContext.currentUserId();
        if (isResidentLike()) {
            Resident viewer = getViewerResident();
            propertyCompanyId = viewer.getPropertyCompanyId();
            residentRole = viewer.getRole();
            residentBuilding = viewer.getBuilding();
        } else if (roleGuard.isPropertyAdmin()) {
            propertyCompanyId = UserContext.currentPropertyCompanyId();
        }

        String statusFilter = isResidentLike() ? "published" : null;

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<Announcement> announcementPage = announcementRepository.findAll(
                AnnouncementSpecifications.withFilters(
                        propertyCompanyId,
                        normalizeType(announcementType),
                        communityId,
                        merchantId,
                        statusFilter,
                        residentRole,
                        residentBuilding),
                pageable);

        return buildListPage(announcementPage, page, pageSize, residentId, false);
    }

    @Transactional(readOnly = true)
    public AnnouncementDetailResponse getAnnouncement(String id) {
        roleGuard.requireAnyRole("resident", "activity_leader", "property_admin", "platform_admin");
        validateAnnouncementId(id);

        Announcement announcement = getReadableAnnouncement(id);
        Resident publisher = residentRepository.findById(announcement.getPublisherId()).orElse(null);
        String residentId = UserContext.currentUserId();
        boolean hasCollected = StringUtils.hasText(residentId)
                && announcementCollectRepository.existsByAnnouncementIdAndResidentId(id, residentId);

        return announcementConverter.toDetail(announcement, publisher, hasCollected);
    }

    @Transactional
    public AnnouncementDetailResponse createAnnouncement(CreateAnnouncementRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        return doCreateAnnouncement(request);
    }

    /**
     * 发布活动公告（v7.8）：前端调用 POST /announcements/activity，
     * 允许活动组长（activity_leader）发布，公告类型强制为 activity。
     * 使用轻量 DTO（ActivityAnnouncementRequest），<b>无需</b>前端传 announcementType。
     */
    @Transactional
    public AnnouncementDetailResponse createActivityAnnouncement(ActivityAnnouncementRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "activity_leader");

        String propertyCompanyId = resolvePropertyCompanyId(null);
        validateOptionalId(request.getCommunityId(), "com_");

        LocalDateTime publishedAt = DateTimeUtil.parseLocalDateTime(request.getPublishedAt());
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }

        Announcement announcement = new Announcement();
        announcement.setId(idGenerator.generateId("ann"));
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAnnouncementType("activity");
        announcement.setPropertyCompanyId(propertyCompanyId);
        announcement.setCommunityId(request.getCommunityId());
        announcement.setMerchantId(null);
        announcement.setPublisherId(UserContext.currentUserId());
        announcement.setPublisherRole(UserContext.currentRole());
        announcement.setActivityGroupId(request.getActivityGroupId());
        announcement.setCollectEnabled(false);
        announcement.setStatus("published");
        announcement.setPublishedAt(publishedAt);
        announcementRepository.save(announcement);

        Resident publisher = residentRepository.findById(announcement.getPublisherId()).orElse(null);
        return announcementConverter.toDetail(announcement, publisher, false);
    }

    private AnnouncementDetailResponse doCreateAnnouncement(CreateAnnouncementRequest request) {
        String normalizedType = normalizeType(request.getAnnouncementType());
        validateAnnouncementType(normalizedType);

        String propertyCompanyId = resolvePropertyCompanyId(request.getPropertyCompanyId());
        validateOptionalId(request.getCommunityId(), "com_");
        validateOptionalId(request.getMerchantId(), "mch_");

        if ("merchant".equals(normalizedType) && StringUtils.hasText(request.getMerchantId())) {
            enforceMerchantWeeklyLimit(request.getMerchantId());
        }

        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus() : "published";
        validateAnnouncementStatus(status);

        Announcement announcement = new Announcement();
        announcement.setId(idGenerator.generateId("ann"));
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAnnouncementType(normalizedType);
        announcement.setPropertyCompanyId(propertyCompanyId);
        announcement.setCommunityId(request.getCommunityId());
        announcement.setMerchantId(request.getMerchantId());
        announcement.setPublisherId(UserContext.currentUserId());
        announcement.setPublisherRole(UserContext.currentRole());
        announcement.setCoverUrls(announcementConverter.toJson(request.getCoverUrls()));
        announcement.setTargetRoles(announcementConverter.toJson(request.getTargetRoles()));
        announcement.setTargetBuildings(announcementConverter.toJson(request.getTargetBuildings()));
        announcement.setCollectEnabled(Boolean.TRUE.equals(request.getCollectEnabled()));
        announcement.setCollectFields(announcementConverter.collectFieldsToJson(request.getCollectFields()));
        announcement.setStatus(status);
        if ("published".equals(status)) {
            announcement.setPublishedAt(LocalDateTime.now());
        }
        announcementRepository.save(announcement);

        Resident publisher = residentRepository.findById(announcement.getPublisherId()).orElse(null);
        return announcementConverter.toDetail(announcement, publisher, false);
    }

    @Transactional
    public AnnouncementDetailResponse updateAnnouncement(String id, UpdateAnnouncementRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "activity_leader");
        validateAnnouncementId(id);

        Announcement announcement = getManageableAnnouncement(id);
        if (StringUtils.hasText(request.getTitle())) {
            announcement.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            announcement.setContent(request.getContent());
        }
        if (StringUtils.hasText(request.getAnnouncementType())) {
            announcement.setAnnouncementType(normalizeType(request.getAnnouncementType()));
        }
        if (request.getCommunityId() != null) {
            validateOptionalId(request.getCommunityId(), "com_");
            announcement.setCommunityId(request.getCommunityId());
        }
        if (request.getMerchantId() != null) {
            validateOptionalId(request.getMerchantId(), "mch_");
            announcement.setMerchantId(request.getMerchantId());
        }
        if (request.getCoverUrls() != null) {
            announcement.setCoverUrls(announcementConverter.toJson(request.getCoverUrls()));
        }
        if (request.getTargetRoles() != null) {
            announcement.setTargetRoles(announcementConverter.toJson(request.getTargetRoles()));
        }
        if (request.getTargetBuildings() != null) {
            announcement.setTargetBuildings(announcementConverter.toJson(request.getTargetBuildings()));
        }
        if (request.getCollectEnabled() != null) {
            announcement.setCollectEnabled(request.getCollectEnabled());
        }
        if (request.getCollectFields() != null) {
            announcement.setCollectFields(announcementConverter.collectFieldsToJson(request.getCollectFields()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            validateAnnouncementStatus(request.getStatus());
            announcement.setStatus(request.getStatus());
            if ("published".equals(request.getStatus()) && announcement.getPublishedAt() == null) {
                announcement.setPublishedAt(LocalDateTime.now());
            }
        }

        announcementRepository.save(announcement);
        Resident publisher = residentRepository.findById(announcement.getPublisherId()).orElse(null);
        return announcementConverter.toDetail(announcement, publisher, false);
    }

    @Transactional
    public DeleteAnnouncementResponse deleteAnnouncement(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "activity_leader");
        validateAnnouncementId(id);

        Announcement announcement = getManageableAnnouncement(id);
        announcement.setStatus("archived");
        announcementRepository.save(announcement);

        return DeleteAnnouncementResponse.builder()
                .id(announcement.getId())
                .status(announcement.getStatus())
                .build();
    }

    @Transactional
    public SubmitCollectResponse submitCollect(String id, SubmitCollectRequest request) {
        roleGuard.requireAnyRole("resident");
        validateAnnouncementId(id);

        Announcement announcement = getReadableAnnouncement(id);
        if (!Boolean.TRUE.equals(announcement.getCollectEnabled())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "公告未启用信息收集");
        }

        String residentId = UserContext.currentUserId();
        if (announcementCollectRepository.existsByAnnouncementIdAndResidentId(id, residentId)) {
            throw new BizException(ErrorCode.COLLECT_ALREADY_SUBMITTED);
        }

        validateCollectedData(request.getCollectedData(), announcement.getCollectFields());

        AnnouncementCollect collect = new AnnouncementCollect();
        collect.setId(idGenerator.generateId("ac"));
        collect.setAnnouncementId(id);
        collect.setResidentId(residentId);
        collect.setCollectedData(announcementConverter.collectedDataToJson(request.getCollectedData()));
        announcementCollectRepository.save(collect);

        return SubmitCollectResponse.builder()
                .id(collect.getId())
                .announcementId(collect.getAnnouncementId())
                .residentId(collect.getResidentId())
                .submittedAt(DateTimeUtil.toIsoUtc(collect.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public AnnouncementCollectListResponse listCollects(String id, int page, int pageSize, String keyword) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateAnnouncementId(id);

        Announcement announcement = getManageableAnnouncement(id);
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }

        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AnnouncementCollect> collectPage = announcementCollectRepository.findByAnnouncementId(id, pageable);

        Map<String, Resident> residentMap = residentRepository.findAllById(
                collectPage.getContent().stream().map(AnnouncementCollect::getResidentId).toList())
                .stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity()));

        List<CollectFieldResponse> fields = announcementConverter.parseCollectFields(announcement.getCollectFields());
        long totalResponses = announcementCollectRepository.countByAnnouncementId(id);

        List<AnnouncementCollectListResponse.CollectItemResponse> list = collectPage.getContent().stream()
                .map(item -> {
                    Resident resident = residentMap.get(item.getResidentId());
                    if (StringUtils.hasText(keyword) && resident != null) {
                        boolean matched = (resident.getName() != null && resident.getName().contains(keyword))
                                || (resident.getPhone() != null && resident.getPhone().contains(keyword));
                        if (!matched) {
                            return null;
                        }
                    }
                    return AnnouncementCollectListResponse.CollectItemResponse.builder()
                            .id(item.getId())
                            .residentId(item.getResidentId())
                            .residentName(resident != null ? resident.getName() : null)
                            .phone(resident != null ? resident.getPhone() : null)
                            .building(resident != null ? resident.getBuilding() : null)
                            .collectedData(parseCollectedData(item.getCollectedData()))
                            .submittedAt(DateTimeUtil.toIsoUtc(item.getCreatedAt()))
                            .build();
                })
                .filter(item -> item != null)
                .toList();

        return AnnouncementCollectListResponse.builder()
                .announcementId(announcement.getId())
                .announcementTitle(announcement.getTitle())
                .totalResponses(totalResponses)
                .responseRate(BigDecimal.ZERO)
                .fields(fields)
                .list(list)
                .pagination(PageResponse.of(list, page, pageSize, collectPage.getTotalElements()).getPagination())
                .build();
    }

    private PageResponse<AnnouncementListItemResponse> buildListPage(
            Page<Announcement> announcementPage,
            int page,
            int pageSize,
            String residentId,
            boolean fullContent) {
        Set<String> publisherIds = announcementPage.getContent().stream()
                .map(Announcement::getPublisherId)
                .collect(Collectors.toSet());
        Map<String, Resident> publisherMap = residentRepository.findAllById(publisherIds).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity()));

        List<AnnouncementListItemResponse> list = announcementPage.getContent().stream()
                .map(announcement -> {
                    boolean hasCollected = StringUtils.hasText(residentId)
                            && announcementCollectRepository.existsByAnnouncementIdAndResidentId(
                                    announcement.getId(), residentId);
                    return announcementConverter.toListItem(
                            announcement,
                            publisherMap.get(announcement.getPublisherId()),
                            hasCollected,
                            fullContent);
                })
                .toList();

        return PageResponse.of(list, page, pageSize, announcementPage.getTotalElements());
    }

    private Announcement getReadableAnnouncement(String id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        if (isResidentLike()) {
            if (!"published".equals(announcement.getStatus())) {
                throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
            }
            Resident viewer = getViewerResident();
            if (announcement.getPropertyCompanyId() != null
                    && !announcement.getPropertyCompanyId().equals(viewer.getPropertyCompanyId())) {
                throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
            }
            return announcement;
        }

        return getManageableAnnouncement(id);
    }

    private Announcement getManageableAnnouncement(String id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        if (roleGuard.isPlatformAdmin()) {
            return announcement;
        }

        // 活动组长可管理自己发布的公告（包括草稿等非 published 状态）
        if (roleGuard.isActivityLeader()
                && UserContext.currentUserId() != null
                && UserContext.currentUserId().equals(announcement.getPublisherId())) {
            return announcement;
        }

        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (propertyCompanyId == null
                || announcement.getPropertyCompanyId() == null
                || !propertyCompanyId.equals(announcement.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        return announcement;
    }

    private Resident getViewerResident() {
        return residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
    }

    /** 看公告列表/详情时按"业主视角"处理的角色：业主或活动组长 */
    private boolean isResidentLike() {
        return roleGuard.isResident() || roleGuard.isActivityLeader();
    }

    private String resolvePropertyCompanyId(String propertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(propertyCompanyId)) {
                if (!propertyCompanyId.startsWith("pc_")) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
                }
                return propertyCompanyId;
            }
            return null;
        }
        return UserContext.currentPropertyCompanyId();
    }

    private void enforceMerchantWeeklyLimit(String merchantId) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        if (announcementRepository.countRecentMerchantAnnouncements(merchantId, since) >= 1) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "商家公告每周限发1次");
        }
    }

    private void validateCollectedData(Map<String, Object> collectedData, String collectFieldsJson) {
        if (collectedData == null || collectedData.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING, "collectedData不能为空");
        }

        List<CollectFieldResponse> fields = announcementConverter.parseCollectFields(collectFieldsJson);
        for (CollectFieldResponse field : fields) {
            if (!Boolean.TRUE.equals(field.getRequired())) {
                continue;
            }
            Object value = collectedData.get(field.getKey());
            if (value == null || (value instanceof String str && !StringUtils.hasText(str))) {
                throw new BizException(ErrorCode.PARAM_MISSING, "缺少必填字段: " + field.getKey());
            }
        }
    }

    private Map<String, Object> parseCollectedData(String json) {
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        if (!StringUtils.hasText(sort)) {
            sort = "-publishedAt";
        }
        boolean desc = sort.startsWith("-");
        String field = desc ? sort.substring(1) : sort;
        if (!SORTABLE_FIELDS.contains(field)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "不支持的排序字段");
        }
        Sort sortOrder = desc ? Sort.by(Sort.Direction.DESC, field) : Sort.by(Sort.Direction.ASC, field);
        return PageRequest.of(Math.max(page, 1) - 1, pageSize, sortOrder);
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        if (type.endsWith("_announcement")) {
            return type.substring(0, type.length() - "_announcement".length());
        }
        return type;
    }

    private void validateAnnouncementType(String type) {
        if (StringUtils.hasText(type) && !RoleGuard.ANNOUNCEMENT_TYPES.contains(type)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "announcementType不合法");
        }
    }

    private void validateAnnouncementStatus(String status) {
        if (!RoleGuard.ANNOUNCEMENT_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }
    }

    private void validateAnnouncementId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("ann_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "公告ID格式不正确");
        }
    }

    private void validateOptionalId(String id, String prefix) {
        if (StringUtils.hasText(id) && !id.startsWith(prefix)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ID格式不正确");
        }
    }
}
