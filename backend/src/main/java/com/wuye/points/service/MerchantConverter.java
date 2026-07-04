package com.wuye.points.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.dto.merchant.MerchantListItemResponse;
import com.wuye.points.dto.merchant.MerchantProductItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Product;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class MerchantConverter {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public MerchantConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MerchantListItemResponse toListItem(Merchant merchant,
                                               MerchantPropertyConfig config,
                                               boolean includeMemberInfo) {
        MerchantListItemResponse.MerchantListItemResponseBuilder builder = MerchantListItemResponse.builder()
                .id(merchant.getId())
                .platformMerchantId(merchant.getPlatformMerchantId())
                .propertyCompanyId(merchant.getPropertyCompanyId())
                .name(merchant.getName())
                .description(merchant.getDescription())
                .coverUrls(parseCoverUrls(merchant.getCoverUrls()))
                .videoUrl(merchant.getVideoUrl())
                .qrCodeUrl(merchant.getQrCodeUrl())
                .merchantLevel(merchant.getMerchantLevel())
                .levelWeight(merchant.getLevelWeight())
                .category(merchant.getCategory())
                .businessHours(merchant.getBusinessHours())
                .contactPhone(merchant.getContactPhone())
                .address(merchant.getAddress())
                .deliveryFee(defaultDecimal(merchant.getDeliveryFee()))
                .freeDeliveryThreshold(merchant.getFreeDeliveryThreshold())
                .coinRebateRate(BigDecimal.ZERO)
                .coinRebateEnabled(false)
                .rankOrder(merchant.getRankOrder())
                .auditStatus(merchant.getAuditStatus())
                .status(merchant.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(merchant.getCreatedAt()));

        if (config != null) {
            builder.coinRebateRate(defaultDecimal(config.getCoinRebateRate()));
            builder.coinRebateEnabled(Boolean.TRUE.equals(config.getCoinRebateEnabled()));
            if (includeMemberInfo) {
                builder.memberDiscountPrice(config.getMemberDiscountPrice());
            }
        }

        return builder.build();
    }

    public MerchantDetailResponse toDetail(Merchant merchant,
                                           MerchantPropertyConfig config,
                                           List<Product> products,
                                           boolean includeMemberInfo) {
        MerchantListItemResponse base = toListItem(merchant, config, includeMemberInfo);
        List<MerchantProductItemResponse> productItems = products == null
                ? Collections.emptyList()
                : products.stream().map(this::toProductItem).toList();

        return MerchantDetailResponse.builder()
                .id(base.getId())
                .platformMerchantId(base.getPlatformMerchantId())
                .propertyCompanyId(base.getPropertyCompanyId())
                .name(base.getName())
                .description(base.getDescription())
                .coverUrls(base.getCoverUrls())
                .videoUrl(base.getVideoUrl())
                .qrCodeUrl(base.getQrCodeUrl())
                .merchantLevel(base.getMerchantLevel())
                .levelWeight(base.getLevelWeight())
                .category(base.getCategory())
                .businessHours(base.getBusinessHours())
                .contactPhone(base.getContactPhone())
                .address(base.getAddress())
                .deliveryFee(base.getDeliveryFee())
                .freeDeliveryThreshold(base.getFreeDeliveryThreshold())
                .memberDiscountPrice(base.getMemberDiscountPrice())
                .coinRebateRate(base.getCoinRebateRate())
                .coinRebateEnabled(base.getCoinRebateEnabled())
                .rankOrder(base.getRankOrder())
                .auditStatus(base.getAuditStatus())
                .status(base.getStatus())
                .createdAt(base.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(merchant.getUpdatedAt()))
                .products(productItems)
                .build();
    }

    public MerchantProductItemResponse toProductItem(Product product) {
        return MerchantProductItemResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .coverUrl(product.getCoverUrl())
                .price(product.getPrice())
                .memberPrice(product.getMemberPrice())
                .pointPrice(product.getPointPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .status(product.getStatus())
                .build();
    }

    public String serializeCoverUrls(List<String> coverUrls) {
        if (coverUrls == null || coverUrls.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(coverUrls);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("coverUrls格式不正确");
        }
    }

    public List<String> parseCoverUrls(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    public int levelWeightOf(String merchantLevel) {
        if ("official_certified".equals(merchantLevel)) {
            return 100;
        }
        if ("property_certified".equals(merchantLevel)) {
            return 50;
        }
        return 0;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
