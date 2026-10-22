package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.activityleader.ApplyActivityLeaderRequest;
import com.wuye.points.dto.activityleader.ApplyActivityLeaderResponse;
import com.wuye.points.entity.ActivityLeaderApplication;
import com.wuye.points.entity.ActivityLeaderIdentity;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ActivityLeaderApplicationRepository;
import com.wuye.points.repository.ActivityLeaderIdentityRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 住户自主申请成为活动组组长（C 端）。
 *
 * <p>与个体负责人申请、一级代理申请同模式：每位住户在同一物业下仅可有一条待审核申请；
 * 已是组长（active 身份记录）则拒绝。审核由管理端 {@link AdminActivityLeaderApplicationService} 处理，
 * 通过后创建组长身份（标签，role 仍为 resident）并自动开通组长小店。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLeaderApplyService {

    private final ActivityLeaderApplicationRepository applicationRepository;
    private final ActivityLeaderIdentityRepository identityRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 提交活动组组长申请。
     */
    @Transactional
    public ApplyActivityLeaderResponse apply(ApplyActivityLeaderRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(resident.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "请先绑定所属物业公司");
        }

        if (identityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                resident.getId(), resident.getPropertyCompanyId(), "active").isPresent()) {
            throw new BizException(ErrorCode.ACTIVITY_LEADER_APPLICATION_PENDING_EXISTS,
                    "您已是活动组组长，无需重复申请");
        }
        if (applicationRepository.findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
                resident.getId(), resident.getPropertyCompanyId(), "pending").isPresent()) {
            throw new BizException(ErrorCode.ACTIVITY_LEADER_APPLICATION_PENDING_EXISTS,
                    "您已有待审核的申请，请勿重复提交");
        }

        ActivityLeaderApplication application = new ActivityLeaderApplication();
        application.setId(idGenerator.generateId("ala"));
        application.setResidentId(resident.getId());
        application.setPropertyCompanyId(resident.getPropertyCompanyId());
        application.setCommunityId(resident.getCommunityId());
        application.setActivityField(StringUtils.hasText(request.getActivityField())
                ? request.getActivityField().trim() : null);
        application.setRemark(StringUtils.hasText(request.getRemark())
                ? request.getRemark().trim() : null);
        application.setAuditStatus("pending");
        applicationRepository.save(application);

        log.info("[组长申请] 已提交: applicationId={}, residentId={}", application.getId(), resident.getId());
        return ApplyActivityLeaderResponse.builder()
                .applicationId(application.getId())
                .status("pending")
                .activityField(application.getActivityField())
                .remark(application.getRemark())
                .message("申请已提交，等待物业审核")
                .build();
    }

    /**
     * 查询我的最新申请状态。
     */
    @Transactional(readOnly = true)
    public ApplyActivityLeaderResponse getMyLatestApplication() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityLeaderApplication application = applicationRepository
                .findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(
                        resident.getId(), resident.getPropertyCompanyId())
                .orElse(null);

        if (application == null) {
            return ApplyActivityLeaderResponse.builder()
                    .applicationId(null)
                    .status("none")
                    .message("暂无申请记录")
                    .build();
        }

        return ApplyActivityLeaderResponse.builder()
                .applicationId(application.getId())
                .status(application.getAuditStatus())
                .activityField(application.getActivityField())
                .remark(application.getRemark())
                .rejectReason(application.getRejectReason())
                .message(resolveStatusMessage(application.getAuditStatus()))
                .build();
    }

    private String resolveStatusMessage(String auditStatus) {
        return switch (auditStatus) {
            case "pending" -> "申请审核中";
            case "approved" -> "申请已通过";
            case "rejected" -> "申请未通过";
            default -> "未知状态";
        };
    }

    private Resident getActiveResident(String userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!"active".equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }
}
