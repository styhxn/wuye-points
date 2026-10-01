package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.activitygrouptier.CreatePricingTierRequest;
import com.wuye.points.dto.activitygrouptier.PricingTierItemResponse;
import com.wuye.points.dto.activitygrouptier.PricingTierListResponse;
import com.wuye.points.dto.activitygrouptier.UpdatePricingTierRequest;
import com.wuye.points.entity.ActivityGroup;
import com.wuye.points.entity.ActivityGroupPricingTier;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ActivityGroupPricingTierRepository;
import com.wuye.points.repository.ActivityGroupRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityGroupPricingTierService {

    private static final Set<String> PRICING_PERIODS = Set.of("monthly", "quarterly", "yearly");
    private static final Set<String> TIER_STATUS = Set.of("active", "inactive");
    private static final String[] ACTIVITY_GROUP_READ_ROLES = {
            "resident", "activity_leader", "coordinator", "sector_leader",
            "individual_leader", "property_admin", "platform_admin"
    };

    private final ActivityGroupPricingTierRepository pricingTierRepository;
    private final ActivityGroupRepository activityGroupRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PricingTierListResponse listTiers(String activityGroupId) {
        roleGuard.requireAnyRole(ACTIVITY_GROUP_READ_ROLES);
        validateActivityGroupId(activityGroupId);

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(activityGroupId, resident);

        List<ActivityGroupPricingTier> tiers =
                pricingTierRepository.findByActivityGroupIdOrderBySortOrder(group.getId());
        return PricingTierListResponse.builder()
                .activityGroupId(group.getId())
                .list(tiers.stream().map(this::toItem).toList())
                .build();
    }

    @Transactional
    public PricingTierItemResponse createTier(String activityGroupId, CreatePricingTierRequest request) {
        roleGuard.requireAnyRole("resident", "activity_leader");
        validateActivityGroupId(activityGroupId);
        validatePeriod(request.getPeriod());

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = requireLeaderOf(activityGroupId, resident);

        ActivityGroupPricingTier tier = new ActivityGroupPricingTier();
        tier.setId(idGenerator.generateId("agt"));
        tier.setActivityGroupId(group.getId());
        tier.setTierName(request.getTierName().trim());
        tier.setTierCode(request.getTierCode().trim());
        tier.setPrice(request.getPrice());
        tier.setPeriod(request.getPeriod());
        tier.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        tier.setStatus("active");
        pricingTierRepository.save(tier);
        return toItem(tier);
    }

    @Transactional
    public PricingTierItemResponse updateTier(String tierId, UpdatePricingTierRequest request) {
        roleGuard.requireAnyRole("resident", "activity_leader");
        validateTierId(tierId);

        ActivityGroupPricingTier tier = pricingTierRepository.findById(tierId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "价格级别不存在"));
        Resident resident = getActiveResident(UserContext.currentUserId());
        requireLeaderOf(tier.getActivityGroupId(), resident);

        if (StringUtils.hasText(request.getTierName())) {
            tier.setTierName(request.getTierName().trim());
        }
        if (StringUtils.hasText(request.getTierCode())) {
            tier.setTierCode(request.getTierCode().trim());
        }
        if (request.getPrice() != null) {
            tier.setPrice(request.getPrice());
        }
        if (StringUtils.hasText(request.getPeriod())) {
            validatePeriod(request.getPeriod());
            tier.setPeriod(request.getPeriod());
        }
        if (request.getSortOrder() != null) {
            tier.setSortOrder(request.getSortOrder());
        }
        if (StringUtils.hasText(request.getStatus())) {
            if (!TIER_STATUS.contains(request.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
            }
            tier.setStatus(request.getStatus());
        }
        pricingTierRepository.save(tier);
        return toItem(tier);
    }

    @Transactional
    public void deleteTier(String tierId) {
        roleGuard.requireAnyRole("resident", "activity_leader");
        validateTierId(tierId);

        ActivityGroupPricingTier tier = pricingTierRepository.findById(tierId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "价格级别不存在"));
        Resident resident = getActiveResident(UserContext.currentUserId());
        requireLeaderOf(tier.getActivityGroupId(), resident);

        pricingTierRepository.delete(tier);
    }

    /**
     * 批量替换指定活动组的全部价格级别：先清空已有级别，再按传入列表重建。
     */
    @Transactional
    public PricingTierListResponse batchReplaceTiers(String activityGroupId,
                                                     List<CreatePricingTierRequest> tiers) {
        roleGuard.requireAnyRole("resident", "activity_leader");
        validateActivityGroupId(activityGroupId);

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = requireLeaderOf(activityGroupId, resident);

        if (tiers == null) {
            throw new BizException(ErrorCode.PARAM_MISSING, "tiers不能为空");
        }

        pricingTierRepository.deleteByActivityGroupId(group.getId());
        pricingTierRepository.flush();

        int index = 0;
        for (CreatePricingTierRequest request : tiers) {
            validatePeriod(request.getPeriod());
            ActivityGroupPricingTier tier = new ActivityGroupPricingTier();
            tier.setId(idGenerator.generateId("agt"));
            tier.setActivityGroupId(group.getId());
            tier.setTierName(request.getTierName().trim());
            tier.setTierCode(request.getTierCode().trim());
            tier.setPrice(request.getPrice());
            tier.setPeriod(request.getPeriod());
            tier.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : index);
            tier.setStatus("active");
            pricingTierRepository.save(tier);
            index++;
        }

        List<ActivityGroupPricingTier> result =
                pricingTierRepository.findByActivityGroupIdOrderBySortOrder(group.getId());
        return PricingTierListResponse.builder()
                .activityGroupId(group.getId())
                .list(result.stream().map(this::toItem).toList())
                .build();
    }

    private PricingTierItemResponse toItem(ActivityGroupPricingTier tier) {
        return PricingTierItemResponse.builder()
                .id(tier.getId())
                .activityGroupId(tier.getActivityGroupId())
                .tierName(tier.getTierName())
                .tierCode(tier.getTierCode())
                .price(tier.getPrice())
                .period(tier.getPeriod())
                .sortOrder(tier.getSortOrder())
                .status(tier.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(tier.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(tier.getUpdatedAt()))
                .build();
    }

    private ActivityGroup requireLeaderOf(String activityGroupId, Resident resident) {
        ActivityGroup group = activityGroupRepository.findById(activityGroupId)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));
        if (!resident.getPropertyCompanyId().equals(group.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND);
        }
        if (!resident.getId().equals(group.getLeaderId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return group;
    }

    private ActivityGroup getReadableGroup(String activityGroupId, Resident resident) {
        ActivityGroup group = activityGroupRepository.findById(activityGroupId)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));
        if (!resident.getPropertyCompanyId().equals(group.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND);
        }
        if (roleGuard.isActivityLeader() && !resident.getId().equals(group.getLeaderId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return group;
    }

    private void validatePeriod(String period) {
        if (!StringUtils.hasText(period) || !PRICING_PERIODS.contains(period)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "period值无效");
        }
    }

    private void validateActivityGroupId(String id) {
        if (!StringUtils.hasText(id) || (!id.startsWith("act_") && !id.startsWith("ag_"))) {
            throw new BizException(ErrorCode.PARAM_INVALID, "活动组ID格式不正确");
        }
    }

    private void validateTierId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("agt_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "价格级别ID格式不正确");
        }
    }

    private Resident getActiveResident(String userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }
}
