package com.wuye.points.util;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
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
}
