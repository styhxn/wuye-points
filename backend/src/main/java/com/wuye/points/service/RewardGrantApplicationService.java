package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.dto.rewardgrant.AuditRewardGrantRequest;
import com.wuye.points.dto.rewardgrant.RewardGrantItemResponse;
import com.wuye.points.dto.rewardgrant.SubmitRewardGrantRequest;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.RewardGrantApplication;
import com.wuye.points.exception.BizException;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.RewardGrantApplicationRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 奖励金/积分发放申请工作流
 * - 申请人(resident/merchant)提交申请
 * - 物业(property_admin)审批，审批通过才发放积分
 * - 两种归属模式：property(从物业积分池扣减) / platform(由平台资金发放，不扣物业池)
 */
@Service
@RequiredArgsConstructor
public class RewardGrantApplicationService {

    private static final Set<String> VALID_MODES = Set.of("property", "resident");
    private static final Set<String> VALID_GRANT_TYPES = Set.of("points", "reward");
    private static final Set<String> VALID_ACTIONS = Set.of("approve", "reject");

    private final RewardGrantApplicationRepository rewardGrantApplicationRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PointService pointService;
    private final AdminPointPoolService adminPointPoolService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public RewardGrantItemResponse submit(SubmitRewardGrantRequest request) {
        roleGuard.requireAnyRole("resident", "merchant");
        if (!VALID_GRANT_TYPES.contains(request.getGrantType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "grantType 必须为 points 或 reward");
        }
        Resident target = residentRepository.findById(request.getTargetResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        String applicantId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();

        RewardGrantApplication application = new RewardGrantApplication();
        application.setId(idGenerator.generateId("rga"));
        application.setApplicantId(applicantId);
        application.setApplicantRole(UserContext.currentRole());
        application.setTargetResidentId(target.getId());
        application.setTargetResidentName(target.getName());
        application.setGrantType(request.getGrantType());
        application.setPointAmount(request.getPointAmount());
        // 归属模式以后台按物业公司设置为准，不由申请人选择
        application.setAttributionMode(resolveAttributionMode(propertyCompanyId, request.getGrantType()));
        application.setReason(request.getReason());
        application.setAuditStatus("pending_audit");
        application.setPropertyCompanyId(propertyCompanyId);
        return toItemResponse(rewardGrantApplicationRepository.save(application));
    }

    /**
     * 后台按物业公司设置的归属开关：积分/奖励金分别可配置
     * property=到物业公司账号 / resident=业主自己使用（钱到平台，需物业同意后发放）。
     */
    private String resolveAttributionMode(String propertyCompanyId, String grantType) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return "property";
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(pc -> "reward".equals(grantType) ? pc.getRewardAttributionMode() : pc.getPointsAttributionMode())
                .filter(StringUtils::hasText)
                .orElse("property");
    }

    @Transactional
    public RewardGrantItemResponse audit(String id, AuditRewardGrantRequest request) {
        roleGuard.requireAnyRole("property_admin");
        if (!VALID_ACTIONS.contains(request.getAction())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "action 必须为 approve 或 reject");
        }
        RewardGrantApplication application = rewardGrantApplicationRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.REWARD_GRANT_NOT_FOUND, "申请不存在"));
        if (!"pending_audit".equals(application.getAuditStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该申请已审批，不可重复操作");
        }
        if (!"approve".equals(request.getAction())
                && !StringUtils.hasText(request.getRemark())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "拒绝时必须填写备注");
        }

        String approverId = UserContext.currentUserId();
        application.setApproverId(approverId);
        application.setApproverRemark(request.getRemark());
        application.setAuditedAt(LocalDateTime.now());

        if ("approve".equals(request.getAction())) {
            Resident target = residentRepository.findById(application.getTargetResidentId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
            pointService.earnPoints(
                    target,
                    application.getPointAmount(),
                    "reward_grant",
                    application.getId(),
                    "奖励金发放(" + application.getAttributionMode() + "): " + application.getReason(),
                    approverId);
            if ("property".equals(application.getAttributionMode())) {
                adminPointPoolService.deductFromPool(
                        target.getPropertyCompanyId(),
                        BigDecimal.valueOf(application.getPointAmount()),
                        "spend",
                        application.getId(),
                        "奖励金发放扣减物业积分池",
                        approverId);
            }
            application.setAuditStatus("approved");
        } else {
            application.setAuditStatus("rejected");
        }
        return toItemResponse(rewardGrantApplicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public PageResponse<RewardGrantItemResponse> listMy(int page, int pageSize) {
        roleGuard.requireAnyRole("resident", "merchant");
        Pageable pageable = buildPageable(page, pageSize);
        Page<RewardGrantApplication> result =
                rewardGrantApplicationRepository.findByApplicantId(UserContext.currentUserId(), pageable);
        return PageResponse.of(result.getContent().stream().map(this::toItemResponse).toList(),
                page, pageSize, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<RewardGrantItemResponse> listAdmin(
            int page, int pageSize, String status, String targetResidentId) {
        // 2026-09-07 前端问题 D：平台管理员需可查看审批列表（仅读，审批仍限物业）
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        Pageable pageable = buildPageable(page, pageSize);
        Page<RewardGrantApplication> result = StringUtils.hasText(status)
                ? rewardGrantApplicationRepository.findByPropertyCompanyIdAndAuditStatus(propertyCompanyId, status, pageable)
                : rewardGrantApplicationRepository.findByPropertyCompanyId(propertyCompanyId, pageable);
        List<RewardGrantItemResponse> list = result.getContent().stream()
                .filter(a -> !StringUtils.hasText(targetResidentId)
                        || targetResidentId.equals(a.getTargetResidentId()))
                .map(this::toItemResponse)
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    private Pageable buildPageable(int page, int pageSize) {
        int size = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private RewardGrantItemResponse toItemResponse(RewardGrantApplication a) {
        return RewardGrantItemResponse.builder()
                .id(a.getId())
                .applicantId(a.getApplicantId())
                .applicantRole(a.getApplicantRole())
                .targetResidentId(a.getTargetResidentId())
                .targetResidentName(a.getTargetResidentName())
                .grantType(a.getGrantType())
                .pointAmount(a.getPointAmount())
                .attributionMode(a.getAttributionMode())
                .reason(a.getReason())
                .auditStatus(a.getAuditStatus())
                .approverId(a.getApproverId())
                .approverRemark(a.getApproverRemark())
                .auditedAt(a.getAuditedAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
