package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.distributor.ApplyDistributorRequest;
import com.wuye.points.dto.distributor.DistributorApplyResponse;
import com.wuye.points.entity.DistributorApplication;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DistributorApplicationRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 一级代理申请（C端）。
 *
 * <p>一级代理角色已放宽（v8.12）：任意已登录注册用户均可申请。
 * 约束：同一用户在同一物业公司下仅允许一条 pending 申请；已是一级代理（名下有
 * is_distributor=1 且审核通过的商家载体）时拒绝重复申请。
 */
@Service
@RequiredArgsConstructor
public class DistributorApplyService {

    private static final Set<String> BUSINESS_SCOPES = Set.of("fresh", "daily", "grain", "other");
    private static final Set<String> SERVICE_SCOPES = Set.of("community", "city");
    private static final String[] ALL_ROLES = {
            "resident", "merchant", "courier", "technician",
            "coordinator", "sector_leader", "individual_leader", "activity_leader",
            "property_admin", "platform_admin", "community_admin"
    };

    private final DistributorApplicationRepository applicationRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 提交一级代理申请。
     */
    @Transactional
    public DistributorApplyResponse apply(ApplyDistributorRequest request) {
        roleGuard.requireAnyRole(ALL_ROLES);

        Resident resident = getActiveResident(UserContext.currentUserId());
        String propertyCompanyId = resolvePropertyCompanyId(resident);

        String businessScope = normalizeEnum(request.getBusinessScope(), BUSINESS_SCOPES, "经营品类");
        String serviceScope = normalizeEnum(request.getServiceScope(), SERVICE_SCOPES, "服务范围");

        boolean hasPending = applicationRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
                        resident.getId(), propertyCompanyId, "pending")
                .isPresent();
        if (hasPending) {
            throw new BizException(ErrorCode.DISTRIBUTOR_APPLICATION_PENDING_EXISTS);
        }

        if (hasEnabledDistributor(resident.getId())) {
            throw new BizException(ErrorCode.DISTRIBUTOR_ALREADY_ENABLED);
        }

        DistributorApplication application = new DistributorApplication();
        application.setId(idGenerator.generateId("dap"));
        application.setResidentId(resident.getId());
        application.setPropertyCompanyId(propertyCompanyId);
        application.setContactName(request.getContactName().trim());
        application.setContactPhone(request.getContactPhone().trim());
        application.setBusinessScope(businessScope);
        application.setServiceScope(serviceScope);
        application.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        application.setAuditStatus("pending");
        applicationRepository.save(application);

        return DistributorApplyResponse.builder()
                .applicationId(application.getId())
                .status(application.getAuditStatus())
                .contactName(application.getContactName())
                .contactPhone(application.getContactPhone())
                .businessScope(application.getBusinessScope())
                .serviceScope(application.getServiceScope())
                .remark(application.getRemark())
                .submittedAt(DateTimeUtil.toIsoUtc(application.getCreatedAt()))
                .message("申请已提交，等待物业审核")
                .build();
    }

    /**
     * 查询我的最新一级代理申请状态。
     */
    @Transactional(readOnly = true)
    public DistributorApplyResponse getMyApplication() {
        roleGuard.requireAnyRole(ALL_ROLES);

        Resident resident = getActiveResident(UserContext.currentUserId());
        String propertyCompanyId = resolvePropertyCompanyId(resident);
        DistributorApplication application = applicationRepository
                .findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(
                        resident.getId(), propertyCompanyId)
                .orElse(null);

        if (application == null) {
            return DistributorApplyResponse.builder()
                    .status("none")
                    .message("暂无申请记录")
                    .build();
        }

        return DistributorApplyResponse.builder()
                .applicationId(application.getId())
                .status(application.getAuditStatus())
                .contactName(application.getContactName())
                .contactPhone(application.getContactPhone())
                .businessScope(application.getBusinessScope())
                .serviceScope(application.getServiceScope())
                .remark(application.getRemark())
                .rejectReason(application.getRejectReason())
                .distributorMerchantId(application.getDistributorMerchantId())
                .submittedAt(DateTimeUtil.toIsoUtc(application.getCreatedAt()))
                .message(resolveStatusMessage(application.getAuditStatus()))
                .build();
    }

    /** 名下是否已有 is_distributor=1 且审核通过的商家载体 */
    boolean hasEnabledDistributor(String residentId) {
        List<Merchant> merchants = merchantRepository.findByResidentId(residentId);
        return merchants.stream()
                .anyMatch(m -> Boolean.TRUE.equals(m.getIsDistributor())
                        && "approved".equals(m.getAuditStatus()));
    }

    private String resolveStatusMessage(String auditStatus) {
        return switch (auditStatus) {
            case "pending" -> "申请审核中";
            case "approved" -> "申请已通过";
            case "rejected" -> "申请未通过";
            default -> "未知状态";
        };
    }

    private String normalizeEnum(String value, Set<String> allowed, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(ErrorCode.PARAM_MISSING, label + "不能为空");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new BizException(ErrorCode.PARAM_INVALID, label + "取值不合法");
        }
        return normalized;
    }

    private String resolvePropertyCompanyId(Resident resident) {
        if (StringUtils.hasText(resident.getPropertyCompanyId())) {
            return resident.getPropertyCompanyId();
        }
        String fromContext = UserContext.currentPropertyCompanyId();
        if (StringUtils.hasText(fromContext)) {
            return fromContext;
        }
        throw new BizException(ErrorCode.PARAM_MISSING, "无法确定所属物业公司，请联系管理员");
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
