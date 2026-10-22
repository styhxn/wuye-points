package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.buildingleader.BuildingLeaderMyResponse;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 楼长工作台：本人信息 + 分成收益概览。
 */
@Service
@RequiredArgsConstructor
public class BuildingLeaderWorkbenchService {

    private static final String WITHDRAWAL_TYPE_BUILDING_LEADER = "building_leader";

    private final BuildingLeaderRepository buildingLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final CommunityRepository communityRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public BuildingLeaderMyResponse getMyInfo() {
        roleGuard.requireAnyRole("resident");
        BuildingLeader leader = buildingLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        UserContext.currentUserId(), UserContext.currentPropertyCompanyId(), "active")
                .orElseThrow(() -> new BizException(ErrorCode.BUILDING_LEADER_NOT_FOUND, "当前用户不是楼长"));

        BigDecimal totalEarning = defaultDecimal(distributionRecordRepository
                .sumBuildingLeaderShareByBuildingLeaderId(leader.getId()));
        BigDecimal withdrawn = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(leader.getId(), WITHDRAWAL_TYPE_BUILDING_LEADER, "completed"));
        BigDecimal pending = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(leader.getId(), WITHDRAWAL_TYPE_BUILDING_LEADER, "pending"))
                .add(defaultDecimal(withdrawalRecordRepository
                        .sumAmountByApplicant(leader.getId(), WITHDRAWAL_TYPE_BUILDING_LEADER, "approved")));
        BigDecimal withdrawable = totalEarning.subtract(withdrawn).subtract(pending).max(BigDecimal.ZERO);

        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        Community community = leader.getCommunityId() != null
                ? communityRepository.findById(leader.getCommunityId()).orElse(null) : null;

        return BuildingLeaderMyResponse.builder()
                .buildingLeaderId(leader.getId())
                .residentId(leader.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .propertyCompanyId(leader.getPropertyCompanyId())
                .communityId(leader.getCommunityId())
                .communityName(community != null ? community.getName() : null)
                .unit(leader.getUnit())
                .commissionRate(leader.getCommissionRate())
                .status(leader.getStatus())
                .totalEarning(totalEarning)
                .withdrawnAmount(withdrawn)
                .pendingAmount(pending)
                .withdrawableAmount(withdrawable)
                .build();
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}