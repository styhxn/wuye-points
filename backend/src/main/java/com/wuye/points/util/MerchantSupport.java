package com.wuye.points.util;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.service.MerchantBindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 商家端当前登录用户与商家实体解析
 */
@Component
@RequiredArgsConstructor
public class MerchantSupport {

    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final MerchantBindingService merchantBindingService;
    private final RoleGuard roleGuard;

    public Resident requireCurrentMerchantUser() {
        roleGuard.requireAnyRole("merchant");
        return residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
    }

    public Merchant requireCurrentMerchant() {
        Resident merchantUser = requireCurrentMerchantUser();
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }

        Merchant merchant = merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        if (!"approved".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_AUDITING);
        }
        if (!"active".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchant;
    }

    /**
     * 提现场景当前商家解析（merchant / activity_leader / technician）。
     *
     * <p>按文档 §23：组长（activity_leader）自动定位 {@code merchant_source=group_leader} 店铺、
     * 技工（technician）自动定位 {@code merchant_source=technician} 店铺（缺失时自动绑定，
     * 保证技工端「我的服务」提现列表/申请不会因店铺缺失而失败）；
     * 商家（merchant）沿用手机号 + 物业匹配原口径。
     * 审核/启停状态校验与 {@link #requireCurrentMerchant()} 一致。
     */
    public Merchant requireCurrentWithdrawableMerchant() {
        String role = roleGuard.normalizeRole(UserContext.currentRole());
        if (!"merchant".equals(role) && !"activity_leader".equals(role) && !"technician".equals(role)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        Resident merchantUser = requireCurrentUser();
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }

        Merchant merchant = resolveWithdrawalMerchant(merchantUser, role);
        if (!"approved".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_AUDITING);
        }
        if (!"active".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchant;
    }

    private Resident requireCurrentUser() {
        return residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
    }

    private Merchant resolveWithdrawalMerchant(Resident user, String role) {
        if ("technician".equals(role)) {
            return merchantRepository
                    .findByContactPhoneAndPropertyCompanyIdAndMerchantSource(
                            user.getPhone(), user.getPropertyCompanyId(), "technician")
                    .orElseGet(() -> merchantBindingService.ensureMerchant(user.getId(), "technician"));
        }
        if ("activity_leader".equals(role)) {
            return merchantRepository
                    .findByContactPhoneAndPropertyCompanyIdAndMerchantSource(
                            user.getPhone(), user.getPropertyCompanyId(), "group_leader")
                    .orElseGet(() -> merchantBindingService.ensureMerchant(user.getId(), "group_leader"));
        }
        return merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        user.getPhone(), user.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }
}
