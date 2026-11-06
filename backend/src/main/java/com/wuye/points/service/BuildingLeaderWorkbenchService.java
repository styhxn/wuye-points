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
import com.wuye.points.repository.ResidentRepository;
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

    private final BuildingLeaderRepository buildingLeaderRepository;
    private final CommunityRepository communityRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final UnifiedEarningsService unifiedEarningsService;
    private final RoleWithdrawableService roleWithdrawableService;

    @Transactional(readOnly = true)
    public BuildingLeaderMyResponse getMyInfo() {
        roleGuard.requireAnyRole("resident");
        BuildingLeader leader = buildingLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        UserContext.currentUserId(), UserContext.currentPropertyCompanyId(), "active")
                .orElseThrow(() -> new BizException(ErrorCode.BUILDING_LEADER_NOT_FOUND, "当前用户不是楼长"));

        // v8.21：楼长收益并入账号统一收益账户（楼长分成 + 业主卖货 + 名下商家），不再单列
        UnifiedEarningsService.UnifiedEarnings unified =
                unifiedEarningsService.calc(leader.getResidentId());
        RoleWithdrawableService.RoleBalance balance = roleWithdrawableService.forBuildingLeader(
                leader.getId(), leader.getResidentId(), leader.getPropertyCompanyId(), unified);
        BigDecimal totalEarning = unified.totalEarning();
        BigDecimal withdrawn = unified.withdrawnAmount();

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
                .pendingAmount(balance.pendingWithdrawalAmount())
                .withdrawableAmount(balance.withdrawableAmount())
                .feeRate(balance.feeRate())
                .withdrawFeeBearer(RoleWithdrawableService.FEE_BEARER_PAYEE)
                .build();
    }
}