package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.individualleader.ApplyIndividualLeaderRequest;
import com.wuye.points.dto.individualleader.ApplyIndividualLeaderResponse;
import com.wuye.points.entity.IndividualLeaderApplication;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.IndividualLeaderApplicationRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SectorCatalog;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 住户自主申请个体负责人（一级代理）。
 * 审核通过后由管理端创建 IndividualLeader 记录并变更角色。
 */
@Service
@RequiredArgsConstructor
public class IndividualLeaderApplyService {

    private final IndividualLeaderApplicationRepository applicationRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 住户提交个体负责人申请。
     * 每位住户在每家物业公司下仅可有一个 pending 状态的申请。
     */
    @Transactional
    public ApplyIndividualLeaderResponse apply(ApplyIndividualLeaderRequest request) {
        roleGuard.requireAnyRole("resident");

        // 2026-09-08：板块入参归一化（支持中文）+ 统一字典校验
        String sector = SectorCatalog.normalizeOrThrow(request.getSector());

        Resident resident = getActiveResident(UserContext.currentUserId());

        // 校验是否已有 pending 申请
        boolean hasPending = applicationRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
                        resident.getId(), resident.getPropertyCompanyId(), "pending")
                .isPresent();
        if (hasPending) {
            throw new BizException(ErrorCode.PARAM_INVALID, "您已有待审核的申请，请勿重复提交");
        }

        // 校验是否已是个体负责人
        if ("individual_leader".equals(resident.getRole())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "您已是个体负责人，无需重复申请");
        }

        IndividualLeaderApplication application = new IndividualLeaderApplication();
        application.setId(idGenerator.generateId("ila"));
        application.setResidentId(resident.getId());
        application.setPropertyCompanyId(resident.getPropertyCompanyId());
        application.setSector(sector);
        application.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        application.setAuditStatus("pending");
        applicationRepository.save(application);

        return ApplyIndividualLeaderResponse.builder()
                .applicationId(application.getId())
                .status(application.getAuditStatus())
                .sector(application.getSector())
                .message("申请已提交，等待物业审核")
                .build();
    }

    /**
     * 住户查询自己的最新申请状态
     */
    @Transactional(readOnly = true)
    public ApplyIndividualLeaderResponse getMyLatestApplication() {
        roleGuard.requireAnyRole("resident");

        Resident resident = getActiveResident(UserContext.currentUserId());
        IndividualLeaderApplication application = applicationRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
                        resident.getId(), resident.getPropertyCompanyId(), null)
                .orElse(null);

        if (application == null) {
            return ApplyIndividualLeaderResponse.builder()
                    .applicationId(null)
                    .status("none")
                    .sector(null)
                    .message("暂无申请记录")
                    .build();
        }

        return ApplyIndividualLeaderResponse.builder()
                .applicationId(application.getId())
                .status(application.getAuditStatus())
                .sector(application.getSector())
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
