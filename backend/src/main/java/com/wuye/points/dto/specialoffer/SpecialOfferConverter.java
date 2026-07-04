package com.wuye.points.dto.specialoffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.entity.SpecialOffer;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SpecialOfferConverter {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public SpecialOfferConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SpecialOfferListItemResponse toListItem(SpecialOffer offer) {
        return SpecialOfferListItemResponse.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .content(offer.getContent())
                .targetType(offer.getTargetType())
                .targetTags(parseStringList(offer.getTargetTags()))
                .publisherId(offer.getPublisherId())
                .publisherRole(offer.getPublisherRole())
                .merchantId(offer.getMerchantId())
                .coverUrl(offer.getCoverUrl())
                .discountInfo(offer.getDiscountInfo())
                .startTime(DateTimeUtil.toIsoUtc(offer.getStartTime()))
                .endTime(DateTimeUtil.toIsoUtc(offer.getEndTime()))
                .status(offer.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(offer.getCreatedAt()))
                .build();
    }

    public SpecialOfferDetailResponse toDetail(SpecialOffer offer) {
        SpecialOfferListItemResponse base = toListItem(offer);
        return SpecialOfferDetailResponse.builder()
                .id(base.getId())
                .title(base.getTitle())
                .content(base.getContent())
                .targetType(base.getTargetType())
                .targetTags(base.getTargetTags())
                .publisherId(base.getPublisherId())
                .publisherRole(base.getPublisherRole())
                .propertyCompanyId(offer.getPropertyCompanyId())
                .communityId(offer.getCommunityId())
                .merchantId(base.getMerchantId())
                .coverUrl(base.getCoverUrl())
                .discountInfo(base.getDiscountInfo())
                .startTime(base.getStartTime())
                .endTime(base.getEndTime())
                .status(base.getStatus())
                .createdAt(base.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(offer.getUpdatedAt()))
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
}
