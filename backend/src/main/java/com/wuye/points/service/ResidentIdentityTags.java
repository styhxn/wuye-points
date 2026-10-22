package com.wuye.points.service;

import com.wuye.points.entity.ActivityLeaderIdentity;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantApplication;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentMerchantApplicationRepository;
import com.wuye.points.util.LeaderIdentityGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 住户身份标签解析（供「我的」页四图标路由使用）。
 *
 * <p>统一产出三类住户子身份标签，避免各调用方重复查询：
 * <ul>
 *   <li>业主小店：{@code resident_merchant_applications} 营业中（active）记录</li>
 *   <li>活动组组长：{@code activity_leader_identities} active 记录（2026-10-21 标签化）</li>
 *   <li>一级代理：名下 {@code is_distributor=1} 且审核通过的商家载体</li>
 * </ul>
 * 与楼长/统筹/板块/个体负责人标签同模式：role 恒为 resident，身份按业务表判定。
 */
@Component
@RequiredArgsConstructor
public class ResidentIdentityTags {

    private final ResidentMerchantApplicationRepository residentMerchantApplicationRepository;
    private final MerchantRepository merchantRepository;
    private final LeaderIdentityGuard leaderIdentityGuard;

    /** 营业中的业主小店申请记录（非营业中/未申请均为空） */
    public Optional<ResidentMerchantApplication> activeResidentMerchant(String residentId) {
        if (residentId == null) {
            return Optional.empty();
        }
        return residentMerchantApplicationRepository.findByResidentId(residentId)
                .filter(a -> "active".equals(a.getStatus()));
    }

    public boolean isResidentMerchant(String residentId) {
        return activeResidentMerchant(residentId).isPresent();
    }

    /** 启用中的活动组组长身份 */
    public Optional<ActivityLeaderIdentity> activeActivityLeader(Resident resident) {
        if (resident == null) {
            return Optional.empty();
        }
        return leaderIdentityGuard.activeActivityLeader(resident.getId(), resident.getPropertyCompanyId());
    }

    public boolean isActivityLeader(Resident resident) {
        return activeActivityLeader(resident).isPresent();
    }

    /** 名下是否已有 is_distributor=1 且审核通过的商家载体 */
    public Optional<Merchant> enabledDistributor(String residentId) {
        if (residentId == null) {
            return Optional.empty();
        }
        return merchantRepository.findByResidentId(residentId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsDistributor())
                        && "approved".equals(m.getAuditStatus()))
                .findFirst();
    }

    public boolean isDistributor(String residentId) {
        return enabledDistributor(residentId).isPresent();
    }
}
