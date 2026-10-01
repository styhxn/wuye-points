package com.wuye.points.dto.specialoffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SpecialOffer;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SpecialOfferConverter {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "draft", "草稿",
            "published", "已发布",
            "ended", "已结束",
            "archived", "已归档");

    private final ObjectMapper objectMapper;
    private final ResidentRepository residentRepository;
    private final MerchantRepository merchantRepository;

    public SpecialOfferConverter(ObjectMapper objectMapper,
                                 ResidentRepository residentRepository,
                                 MerchantRepository merchantRepository) {
        this.objectMapper = objectMapper;
        this.residentRepository = residentRepository;
        this.merchantRepository = merchantRepository;
    }

    public SpecialOfferListItemResponse toListItem(SpecialOffer offer) {
        return toListItem(offer, null, null);
    }

    public SpecialOfferListItemResponse toListItem(SpecialOffer offer,
                                                   Map<String, Resident> publisherMap,
                                                   Map<String, Merchant> merchantMap) {
        Resident publisher = publisherMap != null
                ? publisherMap.get(offer.getPublisherId())
                : residentRepository.findById(offer.getPublisherId()).orElse(null);
        Merchant merchant = offer.getMerchantId() == null ? null
                : (merchantMap != null
                        ? merchantMap.get(offer.getMerchantId())
                        : merchantRepository.findById(offer.getMerchantId()).orElse(null));

        return SpecialOfferListItemResponse.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .content(offer.getContent())
                .targetType(offer.getTargetType())
                .targetTags(parseStringList(offer.getTargetTags()))
                .publisherId(offer.getPublisherId())
                .publisherName(publisher != null ? publisher.getName() : null)
                .publisherRole(offer.getPublisherRole())
                .merchantId(offer.getMerchantId())
                .merchantName(merchant != null ? merchant.getName() : null)
                .coverUrl(offer.getCoverUrl())
                .discountInfo(offer.getDiscountInfo())
                .minConsumption(offer.getMinConsumption())
                .totalQuota(offer.getTotalQuota())
                .perUserQuota(offer.getPerUserQuota())
                .usedQuota(offer.getUsedQuota() != null ? offer.getUsedQuota() : 0)
                .startTime(DateTimeUtil.toIsoUtc(offer.getStartTime()))
                .endTime(DateTimeUtil.toIsoUtc(offer.getEndTime()))
                .status(offer.getStatus())
                .statusLabel(STATUS_LABELS.getOrDefault(offer.getStatus(), offer.getStatus()))
                .createdAt(DateTimeUtil.toIsoUtc(offer.getCreatedAt()))
                .build();
    }

    public SpecialOfferDetailResponse toDetail(SpecialOffer offer) {
        return toDetail(offer, null, null);
    }

    public SpecialOfferDetailResponse toDetail(SpecialOffer offer,
                                               Map<String, Resident> publisherMap,
                                               Map<String, Merchant> merchantMap) {
        SpecialOfferListItemResponse base = toListItem(offer, publisherMap, merchantMap);
        return SpecialOfferDetailResponse.builder()
                .id(base.getId())
                .title(base.getTitle())
                .content(base.getContent())
                .targetType(base.getTargetType())
                .targetTags(base.getTargetTags())
                .publisherId(base.getPublisherId())
                .publisherName(base.getPublisherName())
                .publisherRole(base.getPublisherRole())
                .propertyCompanyId(offer.getPropertyCompanyId())
                .communityId(offer.getCommunityId())
                .merchantId(base.getMerchantId())
                .merchantName(base.getMerchantName())
                .coverUrl(base.getCoverUrl())
                .discountInfo(base.getDiscountInfo())
                .minConsumption(base.getMinConsumption())
                .totalQuota(base.getTotalQuota())
                .perUserQuota(base.getPerUserQuota())
                .usedQuota(base.getUsedQuota())
                .startTime(base.getStartTime())
                .endTime(base.getEndTime())
                .status(base.getStatus())
                .statusLabel(base.getStatusLabel())
                .createdAt(base.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(offer.getUpdatedAt()))
                .build();
    }

    /** 批量转换时预加载发布者/商家，避免 N+1 */
    public Map<String, Resident> loadPublishers(List<SpecialOffer> offers) {
        Set<String> ids = offers.stream()
                .map(SpecialOffer::getPublisherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));
    }

    public Map<String, Merchant> loadMerchants(List<SpecialOffer> offers) {
        Set<String> ids = offers.stream()
                .map(SpecialOffer::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return merchantRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity(), (a, b) -> a));
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
