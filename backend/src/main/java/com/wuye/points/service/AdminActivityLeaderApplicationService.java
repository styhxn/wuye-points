package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.activityleaderapplication.ActivityLeaderApplicationAuditResponse;
import com.wuye.points.dto.admin.activityleaderapplication.ActivityLeaderApplicationListItemResponse;
import com.wuye.points.dto.admin.activityleaderapplication.AuditActivityLeaderApplicationRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.ActivityLeaderApplication;
import com.wuye.points.entity.ActivityLeaderIdentity;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ActivityLeaderApplicationRepository;
import com.wuye.points.repository.ActivityLeaderIdentityRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.service.MerchantBindingService;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动组组长申请审核（管理端）。
 *
 * <p>审核通过（approved）时在同一事务内完成：创建组长身份记录（标签，role 仍为 resident）
 * → 自动开通组长小店（merchant_source=group_leader）。与一级代理申请审核同模式。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActivityLeaderApplicationService {

    private final ActivityLeaderApplicationRepository applicationRepository;
    private final ActivityLeaderIdentityRepository identityRepository;
    private final ResidentRepository residentRepository;
    private final MerchantBindingService merchantBindingService;
    private final AdminSupport adminSupport;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 组长申请列表（按物业隔离；platform_admin 可跨物业）。
     */
    @Transactional(readOnly = true)
    public PageResponse<ActivityLeaderApplicationListItemResponse> listApplications(
            int page, int pageSize, String auditStatus, String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String scoped = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");

        Specification<ActivityLeaderApplication> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // platform_admin 不传 propertyCompanyId 时查看全部物业
            if (StringUtils.hasText(scoped)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), scoped));
            }
            if (StringUtils.hasText(auditStatus)) {
                predicates.add(cb.equal(root.get("auditStatus"), auditStatus));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ActivityLeaderApplication> result = applicationRepository.findAll(spec, pageable);
        Map<String, Resident> residentMap = result.getContent().isEmpty() ? Map.of()
                : residentRepository.findAllById(result.getContent().stream()
                        .map(ActivityLeaderApplication::getResidentId).distinct().toList())
                .stream().collect(Collectors.toMap(Resident::getId, r -> r, (a, b) -> a));
        List<ActivityLeaderApplicationListItemResponse> items = result.getContent().stream()
                .map(a -> toListItem(a, residentMap.get(a.getResidentId())))
                .toList();
        return PageResponse.of(items, page, pageSize, result.getTotalElements());
    }

    /**
     * 审核组长申请。approved 时同事务创建组长身份 + 开通组长小店。
     */
    @Transactional
    public ActivityLeaderApplicationAuditResponse auditApplication(
            String applicationId, AuditActivityLeaderApplicationRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(applicationId, "ala_");

        ActivityLeaderApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_LEADER_APPLICATION_NOT_FOUND));
        adminSupport.assertSameProperty(application.getPropertyCompanyId());

        if (!"pending".equals(application.getAuditStatus())) {
            throw new BizException(ErrorCode.ACTIVITY_LEADER_APPLICATION_ALREADY_AUDITED,
                    "该申请已审核（当前状态：" + application.getAuditStatus() + "）");
        }

        application.setAuditedAt(LocalDateTime.now());
        application.setAuditedBy(UserContext.currentUserId());

        if ("rejected".equals(request.getAuditStatus())) {
            if (!StringUtils.hasText(request.getRejectReason())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "rejectReason不能为空");
            }
            application.setAuditStatus("rejected");
            application.setRejectReason(request.getRejectReason().trim());
            applicationRepository.save(application);
            log.info("[组长申请] 已驳回: applicationId={}", applicationId);
            return toAuditResponse(application, null, null);
        }
        if (!"approved".equals(request.getAuditStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus仅支持 approved/rejected");
        }

        Resident resident = residentRepository.findById(application.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        if (!"active".equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED, "该住户账号已停用，无法通过申请");
        }

        // 1) 组长身份（幂等：已有 active 身份则复用，避免重复建记录）
        ActivityLeaderIdentity identity = identityRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        resident.getId(), resident.getPropertyCompanyId(), "active")
                .orElseGet(() -> {
                    ActivityLeaderIdentity created = new ActivityLeaderIdentity();
                    created.setId(idGenerator.generateId("ali"));
                    created.setResidentId(resident.getId());
                    created.setPropertyCompanyId(resident.getPropertyCompanyId());
                    created.setSource("application");
                    created.setApplicationId(application.getId());
                    created.setStatus("active");
                    created.setCreatedBy(UserContext.currentUserId());
                    return identityRepository.save(created);
                });

        // 2) 自动开通组长小店（幂等）
        merchantBindingService.ensureGroupLeaderMerchant(resident.getId());

        application.setAuditStatus("approved");
        application.setActivityLeaderIdentityId(identity.getId());
        applicationRepository.save(application);

        log.info("[组长申请] 已通过: applicationId={}, residentId={}, identityId={}",
                applicationId, resident.getId(), identity.getId());
        return toAuditResponse(application, identity.getId(), resident.getName());
    }

    /**
     * 撤销某住户的组长身份（管理端）：身份记录置 revoked，历史业务数据保留。
     */
    @Transactional
    public ActivityLeaderApplicationAuditResponse revokeIdentity(String residentId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(residentId, "res_");

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());

        ActivityLeaderIdentity identity = identityRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        residentId, resident.getPropertyCompanyId(), "active")
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_LEADER_IDENTITY_NOT_FOUND,
                        "该用户没有生效中的组长身份"));

        identity.setStatus("revoked");
        identity.setRevokedBy(UserContext.currentUserId());
        identity.setRevokedAt(LocalDateTime.now());
        identityRepository.save(identity);

        log.info("[组长身份] 已撤销: residentId={}, identityId={}", residentId, identity.getId());
        return ActivityLeaderApplicationAuditResponse.builder()
                .applicationId(identity.getApplicationId())
                .auditStatus("revoked")
                .residentId(residentId)
                .residentName(resident.getName())
                .activityLeaderIdentityId(identity.getId())
                .auditedAt(DateTimeUtil.toIsoUtc(identity.getRevokedAt()))
                .build();
    }

    private ActivityLeaderApplicationListItemResponse toListItem(
            ActivityLeaderApplication a, Resident resident) {
        return ActivityLeaderApplicationListItemResponse.builder()
                .id(a.getId())
                .residentId(a.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .residentPhone(resident != null ? PhoneUtil.mask(resident.getPhone()) : null)
                .activityField(a.getActivityField())
                .remark(a.getRemark())
                .auditStatus(a.getAuditStatus())
                .rejectReason(a.getRejectReason())
                .activityLeaderIdentityId(a.getActivityLeaderIdentityId())
                .auditedAt(DateTimeUtil.toIsoUtc(a.getAuditedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(a.getCreatedAt()))
                .propertyCompanyId(a.getPropertyCompanyId())
                .build();
    }

    private ActivityLeaderApplicationAuditResponse toAuditResponse(
            ActivityLeaderApplication a, String identityId, String residentName) {
        return ActivityLeaderApplicationAuditResponse.builder()
                .applicationId(a.getId())
                .auditStatus(a.getAuditStatus())
                .residentId(a.getResidentId())
                .residentName(residentName)
                .rejectReason(a.getRejectReason())
                .auditedAt(DateTimeUtil.toIsoUtc(a.getAuditedAt()))
                .activityLeaderIdentityId(identityId != null ? identityId : a.getActivityLeaderIdentityId())
                .build();
    }
}
