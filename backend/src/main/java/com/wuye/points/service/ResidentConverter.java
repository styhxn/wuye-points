package com.wuye.points.service;

import com.wuye.points.dto.resident.ResidentDetailResponse;
import com.wuye.points.dto.resident.ResidentListItemResponse;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ResidentConverter {

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final CommunityRepository communityRepository;
    private final FamilyRepository familyRepository;

    public ResidentConverter(PropertyCompanyRepository propertyCompanyRepository,
                             CommunityRepository communityRepository,
                             FamilyRepository familyRepository) {
        this.propertyCompanyRepository = propertyCompanyRepository;
        this.communityRepository = communityRepository;
        this.familyRepository = familyRepository;
    }

    public ResidentListItemResponse toListItem(Resident resident, Map<String, String> communityNameMap) {
        String communityName = resident.getCommunityId() == null
                ? null
                : communityNameMap.get(resident.getCommunityId());
        return ResidentListItemResponse.builder()
                .id(resident.getId())
                .name(resident.getName())
                .phone(PhoneUtil.mask(resident.getPhone()))
                .avatarUrl(resident.getAvatarUrl())
                .role(resident.getRole())
                .userType(resident.getUserType())
                .communityId(resident.getCommunityId())
                .communityName(communityName)
                .building(resident.getBuilding())
                .unit(resident.getUnit())
                .room(resident.getRoom())
                .familyId(resident.getFamilyId())
                .pointBalance(defaultPointBalance(resident))
                .coinBalance(defaultCoinBalance(resident))
                .status(resident.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(resident.getCreatedAt()))
                .build();
    }

    public ResidentDetailResponse toDetail(Resident resident,
                                           BigDecimal totalConsumption,
                                           Long totalOrders) {
        String propertyName = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                .map(PropertyCompany::getName)
                .orElse(null);
        String communityName = null;
        if (resident.getCommunityId() != null) {
            communityName = communityRepository.findById(resident.getCommunityId())
                    .map(Community::getName)
                    .orElse(null);
        }
        String familyName = null;
        if (resident.getFamilyId() != null) {
            familyName = familyRepository.findById(resident.getFamilyId())
                    .map(Family::getName)
                    .orElse(null);
        }

        return ResidentDetailResponse.builder()
                .id(resident.getId())
                .name(resident.getName())
                .phone(PhoneUtil.mask(resident.getPhone()))
                .avatarUrl(resident.getAvatarUrl())
                .gender(resident.getGender())
                .age(resident.getAge())
                .role(resident.getRole())
                .userType(resident.getUserType())
                .propertyCompanyId(resident.getPropertyCompanyId())
                .propertyName(propertyName)
                .communityId(resident.getCommunityId())
                .communityName(communityName)
                .building(resident.getBuilding())
                .unit(resident.getUnit())
                .room(resident.getRoom())
                .familyId(resident.getFamilyId())
                .familyName(familyName)
                .pointBalance(defaultPointBalance(resident))
                .coinBalance(defaultCoinBalance(resident))
                .coinFrozen(Boolean.TRUE.equals(resident.getCoinFrozen()))
                .totalConsumption(totalConsumption != null ? totalConsumption : BigDecimal.ZERO)
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .wechatBound(Boolean.TRUE.equals(resident.getWechatBound()))
                .status(resident.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(resident.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(resident.getUpdatedAt()))
                .build();
    }

    public Map<String, String> loadCommunityNameMap(Collection<Resident> residents) {
        List<String> communityIds = residents.stream()
                .map(Resident::getCommunityId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (communityIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return communityRepository.findAllById(communityIds).stream()
                .collect(Collectors.toMap(Community::getId, Community::getName));
    }

    private Integer defaultPointBalance(Resident resident) {
        return resident.getPointBalance() != null ? resident.getPointBalance() : 0;
    }

    private BigDecimal defaultCoinBalance(Resident resident) {
        return resident.getCoinBalance() != null ? resident.getCoinBalance() : BigDecimal.ZERO;
    }
}
