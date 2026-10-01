package com.wuye.points.service;

import com.wuye.points.dto.propertycompany.AdminAccountResponse;
import com.wuye.points.dto.propertycompany.CommunityItemResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyCurrentResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyDetailResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyListItemResponse;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertyCompanyConverter {

    public PropertyCompanyCurrentResponse toCurrentResponse(PropertyCompany company) {
        return PropertyCompanyCurrentResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .contactPhone(company.getContactPhone())
                .address(company.getAddress())
                .distributionRates(PropertyCompanyCurrentResponse.DistributionRates.builder()
                        .platformRate(company.getPlatformShareRate())
                        .propertyRate(company.getPropertyShareRate())
                        .coordinatorRate(company.getCoordinatorShareRate())
                        .sectorLeaderRate(company.getSectorLeaderRate())
                        .individualLeaderRate(company.getIndividualLeaderRate())
                        .build())
                .coinDisplayEnabled(company.getCoinDisplayEnabled())
                .coinIssueMode(company.getCoinIssueMode())
                .coinExpiryDays(company.getCoinExpiryDays())
                .deliveryConfig(PropertyCompanyCurrentResponse.DeliveryConfig.builder()
                        .baseFee(company.getDeliveryBaseFee())
                        .courierShareRate(company.getDeliveryCourierShareRate())
                        .courierPerOrder(company.getDeliveryCourierPerOrder())
                        .build())
                .withdrawalFeeRate(company.getWithdrawalFeeRate())
                .pointToFeeRate(company.getPointToFeeRate())
                .twoYearClearEnabled(company.getTwoYearClearEnabled())
                .neighborDailyContactLimit(company.getNeighborDailyContactLimit())
                .build();
    }

    public PropertyCompanyListItemResponse toListItem(
            PropertyCompany company,
            long communityCount,
            long merchantCount,
            long residentCount) {
        return PropertyCompanyListItemResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .contactPhone(company.getContactPhone())
                .address(company.getAddress())
                .status(company.getStatus())
                .communityCount((int) communityCount)
                .stats(PropertyCompanyListItemResponse.PropertyCompanyStats.builder()
                        .merchantCount(merchantCount)
                        .residentCount(residentCount)
                        .build())
                .createdAt(DateTimeUtil.toIsoUtc(company.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(company.getUpdatedAt()))
                .build();
    }

    public PropertyCompanyDetailResponse toDetail(
            PropertyCompany company,
            List<CommunityItemResponse> communities,
            List<AdminAccountResponse> adminAccounts) {
        return PropertyCompanyDetailResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .contactPhone(company.getContactPhone())
                .address(company.getAddress())
                .status(company.getStatus())
                .platformShareRate(company.getPlatformShareRate())
                .propertyShareRate(company.getPropertyShareRate())
                .coordinatorShareRate(company.getCoordinatorShareRate())
                .sectorLeaderRate(company.getSectorLeaderRate())
                .individualLeaderRate(company.getIndividualLeaderRate())
                .coinDisplayEnabled(company.getCoinDisplayEnabled())
                .coinIssueMode(company.getCoinIssueMode())
                .coinExpiryDays(company.getCoinExpiryDays())
                .coinFreezeDefault(company.getCoinFreezeDefault())
                .deliveryBaseFee(company.getDeliveryBaseFee())
                .deliveryCourierShareRate(company.getDeliveryCourierShareRate())
                .deliveryCourierPerOrder(company.getDeliveryCourierPerOrder())
                .withdrawalFeeRate(company.getWithdrawalFeeRate())
                .pointToFeeRate(company.getPointToFeeRate())
                .twoYearClearEnabled(company.getTwoYearClearEnabled())
                .neighborDailyContactLimit(company.getNeighborDailyContactLimit())
                .communities(communities)
                .adminAccounts(adminAccounts)
                .createdAt(DateTimeUtil.toIsoUtc(company.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(company.getUpdatedAt()))
                .build();
    }

    public CommunityItemResponse toCommunityItem(Community community) {
        return CommunityItemResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .address(community.getAddress())
                .totalBuildings(community.getTotalBuildings())
                .totalUnits(community.getTotalUnits())
                .status(community.getStatus())
                .build();
    }

    public AdminAccountResponse toAdminAccount(Resident resident, String email) {
        return toAdminAccount(resident, email, null);
    }

    public AdminAccountResponse toAdminAccount(Resident resident, String email, String initialPassword) {
        return AdminAccountResponse.builder()
                .id(resident.getId())
                .name(resident.getName())
                .phone(resident.getPhone())
                .role(resident.getRole())
                .email(email)
                .initialPassword(initialPassword)
                .build();
    }
}
