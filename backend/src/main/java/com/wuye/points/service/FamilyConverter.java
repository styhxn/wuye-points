package com.wuye.points.service;

import com.wuye.points.dto.family.FamilyDetailResponse;
import com.wuye.points.dto.family.FamilyMemberItemResponse;
import com.wuye.points.dto.family.FamilyResponse;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FamilyConverter {

    private final CommunityRepository communityRepository;
    private final ResidentRepository residentRepository;

    public FamilyResponse toResponse(Family family) {
        return toResponse(family, resolveOwnerName(family.getOwnerId()), resolveCommunityName(family.getCommunityId()));
    }

    public FamilyResponse toResponse(Family family, String ownerName, String communityName) {
        return FamilyResponse.builder()
                .id(family.getId())
                .name(family.getName())
                .ownerId(family.getOwnerId())
                .ownerName(ownerName)
                .communityId(family.getCommunityId())
                .communityName(communityName)
                .building(family.getBuilding())
                .unit(family.getUnit())
                .room(family.getRoom())
                .memberCount(defaultMemberCount(family))
                .pointBalance(defaultPointBalance(family))
                .coinBalance(defaultCoinBalance(family))
                .voteWeight(family.getVoteWeight() != null ? family.getVoteWeight() : 1)
                .auditStatus(family.getAuditStatus())
                .status(family.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(family.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(family.getUpdatedAt()))
                .build();
    }

    public FamilyDetailResponse toDetail(Family family, List<Resident> members) {
        String ownerName = resolveOwnerName(family.getOwnerId());
        String communityName = resolveCommunityName(family.getCommunityId());
        List<FamilyMemberItemResponse> memberItems = members.stream()
                .map(member -> toMemberItem(member, family.getOwnerId()))
                .toList();

        return FamilyDetailResponse.builder()
                .id(family.getId())
                .name(family.getName())
                .ownerId(family.getOwnerId())
                .ownerName(ownerName)
                .communityId(family.getCommunityId())
                .communityName(communityName)
                .building(family.getBuilding())
                .unit(family.getUnit())
                .room(family.getRoom())
                .memberCount(defaultMemberCount(family))
                .pointBalance(defaultPointBalance(family))
                .coinBalance(defaultCoinBalance(family))
                .voteWeight(family.getVoteWeight() != null ? family.getVoteWeight() : 1)
                .auditStatus(family.getAuditStatus())
                .status(family.getStatus())
                .members(memberItems)
                .createdAt(DateTimeUtil.toIsoUtc(family.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(family.getUpdatedAt()))
                .build();
    }

    public FamilyMemberItemResponse toMemberItem(Resident member, String ownerId) {
        boolean isOwner = ownerId != null && ownerId.equals(member.getId());
        String relation = isOwner ? "owner" : member.getFamilyRelation();
        if (relation == null) {
            relation = isOwner ? "owner" : "other";
        }

        return FamilyMemberItemResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .phone(PhoneUtil.mask(member.getPhone()))
                .avatarUrl(member.getAvatarUrl())
                .relation(relation)
                .isOwner(isOwner)
                .pointBalance(member.getPointBalance() != null ? member.getPointBalance() : 0)
                .coinBalance(member.getCoinBalance() != null ? member.getCoinBalance() : BigDecimal.ZERO)
                .status(member.getStatus())
                .joinedAt(DateTimeUtil.toIsoUtc(member.getUpdatedAt()))
                .build();
    }

    private String resolveOwnerName(String ownerId) {
        if (ownerId == null) {
            return null;
        }
        return residentRepository.findById(ownerId)
                .map(Resident::getName)
                .orElse(null);
    }

    private String resolveCommunityName(String communityId) {
        if (communityId == null) {
            return null;
        }
        return communityRepository.findById(communityId)
                .map(Community::getName)
                .orElse(null);
    }

    private Integer defaultMemberCount(Family family) {
        return family.getMemberCount() != null ? family.getMemberCount() : 0;
    }

    private Integer defaultPointBalance(Family family) {
        return family.getPointBalance() != null ? family.getPointBalance() : 0;
    }

    private BigDecimal defaultCoinBalance(Family family) {
        return family.getCoinBalance() != null ? family.getCoinBalance() : BigDecimal.ZERO;
    }
}
