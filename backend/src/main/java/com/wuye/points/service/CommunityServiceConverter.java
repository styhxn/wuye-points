package com.wuye.points.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.dto.service.CommunityServiceDetailResponse;
import com.wuye.points.dto.service.CommunityServiceListItemResponse;
import com.wuye.points.dto.service.ServiceSubscriptionItemResponse;
import com.wuye.points.entity.CommunityService;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ServiceSubscription;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Component
public class CommunityServiceConverter {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public CommunityServiceConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CommunityServiceListItemResponse toListItem(CommunityService service, Resident provider) {
        return CommunityServiceListItemResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .coverUrls(parseStringList(service.getCoverUrls()))
                .category(service.getCategory())
                .price(service.getPrice())
                .memberPrice(service.getMemberPrice())
                .priceUnit(service.getPriceUnit())
                .providerId(service.getProviderId())
                .providerName(provider != null ? provider.getName() : null)
                .providerType(service.getProviderType())
                .isSubscription(Boolean.TRUE.equals(service.getIsSubscription()))
                .rankOrder(defaultInt(service.getRankOrder()))
                .status(service.getStatus())
                .propertyCompanyId(service.getPropertyCompanyId())
                .createdAt(DateTimeUtil.toIsoUtc(service.getCreatedAt()))
                .build();
    }

    public CommunityServiceDetailResponse toDetail(CommunityService service, Resident provider) {
        CommunityServiceListItemResponse listItem = toListItem(service, provider);
        return CommunityServiceDetailResponse.builder()
                .id(listItem.getId())
                .name(listItem.getName())
                .description(listItem.getDescription())
                .coverUrls(listItem.getCoverUrls())
                .category(listItem.getCategory())
                .price(listItem.getPrice())
                .memberPrice(listItem.getMemberPrice())
                .priceUnit(listItem.getPriceUnit())
                .providerId(listItem.getProviderId())
                .providerName(listItem.getProviderName())
                .providerType(listItem.getProviderType())
                .isSubscription(listItem.getIsSubscription())
                .rankOrder(listItem.getRankOrder())
                .status(listItem.getStatus())
                .propertyCompanyId(listItem.getPropertyCompanyId())
                .createdAt(listItem.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(service.getUpdatedAt()))
                .build();
    }

    public ServiceSubscriptionItemResponse toSubscriptionItem(
            ServiceSubscription subscription,
            CommunityService service) {
        List<String> coverUrls = service != null ? parseStringList(service.getCoverUrls()) : Collections.emptyList();
        String serviceCover = coverUrls.isEmpty() ? null : coverUrls.get(0);
        return ServiceSubscriptionItemResponse.builder()
                .id(subscription.getId())
                .serviceId(subscription.getServiceId())
                .serviceName(service != null ? service.getName() : null)
                .serviceCover(serviceCover)
                .subType(subscription.getSubType())
                .amount(subscription.getAmount())
                .startDate(formatDate(subscription.getStartDate()))
                .endDate(formatDate(subscription.getEndDate()))
                .autoRenew(Boolean.TRUE.equals(subscription.getAutoRenew()))
                .status(subscription.getStatus())
                .daysRemaining(calcDaysRemaining(subscription.getEndDate()))
                .createdAt(DateTimeUtil.toIsoUtc(subscription.getCreatedAt()))
                .build();
    }

    public String toJson(List<String> values) {
        if (values == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("coverUrls格式不正确", ex);
        }
    }

    public List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    private Integer calcDaysRemaining(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return (int) Math.max(days, 0);
    }

    private int defaultInt(Integer value) {
        return value != null ? value : 0;
    }
}
