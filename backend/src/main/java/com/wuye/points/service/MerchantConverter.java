package com.wuye.points.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.dto.merchant.DeliveryFeeTier;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.dto.merchant.MerchantListItemResponse;
import com.wuye.points.dto.merchant.MerchantProductItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Product;
import com.wuye.points.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class MerchantConverter {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};
    private static final TypeReference<List<DeliveryFeeTier>> TIER_LIST = new TypeReference<>() {};

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
                .residentId(merchant.getResidentId())
                .address(merchant.getAddress())
                .deliveryFee(defaultDecimal(merchant.getDeliveryFee()))
                .freeDeliveryThreshold(merchant.getFreeDeliveryThreshold())
                .freeDeliveryEnabled(merchant.getFreeDeliveryEnabled())
                .freeDeliverySponsor(merchant.getFreeDeliverySponsor())
                .deliveryScope(merchant.getDeliveryScope())
                .deliveryFeeTiers(parseDeliveryFeeTiers(merchant.getDeliveryFeeTiers()))
                .coinRebateRate(BigDecimal.ZERO)
                .coinRebateEnabled(false)
                .rankOrder(merchant.getRankOrder())
                .auditStatus(merchant.getAuditStatus())
                .status(merchant.getStatus())
                .merchantSource(merchant.getMerchantSource())
                .shopType("resident_shop".equals(merchant.getMerchantSource()) ? "resident" : "merchant")
                .isRecommended(Boolean.TRUE.equals(merchant.getIsRecommended()))
                .recommendedSort(merchant.getRecommendedSort())
                .isDistributor(Boolean.TRUE.equals(merchant.getIsDistributor()))
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
                .residentId(base.getResidentId())
                .address(base.getAddress())
                .deliveryFee(base.getDeliveryFee())
                .freeDeliveryThreshold(base.getFreeDeliveryThreshold())
                .freeDeliveryEnabled(base.getFreeDeliveryEnabled())
                .freeDeliverySponsor(base.getFreeDeliverySponsor())
                .deliveryScope(base.getDeliveryScope())
                .deliveryFeeTiers(base.getDeliveryFeeTiers())
                .memberDiscountPrice(base.getMemberDiscountPrice())
                .coinRebateRate(base.getCoinRebateRate())
                .coinRebateEnabled(base.getCoinRebateEnabled())
                .rankOrder(base.getRankOrder())
                .auditStatus(base.getAuditStatus())
                .status(base.getStatus())
                .merchantSource(base.getMerchantSource())
                .shopType(base.getShopType())
                .isDistributor(base.getIsDistributor())
                .createdAt(base.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(merchant.getUpdatedAt()))
                .pointExchangeRate(resolvePointExchangeRate(config))
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

    public String serializeDeliveryFeeTiers(List<DeliveryFeeTier> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(tiers);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("deliveryFeeTiers格式不正确");
        }
    }

    /**
     * 解析配送费档位 JSON。
     *
     * <p>容错两点，否则档位会在读取侧静默变空、配置永远不生效（本次「档位不生效」的机制之一）：</p>
     * <ul>
     *   <li>H2（测试库）的 {@code json} 列会把整段数组再包一层字符串返回，MySQL 不会，两种形态都要能读；</li>
     *   <li>真解析失败时打 WARN，而不是无声返回空列表。</li>
     * </ul>
     */
    public List<DeliveryFeeTier> parseDeliveryFeeTiers(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        String payload = json.trim();
        if (payload.startsWith("\"") && payload.endsWith("\"")) {
            try {
                payload = objectMapper.readValue(payload, String.class);
            } catch (JsonProcessingException e) {
                log.warn("[配送费档位] 外层字符串解包失败，按未配置处理 raw={}", json, e);
                return Collections.emptyList();
            }
        }
        try {
            return objectMapper.readValue(payload, TIER_LIST);
        } catch (JsonProcessingException e) {
            log.warn("[配送费档位] JSON 解析失败，按未配置处理 raw={}", json, e);
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

    private BigDecimal resolvePointExchangeRate(MerchantPropertyConfig config) {
        if (config == null || config.getPointExchangeRate() == null
                || config.getPointExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("100.0000");
        }
        return config.getPointExchangeRate();
    }
}
