package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.distributorapplication.AuditDistributorApplicationRequest;
import com.wuye.points.dto.admin.distributorapplication.DistributorApplicationAuditResponse;
import com.wuye.points.dto.admin.distributorapplication.DistributorApplicationListItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.DistributorApplication;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PlatformMerchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DistributorApplicationRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 一级代理申请审核（管理端）。
 *
 * <p>审核通过（approved）时在同一事务内完成载体闭环（R5 方案A）：创建/关联 merchants 载体
 * → 置 is_distributor=1 → audit_status=approved，避免依赖平台运营手工建载体。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDistributorApplicationService {

    private static final String MERCHANT_SOURCE = "distributor";
    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");

    private final DistributorApplicationRepository applicationRepository;
    private final ResidentRepository residentRepository;
    private final MerchantRepository merchantRepository;
    private final PlatformMerchantRepository platformMerchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final PlatformSettingService platformSettingService;
    private final AdminSupport adminSupport;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 一级代理申请列表（按物业隔离；platform_admin 可跨物业）。
     */
    @Transactional(readOnly = true)
    public PageResponse<DistributorApplicationListItemResponse> listApplications(
            int page, int pageSize, String auditStatus, String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String scoped = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");

        Specification<DistributorApplication> spec = (root, query, cb) -> {
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

        Page<DistributorApplication> result = applicationRepository.findAll(spec, pageable);
        List<DistributorApplicationListItemResponse> items = result.getContent().stream()
                .map(this::toListItem)
                .toList();
        return PageResponse.of(items, page, pageSize, result.getTotalElements());
    }

    /**
     * 审核一级代理申请。approved 时同事务创建/关联商家载体并开通一级代理。
     */
    @Transactional
    public DistributorApplicationAuditResponse auditApplication(
            String applicationId, AuditDistributorApplicationRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(applicationId, "dap_");

        DistributorApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BizException(ErrorCode.DISTRIBUTOR_APPLICATION_NOT_FOUND));
        adminSupport.assertSameProperty(application.getPropertyCompanyId());

        if (!"pending".equals(application.getAuditStatus())) {
            throw new BizException(ErrorCode.DISTRIBUTOR_APPLICATION_ALREADY_AUDITED,
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
            return toAuditResponse(application, null);
        }

        Resident resident = residentRepository.findById(application.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());
        if (!"active".equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED, "该住户账号已停用，无法通过申请");
        }

        Merchant merchant = ensureDistributorMerchant(application, resident);

        application.setAuditStatus("approved");
        application.setDistributorMerchantId(merchant.getId());
        applicationRepository.save(application);
        return toAuditResponse(application, merchant.getId());
    }

    /**
     * 确保申请人名下有 is_distributor=1 且审核通过的商家载体（幂等）：
     * 已有则复用；否则三表联建（platform_merchants + merchants + merchant_property_configs）。
     */
    private Merchant ensureDistributorMerchant(DistributorApplication application, Resident resident) {
        Optional<Merchant> existing = merchantRepository.findByResidentId(resident.getId()).stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsDistributor())
                        && "approved".equals(m.getAuditStatus()))
                .findFirst();
        if (existing.isPresent()) {
            Merchant merchant = existing.get();
            if (!"active".equals(merchant.getStatus())) {
                merchant.setStatus("active");
                merchantRepository.save(merchant);
            }
            return merchant;
        }

        String shopName = application.getContactName() + "的一级代理";

        PlatformMerchant platformMerchant = new PlatformMerchant();
        platformMerchant.setId(idGenerator.generateId("pmm"));
        platformMerchant.setName(shopName);
        platformMerchant.setContactPhone(application.getContactPhone());
        platformMerchant.setStatus("active");
        platformMerchantRepository.save(platformMerchant);

        Merchant merchant = new Merchant();
        merchant.setId(idGenerator.generateId("mch"));
        merchant.setPlatformMerchantId(platformMerchant.getId());
        merchant.setPropertyCompanyId(application.getPropertyCompanyId());
        merchant.setName(shopName);
        merchant.setMerchantLevel("normal");
        merchant.setLevelWeight(0);
        merchant.setCategory("一级代理");
        merchant.setAuditStatus("approved");
        merchant.setRankOrder(0);
        merchant.setContactPhone(application.getContactPhone());
        merchant.setResidentId(resident.getId());
        merchant.setDeliveryFee(BigDecimal.ZERO);
        merchant.setDeliveryScope("both");
        merchant.setStatus("active");
        merchant.setServeAllCommunities(true);
        // 显式置 merchant_type=goods（DB 为 NOT NULL DEFAULT 'goods'，避免依赖非严格 SQL 模式兜底）
        merchant.setMerchantType("goods");
        merchant.setMerchantSource(MERCHANT_SOURCE);
        merchant.setIsDistributor(true);
        merchantRepository.save(merchant);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(idGenerator.generateId("mpc"));
        config.setMerchantId(merchant.getId());
        config.setPropertyCompanyId(merchant.getPropertyCompanyId());
        config.setCommissionRate(platformSettingService.getResidentMerchantDistributorCommissionRate());
        config.setPointExchangeRate(DEFAULT_POINT_EXCHANGE_RATE);
        config.setCoinRebateRate(BigDecimal.ZERO);
        config.setCoinRebateEnabled(false);
        config.setStatus("active");
        merchantPropertyConfigRepository.save(config);

        log.info("一级代理商家载体已创建: applicationId={}, merchantId={}, residentId={}",
                application.getId(), merchant.getId(), resident.getId());
        return merchant;
    }

    private DistributorApplicationListItemResponse toListItem(DistributorApplication a) {
        return DistributorApplicationListItemResponse.builder()
                .id(a.getId())
                .residentId(a.getResidentId())
                .contactName(a.getContactName())
                .contactPhone(a.getContactPhone())
                .businessScope(a.getBusinessScope())
                .serviceScope(a.getServiceScope())
                .remark(a.getRemark())
                .auditStatus(a.getAuditStatus())
                .rejectReason(a.getRejectReason())
                .distributorMerchantId(a.getDistributorMerchantId())
                .auditedAt(DateTimeUtil.toIsoUtc(a.getAuditedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(a.getCreatedAt()))
                .propertyCompanyId(a.getPropertyCompanyId())
                .build();
    }

    private DistributorApplicationAuditResponse toAuditResponse(DistributorApplication a, String merchantId) {
        return DistributorApplicationAuditResponse.builder()
                .applicationId(a.getId())
                .auditStatus(a.getAuditStatus())
                .residentId(a.getResidentId())
                .contactName(a.getContactName())
                .businessScope(a.getBusinessScope())
                .serviceScope(a.getServiceScope())
                .rejectReason(a.getRejectReason())
                .auditedAt(DateTimeUtil.toIsoUtc(a.getAuditedAt()))
                .distributorMerchantId(merchantId != null ? merchantId : a.getDistributorMerchantId())
                .build();
    }
}
