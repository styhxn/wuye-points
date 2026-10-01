package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantCommunityDistance;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantCommunityDistanceRepository;
import com.wuye.points.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家与「当前用户小区」的距离解析。
 * 距离来源为管理端配置（merchant_community_distances），不再使用经纬度 Haversine；
 * 无配置时不展示距离（返回 null）。
 */
@Component
@RequiredArgsConstructor
public class MerchantDistanceService {

    private final ResidentRepository residentRepository;
    private final MerchantCommunityDistanceRepository distanceRepository;

    /**
     * 商家与当前登录用户小区的配置距离（公里）；用户未登录、无小区或未配置时返回 null。
     */
    public BigDecimal resolveDistanceKm(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        String communityId = resolveUserCommunityId();
        if (communityId == null) {
            return null;
        }
        return distanceRepository.findByMerchantIdAndCommunityId(merchant.getId(), communityId)
                .map(MerchantCommunityDistance::getDistanceKm)
                .orElse(null);
    }

    /**
     * 商家与指定小区（communityId）的配置距离（公里）；小区为空或未配置时返回 null。
     */
    public BigDecimal resolveDistanceKm(Merchant merchant, String communityId) {
        if (merchant == null || !StringUtils.hasText(communityId)) {
            return null;
        }
        return distanceRepository.findByMerchantIdAndCommunityId(merchant.getId(), communityId)
                .map(MerchantCommunityDistance::getDistanceKm)
                .orElse(null);
    }

    /**
     * 批量解析多个商家与指定小区的配置距离（一次 IN 查询，避免列表 N+1）。
     * 返回 merchantId → distanceKm；未配置的商家不包含在结果中。
     */
    public Map<String, BigDecimal> resolveDistanceMap(Collection<Merchant> merchants, String communityId) {
        if (merchants == null || merchants.isEmpty() || !StringUtils.hasText(communityId)) {
            return Collections.emptyMap();
        }
        List<String> merchantIds = merchants.stream()
                .map(Merchant::getId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (merchantIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return distanceRepository.findByCommunityIdAndMerchantIdIn(communityId, merchantIds)
                .stream()
                .collect(Collectors.toMap(MerchantCommunityDistance::getMerchantId,
                        MerchantCommunityDistance::getDistanceKm));
    }

    /**
     * 批量解析多个商家与当前登录用户小区的配置距离。
     */
    public Map<String, BigDecimal> resolveDistanceMap(Collection<Merchant> merchants) {
        String communityId = resolveUserCommunityId();
        return resolveDistanceMap(merchants, communityId);
    }

    private String resolveUserCommunityId() {
        String userId = UserContext.currentUserId();
        if (userId == null) {
            return null;
        }
        Resident resident = residentRepository.findById(userId).orElse(null);
        if (resident == null || !StringUtils.hasText(resident.getCommunityId())) {
            return null;
        }
        return resident.getCommunityId();
    }
}
