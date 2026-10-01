package com.wuye.points.payment;

import com.wuye.points.entity.WechatSplitReceiverAccount;
import com.wuye.points.repository.WechatSplitReceiverAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 微信收款账户解析服务：按主体类型 + 实体 ID 解析出可用于普通分账的微信账户号。
 *
 * <p>仅返回已通过校验（verified=true）的账户；缺户 / 注册中返回 null，
 * 调用方据此将分账记录置为 {@code skipped} 等待下一周期补分。</p>
 */
@Service
@RequiredArgsConstructor
public class WechatSplitReceiverAccountService {

    private final WechatSplitReceiverAccountRepository repository;

    /**
     * 解析已通过校验（verified=true）的收款账户完整信息（账户号 + 姓名）。
     * 缺户 / 注册中返回 null，调用方据此将分账记录置为 {@code skipped} 等待下一周期补分。
     */
    public WechatSplitReceiverAccount resolveAccount(String ownerType, String ownerId) {
        if (!StringUtils.hasText(ownerType) || !StringUtils.hasText(ownerId)) {
            return null;
        }
        return repository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .filter(WechatSplitReceiverAccount::isVerified)
                .orElse(null);
    }

    /**
     * 解析收款方微信账户号（商户号或 openid）。
     *
     * @return 有效账户号；缺户或未完成校验时返回 null
     */
    public String resolveAccountNo(String ownerType, String ownerId) {
        WechatSplitReceiverAccount account = resolveAccount(ownerType, ownerId);
        return account == null ? null : account.getAccountNo();
    }
}
