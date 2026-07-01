package com.wuye.points.service;

import com.wuye.points.dto.activity.ActivityGroupDetailResponse;
import com.wuye.points.dto.activity.ActivityGroupListItemResponse;
import com.wuye.points.dto.activity.ActivityGroupMemberItemResponse;
import com.wuye.points.dto.activity.ActivityGroupSubscriptionItemResponse;
import com.wuye.points.dto.activity.SubscribeActivityGroupResponse;
import com.wuye.points.entity.ActivityGroup;
import com.wuye.points.entity.ActivityGroupMember;
import com.wuye.points.entity.ActivityGroupSubscription;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Resident;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ActivityGroupConverter {

    public ActivityGroupListItemResponse toListItem(ActivityGroup group,
                                                    Resident leader,
                                                    Community community,
                                                    ActivityGroupMember myMembership) {
        return ActivityGroupListItemResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .coverUrl(group.getCoverUrl())
                .leaderId(group.getLeaderId())
                .leaderName(leader != null ? leader.getName() : null)
                .communityId(group.getCommunityId())
                .communityName(community != null ? community.getName() : null)
                .memberCount(defaultCount(group.getMemberCount()))
                .monthlyFee(defaultFee(group.getMonthlyFee()))
                .yearlyFee(defaultFee(group.getYearlyFee()))
                .status(defaultStatus(group.getStatus()))
                .hasJoined(myMembership != null)
                .myRole(myMembership != null ? myMembership.getRole() : null)
                .createdAt(DateTimeUtil.toIsoUtc(group.getCreatedAt()))
                .build();
    }

    public ActivityGroupDetailResponse toDetail(ActivityGroup group,
                                                Resident leader,
                                                Community community,
                                                ActivityGroupMember myMembership) {
        ActivityGroupListItemResponse listItem = toListItem(group, leader, community, myMembership);
        return ActivityGroupDetailResponse.builder()
                .id(listItem.getId())
                .name(listItem.getName())
                .description(listItem.getDescription())
                .coverUrl(listItem.getCoverUrl())
                .leaderId(listItem.getLeaderId())
                .leaderName(listItem.getLeaderName())
                .propertyCompanyId(group.getPropertyCompanyId())
                .communityId(listItem.getCommunityId())
                .communityName(listItem.getCommunityName())
                .memberCount(listItem.getMemberCount())
                .monthlyFee(listItem.getMonthlyFee())
                .yearlyFee(listItem.getYearlyFee())
                .status(listItem.getStatus())
                .hasJoined(listItem.getHasJoined())
                .myRole(listItem.getMyRole())
                .createdAt(listItem.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(group.getUpdatedAt()))
                .build();
    }

    public ActivityGroupMemberItemResponse toMemberItem(ActivityGroupMember member, Resident resident) {
        return ActivityGroupMemberItemResponse.builder()
                .memberId(member.getId())
                .residentId(member.getResidentId())
                .name(resident != null ? resident.getName() : null)
                .avatarUrl(resident != null ? resident.getAvatarUrl() : null)
                .role(member.getRole())
                .status(member.getStatus())
                .joinedAt(DateTimeUtil.toIsoUtc(member.getJoinedAt()))
                .build();
    }

    public SubscribeActivityGroupResponse toSubscribeResponse(ActivityGroupSubscription subscription,
                                                              ActivityGroup group,
                                                              Boolean autoRenew) {
        return SubscribeActivityGroupResponse.builder()
                .subscriptionId(subscription.getId())
                .groupId(group.getId())
                .groupName(group.getName())
                .subType(subscription.getSubType())
                .amount(subscription.getAmount())
                .startDate(formatDate(subscription.getStartDate()))
                .endDate(formatDate(subscription.getEndDate()))
                .autoRenew(Boolean.TRUE.equals(autoRenew))
                .status(resolveSubscriptionStatus(subscription))
                .paidAt(DateTimeUtil.toIsoUtc(subscription.getCreatedAt()))
                .build();
    }

    public ActivityGroupSubscriptionItemResponse toSubscriptionItem(ActivityGroupSubscription subscription,
                                                                    ActivityGroup group,
                                                                    Boolean autoRenew) {
        LocalDate today = LocalDate.now();
        int daysRemaining = 0;
        if (subscription.getEndDate() != null && !subscription.getEndDate().isBefore(today)) {
            daysRemaining = (int) ChronoUnit.DAYS.between(today, subscription.getEndDate()) + 1;
        }

        return ActivityGroupSubscriptionItemResponse.builder()
                .subscriptionId(subscription.getId())
                .groupId(group.getId())
                .groupName(group.getName())
                .groupCover(group.getCoverUrl())
                .subType(subscription.getSubType())
                .amount(subscription.getAmount())
                .startDate(formatDate(subscription.getStartDate()))
                .endDate(formatDate(subscription.getEndDate()))
                .daysRemaining(daysRemaining)
                .autoRenew(Boolean.TRUE.equals(autoRenew))
                .status(resolveSubscriptionStatus(subscription))
                .paidAt(DateTimeUtil.toIsoUtc(subscription.getCreatedAt()))
                .build();
    }

    public String resolveSubscriptionStatus(ActivityGroupSubscription subscription) {
        if ("cancelled".equals(subscription.getStatus())) {
            return "cancelled";
        }
        LocalDate today = LocalDate.now();
        if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(today)) {
            return "expired";
        }
        return subscription.getStatus() != null ? subscription.getStatus() : "active";
    }

    public boolean isPaidGroup(ActivityGroup group) {
        return defaultFee(group.getMonthlyFee()).compareTo(BigDecimal.ZERO) > 0
                || defaultFee(group.getYearlyFee()).compareTo(BigDecimal.ZERO) > 0;
    }

    public int defaultCount(Integer value) {
        return value != null ? value : 0;
    }

    public BigDecimal defaultFee(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String defaultStatus(String status) {
        return status != null ? status : "active";
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }
}
