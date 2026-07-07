package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.sectorleader.SectorLeaderMyResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectorLeaderWorkbenchService {

    private final SectorLeaderRepository sectorLeaderRepository;
    private final ResidentRepository residentRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final MerchantRepository merchantRepository;
    private final DistributionConverter distributionConverter;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public SectorLeaderMyResponse getMyInfo() {
        roleGuard.requireAnyRole("sector_leader");

        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();

        SectorLeader leader = sectorLeaderRepository
                .findByResidentIdAndPropertyCompanyIdAndStatus(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.SECTOR_LEADER_NOT_FOUND, "当前用户不是板块负责人"));

        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        PropertyCompany company = propertyCompanyRepository.findById(leader.getPropertyCompanyId()).orElse(null);

        String coordinatorName = null;
        if (leader.getCoordinatorId() != null) {
            Coordinator coordinator = coordinatorRepository.findById(leader.getCoordinatorId()).orElse(null);
            if (coordinator != null) {
                Resident cooResident = residentRepository.findById(coordinator.getResidentId()).orElse(null);
                coordinatorName = cooResident != null ? cooResident.getName() : null;
            }
        }

        long individualLeaderCount = individualLeaderRepository.countBySectorLeaderIdAndStatus(leader.getId(), "active");
        long merchantCount = merchantRepository.countByPropertyCompanyId(leader.getPropertyCompanyId());
        long activeOfferCount = 0;
        if (leader.getPropertyCompanyId() != null) {
            activeOfferCount = distributionRecordRepository.sumSectorLeaderShareBySectorLeaderId(leader.getId()) != null ? 0 : 0;
        }

        return SectorLeaderMyResponse.builder()
                .id(leader.getId())
                .residentName(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(leader.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .sector(leader.getSector())
                .sectorName(distributionConverter.sectorLabel(leader.getSector()))
                .coordinatorId(leader.getCoordinatorId())
                .coordinatorName(coordinatorName)
                .individualLeaderCount(individualLeaderCount)
                .merchantCount(merchantCount)
                .activeSpecialOfferCount(activeOfferCount)
                .totalEarnings(distributionRecordRepository.sumSectorLeaderShareBySectorLeaderId(leader.getId()))
                .status(leader.getStatus())
                .build();
    }
}
