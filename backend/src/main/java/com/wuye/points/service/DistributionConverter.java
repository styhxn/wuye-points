package com.wuye.points.service;

import com.wuye.points.dto.distribution.DistributionRecordItemResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PointPoolRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class DistributionConverter {

    private static final Map<String, String> SECTOR_LABELS = Map.of(
            "elderly", "养老板块",
            "medical", "医疗板块",
            "education", "教育板块",
            "cleaning", "保洁板块",
            "canteen", "食堂板块",
            "pet", "宠物板块",
            "life_service", "生活服务");

    public DistributionRecordItemResponse toListItem(
            DistributionRecord record,
            Order order,
            Merchant merchant,
            Resident resident,
            Map<String, Resident> residentNameMap) {
        return DistributionRecordItemResponse.builder()
                .id(record.getId())
                .orderId(record.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .merchantId(record.getMerchantId())
                .merchantName(merchant != null ? merchant.getName() : null)
                .residentId(record.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .propertyCompanyId(record.getPropertyCompanyId())
                .totalAmount(defaultDecimal(record.getTotalAmount()))
                .commissionRate(record.getCommissionRate())
                .pointCost(defaultDecimal(record.getPointCost()))
                .coinCost(defaultDecimal(record.getCoinCost()))
                .distributableAmount(record.getDistributableAmount())
                .propertyShare(defaultDecimal(record.getPropertyShare()))
                .coordinatorShare(defaultDecimal(record.getCoordinatorShare()))
                .sectorLeaderShare(defaultDecimal(record.getSectorLeaderShare()))
                .individualLeaderShare(defaultDecimal(record.getIndividualLeaderShare()))
                .coordinatorId(record.getCoordinatorId())
                .coordinatorName(resolveResidentName(record.getCoordinatorId(), residentNameMap))
                .sectorLeaderId(record.getSectorLeaderId())
                .sectorLeaderName(resolveResidentName(record.getSectorLeaderId(), residentNameMap))
                .individualLeaderId(record.getIndividualLeaderId())
                .individualLeaderName(resolveLeaderName(record.getIndividualLeaderId(), residentNameMap))
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    public String sectorLabel(String sector) {
        if (sector == null) {
            return null;
        }
        return SECTOR_LABELS.getOrDefault(sector, sector);
    }

    private String resolveResidentName(String leaderId, Map<String, Resident> leaderResidentMap) {
        if (leaderId == null) {
            return null;
        }
        Resident resident = leaderResidentMap.get(leaderId);
        return resident != null ? resident.getName() : null;
    }

    private String resolveLeaderName(String individualLeaderId, Map<String, Resident> residentNameMap) {
        return resolveResidentName(individualLeaderId, residentNameMap);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
