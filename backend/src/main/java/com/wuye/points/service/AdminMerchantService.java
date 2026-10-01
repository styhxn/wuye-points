package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.merchant.AuditMerchantRegistrationRequest;
import com.wuye.points.dto.admin.merchant.AuditMerchantRegistrationResponse;
import com.wuye.points.dto.admin.merchant.KickMerchantRequest;
import com.wuye.points.dto.admin.merchant.KickMerchantResponse;
import com.wuye.points.dto.admin.merchant.SetOfficialRecommendationRequest;
import com.wuye.points.dto.merchant.ApplymentDetailResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantApplyment;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantApplymentRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminMerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantApplymentRepository applymentRepository;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;
    private final EcommerceApplymentService applymentService;

    @Transactional
    public KickMerchantResponse kickMerchant(String merchantId, KickMerchantRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        if (!roleGuard.isPlatformAdmin()) {
            String propertyCompanyId = adminSupport.resolvePropertyCompanyId(null);
            if (!merchant.getPropertyCompanyId().equals(propertyCompanyId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        }

        if ("kicked".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_ALREADY_KICKED);
        }

        merchant.setStatus("kicked");
        merchantRepository.save(merchant);

        return KickMerchantResponse.builder()
                .id(merchant.getId())
                .status(merchant.getStatus())
                .reason(request.getReason())
                .kickedAt(DateTimeUtil.toIsoUtc(LocalDateTime.now()))
                .build();
    }

    @Transactional
    public AuditMerchantRegistrationResponse auditMerchantRegistration(
            String merchantId, AuditMerchantRegistrationRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        if (!roleGuard.isPlatformAdmin()) {
            String propertyCompanyId = adminSupport.resolvePropertyCompanyId(null);
            if (!merchant.getPropertyCompanyId().equals(propertyCompanyId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        }

        if (!"pending_audit".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
        }

        String auditResult = request.getAuditResult();
        if (!"approved".equals(auditResult) && !"rejected".equals(auditResult)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "审核结果必须为approved或rejected");
        }

        merchant.setAuditStatus(auditResult);

        if ("approved".equals(auditResult)) {
            merchant.setStatus("active");
            if (StringUtils.hasText(request.getMerchantLevel())) {
                merchant.setMerchantLevel(request.getMerchantLevel());
            }
            if (StringUtils.hasText(request.getCategory())) {
                merchant.setCategory(request.getCategory());
            }
            if (StringUtils.hasText(request.getBusinessHours())) {
                merchant.setBusinessHours(request.getBusinessHours());
            }
            if (StringUtils.hasText(request.getDeliveryFee())) {
                merchant.setDeliveryFee(new BigDecimal(request.getDeliveryFee()));
            }
            if (StringUtils.hasText(request.getFreeDeliveryThreshold())) {
                merchant.setFreeDeliveryThreshold(new BigDecimal(request.getFreeDeliveryThreshold()));
            }
        } else {
            merchant.setStatus("rejected");
        }

        merchantRepository.save(merchant);

        String operatorId = UserContext.currentUserId();

        return AuditMerchantRegistrationResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .auditStatus(merchant.getAuditStatus())
                .auditResult(auditResult)
                .rejectReason(request.getRejectReason())
                .merchantLevel(merchant.getMerchantLevel())
                .category(merchant.getCategory())
                .operatorId(operatorId)
                .operatorName(null)
                .auditedAt(DateTimeUtil.toIsoUtc(LocalDateTime.now()))
                .build();
    }

    // ---------------------------------------------------------------------
    // 微信进件（电商收付通阶段1）
    // ---------------------------------------------------------------------

    /** 查看商家进件资料与状态（运营后台）。 */
    @Transactional(readOnly = true)
    public ApplymentDetailResponse getApplyment(String merchantId) {
        requireAnyAdminRole();
        requireMerchantInScope(merchantId);
        return applymentService.getApplyment(merchantId);
    }

    /** 手动触发商家进件提交（运营后台；默认进件触发方式）。 */
    @Transactional
    public ApplymentDetailResponse submitApplyment(String merchantId) {
        requireAnyAdminRole();
        requireMerchantInScope(merchantId);
        return applymentService.submitApplyment(merchantId);
    }

    /** 手动查询微信侧进件状态并推进本地状态机（运营后台）。 */
    @Transactional
    public ApplymentDetailResponse refreshApplyment(String merchantId) {
        requireAnyAdminRole();
        requireMerchantInScope(merchantId);
        MerchantApplyment applyment = applymentRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.APPLYMENT_NOT_FOUND));
        applymentService.refreshApplymentStatus(applyment);
        return applymentService.getApplyment(merchantId);
    }

    /** 设置/取消商家官方推荐（运营后台）。 */
    @Transactional
    public void setRecommendation(String merchantId, SetOfficialRecommendationRequest request) {
        requireAnyAdminRole();
        Merchant merchant = requireMerchantInScope(merchantId);
        merchant.setIsRecommended(Boolean.TRUE.equals(request.getIsRecommended()));
        merchant.setRecommendedSort(request.getRecommendedSort());
        merchantRepository.save(merchant);
    }

    private void requireAnyAdminRole() {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
    }

    private Merchant requireMerchantInScope(String merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!roleGuard.isPlatformAdmin()) {
            String propertyCompanyId = adminSupport.resolvePropertyCompanyId(null);
            if (!merchant.getPropertyCompanyId().equals(propertyCompanyId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        }
        return merchant;
    }
}
