package com.wuye.points.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.dto.announcement.AnnouncementDetailResponse;
import com.wuye.points.dto.announcement.AnnouncementListItemResponse;
import com.wuye.points.dto.announcement.CollectFieldRequest;
import com.wuye.points.dto.announcement.CollectFieldResponse;
import com.wuye.points.entity.Announcement;
import com.wuye.points.entity.Resident;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AnnouncementConverter {

    private static final int LIST_CONTENT_LIMIT = 200;
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};
    private static final TypeReference<List<CollectFieldResponse>> COLLECT_FIELD_LIST = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public AnnouncementConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AnnouncementListItemResponse toListItem(Announcement announcement,
                                                   Resident publisher,
                                                   boolean hasCollected,
                                                   boolean fullContent) {
        return AnnouncementListItemResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(formatContent(announcement.getContent(), fullContent))
                .announcementType(announcement.getAnnouncementType())
                .publisherId(announcement.getPublisherId())
                .publisherName(publisher != null ? publisher.getName() : null)
                .publisherRole(announcement.getPublisherRole())
                .coverUrls(parseStringList(announcement.getCoverUrls()))
                .collectEnabled(Boolean.TRUE.equals(announcement.getCollectEnabled()))
                .collectDeadline(null)
                .hasCollected(hasCollected)
                .readStatus("unread")
                .publishedAt(DateTimeUtil.toIsoUtc(announcement.getPublishedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(announcement.getCreatedAt()))
                .build();
    }

    public AnnouncementDetailResponse toDetail(Announcement announcement,
                                               Resident publisher,
                                               boolean hasCollected) {
        AnnouncementListItemResponse base = toListItem(announcement, publisher, hasCollected, true);
        return AnnouncementDetailResponse.builder()
                .id(base.getId())
                .title(base.getTitle())
                .content(base.getContent())
                .announcementType(base.getAnnouncementType())
                .publisherId(base.getPublisherId())
                .publisherName(base.getPublisherName())
                .publisherRole(base.getPublisherRole())
                .coverUrls(base.getCoverUrls())
                .collectEnabled(base.getCollectEnabled())
                .collectDeadline(base.getCollectDeadline())
                .hasCollected(base.getHasCollected())
                .readStatus(base.getReadStatus())
                .publishedAt(base.getPublishedAt())
                .createdAt(base.getCreatedAt())
                .propertyCompanyId(announcement.getPropertyCompanyId())
                .communityId(announcement.getCommunityId())
                .merchantId(announcement.getMerchantId())
                .targetRoles(parseStringList(announcement.getTargetRoles()))
                .targetBuildings(parseStringList(announcement.getTargetBuildings()))
                .collectFields(parseCollectFields(announcement.getCollectFields()))
                .status(announcement.getStatus())
                .updatedAt(DateTimeUtil.toIsoUtc(announcement.getUpdatedAt()))
                .build();
    }

    public String toJson(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON序列化失败", ex);
        }
    }

    public String collectFieldsToJson(List<CollectFieldRequest> fields) {
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON序列化失败", ex);
        }
    }

    public String collectedDataToJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON序列化失败", ex);
        }
    }

    public List<CollectFieldResponse> parseCollectFields(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, COLLECT_FIELD_LIST);
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }

    public List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }

    private String formatContent(String content, boolean fullContent) {
        if (content == null) {
            return null;
        }
        if (fullContent || content.length() <= LIST_CONTENT_LIMIT) {
            return content;
        }
        return content.substring(0, LIST_CONTENT_LIMIT) + "...";
    }
}
