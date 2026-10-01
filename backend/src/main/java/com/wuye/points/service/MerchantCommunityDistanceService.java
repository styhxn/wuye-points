package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.merchant.CommunityDistanceItemResponse;
import com.wuye.points.dto.admin.merchant.UpdateMerchantCommunityDistancesRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantCommunityDistance;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.MerchantCommunityDistanceRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端：商家 × 小区 距离配置（替代 GPS Haversine 自动算距）。
 * 商品/商家列表按当前用户 communityId 查表写入 distanceKm；无配置则不展示距离。
 */
@Service
@RequiredArgsConstructor
public class MerchantCommunityDistanceService {

    private final MerchantRepository merchantRepository;
    private final CommunityRepository communityRepository;
    private final MerchantCommunityDistanceRepository distanceRepository;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 获取商家距离配置：返回该商家所属物业下全部小区，已配置的带 distanceKm。
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Transactional(readOnly = true)
    public List<CommunityDistanceItemResponse> getMerchantDistances(String merchantId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator", "sector_leader", "individual_leader");
        Merchant merchant = resolveMerchant(merchantId);
        adminSupport.assertSameProperty(merchant.getPropertyCompanyId());

        List<Community> communities = communityRepository.findByPropertyCompanyId(merchant.getPropertyCompanyId());
        Map<String, MerchantCommunityDistance> distanceMap = distanceRepository.findByMerchantId(merchantId)
                .stream()
                .collect(Collectors.toMap(MerchantCommunityDistance::getCommunityId, d -> d));

        return communities.stream()
                .map(community -> CommunityDistanceItemResponse.builder()
                        .communityId(community.getId())
                        .communityName(community.getName())
                        .distanceKm(distanceMap.containsKey(community.getId())
                                ? distanceMap.get(community.getId()).getDistanceKm()
                                : null)
                        .build())
                .toList();
    }

    /**
     * 配置商家距离：distanceKm >= 0 时写入/更新；null 时清除该小区配置。
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Transactional
    public List<CommunityDistanceItemResponse> updateMerchantDistances(
            String merchantId, UpdateMerchantCommunityDistancesRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator", "sector_leader", "individual_leader");
        Merchant merchant = resolveMerchant(merchantId);
        adminSupport.assertSameProperty(merchant.getPropertyCompanyId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING, "items不能为空");
        }

        Map<String, Community> communityMap = communityRepository
                .findByPropertyCompanyId(merchant.getPropertyCompanyId())
                .stream()
                .collect(Collectors.toMap(Community::getId, c -> c));

        for (UpdateMerchantCommunityDistancesRequest.Item item : request.getItems()) {
            if (!StringUtils.hasText(item.getCommunityId())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "communityId不能为空");
            }
            Community community = communityMap.get(item.getCommunityId());
            if (community == null) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "小区不存在或不属于该商家所在物业：" + item.getCommunityId());
            }
            if (item.getDistanceKm() != null
                    && item.getDistanceKm().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "distanceKm不能为负数");
            }

            if (item.getDistanceKm() == null) {
                distanceRepository.findByMerchantIdAndCommunityId(merchantId, item.getCommunityId())
                        .ifPresent(distanceRepository::delete);
                continue;
            }

            MerchantCommunityDistance distance = distanceRepository
                    .findByMerchantIdAndCommunityId(merchantId, item.getCommunityId())
                    .orElseGet(() -> {
                        MerchantCommunityDistance d = new MerchantCommunityDistance();
                        d.setId(idGenerator.generateId("mcd"));
                        d.setMerchantId(merchantId);
                        d.setCommunityId(item.getCommunityId());
                        d.setPropertyCompanyId(merchant.getPropertyCompanyId());
                        return d;
                    });
            distance.setDistanceKm(item.getDistanceKm().setScale(2, java.math.RoundingMode.HALF_UP));
            distance.setOperatorId(UserContext.currentUserId());
            distanceRepository.save(distance);
        }

        return getMerchantDistances(merchantId);
    }

    private Merchant resolveMerchant(String merchantId) {
        if (!StringUtils.hasText(merchantId) || !merchantId.startsWith("mch_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商家ID格式不正确");
        }
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }
}
