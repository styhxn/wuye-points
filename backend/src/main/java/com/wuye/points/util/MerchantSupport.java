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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 商家端当前登录用户与商家实体解析
 */
@Slf4j
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
        Merchant merchant = requireOwnedMerchant(merchantRepository, merchantUser);

        if (!"approved".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_AUDITING);
        }
        if (!"active".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchant;
    }

    /**
     * 解析某个住户账号名下的店铺（全项目唯一口径）。
     *
     * <p>归属只认 {@code merchants.resident_id}：{@code contact_phone} 是对外展示的联系电话，
     * 管理端建店时可以随手填别人的号，用它当身份会出现「A 的手机号被填进 B 的店，
     * A 登录后进了 B 的商家工作台」（2026-11-05 元序账号被「验收补漏食堂」顶替即此因）。</p>
     *
     * <p>因此顺序是：① 先按 {@code resident_id + 物业公司} 找；② 没找到才按手机号兜底，
     * 且兜底只接受 {@code resident_id} 为空的存量店——手机号命中一个已绑定别人的店，
     * 说明是数据冲突，必须按「非本人」处理，不能放行。</p>
     */
    public static Optional<Merchant> findOwnedMerchant(MerchantRepository merchantRepository, Resident user) {
        if (user == null) {
            return Optional.empty();
        }
        Optional<Merchant> byResident = merchantRepository
                .findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(
                        user.getId(), user.getPropertyCompanyId());
        if (byResident.isPresent()) {
            return byResident;
        }
        if (!StringUtils.hasText(user.getPhone())) {
            return Optional.empty();
        }
        Optional<Merchant> byPhone = merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        user.getPhone(), user.getPropertyCompanyId());
        if (byPhone.isPresent()) {
            String boundResidentId = byPhone.get().getResidentId();
            if (StringUtils.hasText(boundResidentId) && !boundResidentId.equals(user.getId())) {
                log.warn("商家归属冲突：住户 {} 的手机号命中商家 {}，但该店已绑定住户 {}，按非本人处理",
                        user.getId(), byPhone.get().getId(), boundResidentId);
                return Optional.empty();
            }
        }
        return byPhone;
    }

    public static Merchant requireOwnedMerchant(MerchantRepository merchantRepository, Resident user) {
        return findOwnedMerchant(merchantRepository, user)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    /** 判断某个店铺是否属于该住户：有 resident_id 只认它，历史未绑定的店才退回手机号比对。 */
    public static boolean isOwnedBy(Merchant merchant, Resident user) {
        if (merchant == null || user == null) {
            return false;
        }
        String userPc = user.getPropertyCompanyId();
        String merchantPc = merchant.getPropertyCompanyId();
        if (userPc == null || merchantPc == null || !userPc.equals(merchantPc)) {
            return false;
        }
        if (StringUtils.hasText(merchant.getResidentId())) {
            return merchant.getResidentId().equals(user.getId());
        }
        if (!StringUtils.hasText(user.getPhone()) || !StringUtils.hasText(merchant.getContactPhone())) {
            return false;
        }
        return user.getPhone().equals(merchant.getContactPhone());
    }

    /**
     * 提现场景当前商家解析（merchant / activity_leader / technician）。
     *
     * <p>按文档 §23：组长（activity_leader）自动定位 {@code merchant_source=group_leader} 店铺、
     * 技工（technician）自动定位 {@code merchant_source=technician} 店铺（缺失时自动绑定，
     * 保证技工端「我的服务」提现列表/申请不会因店铺缺失而失败）；
     * 商家（merchant）按 {@link #findOwnedMerchant} 口径（先 resident_id，再未绑定店的手机号兜底）。
     * 审核/启停状态校验与 {@link #requireCurrentMerchant()} 一致。
     *
     * <p>2026-10-21 组长标签化：组长 role 归一为 resident，身份按
     * {@code activity_leader_identities} 表 active 记录判定（兼容存量 JWT role=activity_leader）。</p>
     */
    public Merchant requireCurrentWithdrawableMerchant() {
        String role = roleGuard.normalizeRole(UserContext.currentRole());
        boolean activityLeader = roleGuard.isActivityLeader();
        if (!"merchant".equals(role) && !"activity_leader".equals(role) && !"technician".equals(role)
                && !activityLeader) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        Resident merchantUser = requireCurrentUser();
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }

        Merchant merchant = resolveWithdrawalMerchant(merchantUser, role, activityLeader);
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

    private Merchant resolveWithdrawalMerchant(Resident user, String role, boolean activityLeader) {
        if ("technician".equals(role)) {
            return merchantRepository
                    .findByContactPhoneAndPropertyCompanyIdAndMerchantSource(
                            user.getPhone(), user.getPropertyCompanyId(), "technician")
                    .orElseGet(() -> merchantBindingService.ensureMerchant(user.getId(), "technician"));
        }
        // 组长（含标签化后的 resident+组长身份、存量 JWT role=activity_leader）定位 group_leader 店
        if (activityLeader || "activity_leader".equals(role)) {
            return merchantRepository
                    .findByContactPhoneAndPropertyCompanyIdAndMerchantSource(
                            user.getPhone(), user.getPropertyCompanyId(), "group_leader")
                    .orElseGet(() -> merchantBindingService.ensureMerchant(user.getId(), "group_leader"));
        }
        return requireOwnedMerchant(merchantRepository, user);
    }
}